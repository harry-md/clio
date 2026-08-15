import {
  type DBSchema,
  type IDBPDatabase,
  openDB,
  type StoreKey,
  type StoreNames,
  type StoreValue,
} from "idb";
import { base64url, importSPKI, jwtVerify } from "jose";

const DB_NAME = "offline";
const DB_VERSION = 2;
const KEY_STORE = "device-keys";
export const BOOK_STORE = "offline-books";

export interface AccountKey {
  id: number;
  privateKey: CryptoKey;
  publicKeySpki: string;
  createdAt: string;
}

export interface BookData {
  userId: number;
  bookId: number;
  license: string;
  encryptedFile: Blob;
  downloadedAt: string;
}

interface MyDB extends DBSchema {
  "device-keys": {
    key: number;
    value: AccountKey;
  };
  "offline-books": {
    key: [number, number];
    value: BookData;
  };
}

let dbPromise: Promise<IDBPDatabase<MyDB>> | null = null;

const getDatabase = (): Promise<IDBPDatabase<MyDB>> => {
  if (typeof globalThis.indexedDB === "undefined") {
    return Promise.reject(new Error("Trình duyệt không hỗ trợ IndexedDB"));
  }

  if (dbPromise === null) {
    dbPromise = openDB<MyDB>(DB_NAME, DB_VERSION, {
      upgrade(db) {
        if (!db.objectStoreNames.contains(KEY_STORE)) {
          db.createObjectStore(KEY_STORE);
        }
        if (!db.objectStoreNames.contains(BOOK_STORE)) {
          db.createObjectStore(BOOK_STORE);
        }
      },
    }).catch((error: unknown) => {
      dbPromise = null;
      throw error;
    });
  }
  return dbPromise;
};

export const get = async <StoreName extends StoreNames<MyDB>>(
  objectKey: StoreName,
  key: StoreKey<MyDB, StoreName>,
): Promise<StoreValue<MyDB, StoreName> | undefined> => {
  const db: IDBPDatabase<MyDB> = await getDatabase();

  return db.get(objectKey, key);
};

const save = async <StoreName extends StoreNames<MyDB>>(
  objectKey: StoreName,
  value: StoreValue<MyDB, StoreName>,
): Promise<void> => {
  const db: IDBPDatabase<MyDB> = await getDatabase();

  if (objectKey === KEY_STORE) {
    const accountKey = value as AccountKey;
    await db.add(KEY_STORE, accountKey, accountKey.id);
    return;
  }

  const bookData = value as BookData;
  await db.put(BOOK_STORE, bookData, [bookData.userId, bookData.bookId]);
};

export const del = async <StoreName extends StoreNames<MyDB>>(
  objectKey: StoreName,
  key: StoreKey<MyDB, StoreName>,
): Promise<void> => {
  const db: IDBPDatabase<MyDB> = await getDatabase();
  await db.delete(objectKey, key);
};

const generateKey = async (userId: number): Promise<AccountKey> => {
  const keyPair: CryptoKeyPair = await crypto.subtle.generateKey(
    {
      name: "RSA-OAEP",
      modulusLength: 2048,
      publicExponent: new Uint8Array([1, 0, 1]),
      hash: "SHA-256",
    },
    false,
    ["wrapKey", "unwrapKey"],
  );

  const publicKeySpki: string = btoa(
    String.fromCharCode(
      ...new Uint8Array(
        await crypto.subtle.exportKey("spki", keyPair.publicKey),
      ),
    ),
  );

  return {
    id: userId,
    privateKey: keyPair.privateKey,
    publicKeySpki: publicKeySpki,
    createdAt: new Date().toISOString(),
  };
};

export const getOrCreateKey = async (userId: number): Promise<AccountKey> => {
  const key = await get(KEY_STORE, userId);
  if (key !== undefined) {
    return key;
  }

  const accountKey = await generateKey(userId);
  await save(KEY_STORE, accountKey);
  return accountKey;
};

export const getBooksByUser = async (userId: number): Promise<BookData[]> => {
  const db: IDBPDatabase<MyDB> = await getDatabase();

  const range = IDBKeyRange.bound(
    [userId, 0],
    [userId, Number.MAX_SAFE_INTEGER],
  );
  return db.getAll(BOOK_STORE, range);
};

export const storeBook = async (
  userId: number,
  bookId: number,
  license: string,
  downloadUrl: string,
): Promise<void> => {
  const res: Response = await fetch(downloadUrl, {
    credentials: "omit",
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Lỗi tải file");
  }

  const encryptedFile: Blob = await res.blob();
  if (encryptedFile.size === 0) {
    throw new Error("File bị lỗi");
  }

  if (license.trim() === "") {
    throw new Error("License không hợp lệ");
  }

  const bookData: BookData = {
    userId: userId,
    bookId: bookId,
    license: license,
    encryptedFile: encryptedFile,
    downloadedAt: new Date().toISOString(),
  };

  await save(BOOK_STORE, bookData);
};

interface BaseLibraryLicense {
  userId: number;
  bookId: number;
  wrappedContentKey: string;
  iat: number;
}

export type LibraryLicense =
  | (BaseLibraryLicense & { licenseType: "PURCHASED" })
  | (BaseLibraryLicense & {
      licenseType: "SUBSCRIPTION";
      subId: number;
      offlineUntil: number;
      exp: number;
    });

export const verifyLicense = async (
  license: string,
  expectedUserId: number,
  expectedBookId: number,
): Promise<LibraryLicense> => {
  const publicKeyBase64 = process.env.NEXT_PUBLIC_PUBLIC_LICENSE_KEY;
  if (!publicKeyBase64) {
    throw new Error("Không tìm thấy license public key");
  }

  const publicKeyPem = atob(publicKeyBase64);

  const publicKey = await importSPKI(publicKeyPem, "RS256");

  const { payload } = await jwtVerify(license, publicKey, {
    algorithms: ["RS256"],
  });

  if (
    payload.sub !== String(expectedUserId) ||
    typeof payload.bookId !== "number" ||
    payload.bookId !== expectedBookId
  ) {
    throw new Error("License không thuộc tài khoản hoặc sách");
  }

  if (
    typeof payload.wrappedContentKey !== "string" ||
    typeof payload.iat !== "number"
  ) {
    throw new Error("License có field sai kiểu dữ liệu");
  }

  const baseLicense: BaseLibraryLicense = {
    userId: Number(payload.sub),
    bookId: payload.bookId,
    wrappedContentKey: payload.wrappedContentKey,
    iat: payload.iat,
  };

  if (payload.licenseType === "PURCHASED") {
    return {
      ...baseLicense,
      licenseType: "PURCHASED",
    };
  }
  if (payload.licenseType !== "SUBSCRIPTION") {
    throw new Error("Loại license không hợp lệ");
  }

  if (
    typeof payload.subId !== "number" ||
    typeof payload.offlineUntil !== "number" ||
    typeof payload.exp !== "number"
  ) {
    throw new Error("License có field sai kiểu dữ liệu");
  }

  const now = Math.floor(Date.now() / 1000);
  if (payload.offlineUntil <= now || payload.exp <= now) {
    throw new Error("Subscription license đã hết hạn");
  }

  return {
    ...baseLicense,
    licenseType: "SUBSCRIPTION",
    subId: payload.subId,
    offlineUntil: payload.offlineUntil,
    exp: payload.exp,
  };
};

export const decryptFile = async (
  userId: number,
  wrappedContentKey: string,
  encryptedFile: Blob,
): Promise<ArrayBuffer> => {
  const accountKey = await getOrCreateKey(userId);

  const wrappedKey = new Uint8Array(base64url.decode(wrappedContentKey));

  const contentKey = await crypto.subtle.unwrapKey(
    "raw",
    wrappedKey,
    accountKey.privateKey,
    { name: "RSA-OAEP" },
    { name: "AES-GCM", length: 256 },
    false,
    ["decrypt"],
  );

  const data = await encryptedFile.arrayBuffer();
  const nonce = data.slice(0, 12);
  const ciphertext = data.slice(12);

  return await crypto.subtle.decrypt(
    { name: "AES-GCM", iv: nonce, tagLength: 128 },
    contentKey,
    ciphertext,
  );
};
