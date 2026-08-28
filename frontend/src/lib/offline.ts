import {
  type DBSchema,
  type IDBPDatabase,
  openDB,
  type StoreKey,
  type StoreNames,
  type StoreValue,
} from "idb";
import { base64url, importSPKI, jwtVerify } from "jose";
import type { LibraryItem } from "./types";

const DB_NAME = "offline";
const DB_VERSION = 4;
const KEY_STORE = "device-keys";

export const BOOK_STORE = "offline-books";
export const ACCOUNT_STORE = "offline-account";
export const ACTIVE_ACCOUNT_KEY = "active-account";
const CLOCK_KEY_STORE = "clock-key";
const CLOCK_KEY_ID = "browser-clock-key";
const CLOCK_STATE_VERSION = 1;
const CLOCK_IV_LENGTH = 12;
const CLOCK_STATE_ERROR = "Dữ liệu offline đã thay đổi.";

export interface AccountKey {
  id: number;
  privateKey: CryptoKey;
  publicKeySpki: string;
  createdAt: string;
}

export type OfflineBookMetadata = Pick<
  LibraryItem,
  "title" | "authors" | "type"
>;

export interface EncryptedClockState {
  version: 1;
  iv: ArrayBuffer;
  ciphertext: ArrayBuffer;
}

export interface BookData {
  userId: number;
  bookId: number;
  metadata: OfflineBookMetadata;
  license: string;
  encryptedFile: Blob;
  downloadedAt: string;
  clockState?: EncryptedClockState;
}

export interface OfflineAccount {
  userId: number;
  username: string;
  firstName: string;
  lastName: string;
  loggedInAt: string;
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
  "offline-account": {
    key: string;
    value: OfflineAccount;
  };
  "clock-key": {
    key: string;
    value: CryptoKey;
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
        if (!db.objectStoreNames.contains(ACCOUNT_STORE)) {
          db.createObjectStore(ACCOUNT_STORE);
        }
        if (!db.objectStoreNames.contains(CLOCK_KEY_STORE)) {
          db.createObjectStore(CLOCK_KEY_STORE);
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

export const save = async <StoreName extends StoreNames<MyDB>>(
  objectKey: StoreName,
  value: StoreValue<MyDB, StoreName>,
) => {
  const db: IDBPDatabase<MyDB> = await getDatabase();

  switch (objectKey) {
    case KEY_STORE: {
      const accountKey = value as AccountKey;
      await db.add(KEY_STORE, accountKey, accountKey.id);
      return;
    }
    case BOOK_STORE: {
      const bookData = value as BookData;
      await db.put(BOOK_STORE, bookData, [bookData.userId, bookData.bookId]);
      return;
    }
    case ACCOUNT_STORE: {
      const account = value as OfflineAccount;
      await db.put(ACCOUNT_STORE, account, ACTIVE_ACCOUNT_KEY);
      return;
    }

    default:
      throw new Error("Object key không hợp lệ");
  }
};

export const del = async <StoreName extends StoreNames<MyDB>>(
  objectKey: StoreName,
  key: StoreKey<MyDB, StoreName>,
) => {
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

const generateClockKey = async (): Promise<CryptoKey> => {
  return crypto.subtle.generateKey(
    {
      name: "AES-GCM",
      length: 256,
    },
    false,
    ["encrypt", "decrypt"],
  );
};

const getOrCreateClockKeyForDownload = async (): Promise<CryptoKey> => {
  const db = await getDatabase();
  const storedKey = await db.get(CLOCK_KEY_STORE, CLOCK_KEY_ID);
  if (storedKey !== undefined) {
    return storedKey;
  }

  const generatedKey = await generateClockKey();
  await db.add(CLOCK_KEY_STORE, generatedKey, CLOCK_KEY_ID);
  return generatedKey;
};

const getClockKeyForRead = async (): Promise<CryptoKey> => {
  const db = await getDatabase();
  const key = await db.get(CLOCK_KEY_STORE, CLOCK_KEY_ID);

  if (key === undefined) {
    throw new Error(CLOCK_STATE_ERROR);
  }

  return key;
};

const copyToArrayBuffer = (bytes: Uint8Array): ArrayBuffer => {
  const buffer = new ArrayBuffer(bytes.byteLength);
  new Uint8Array(buffer).set(bytes);
  return buffer;
};

const encryptClockState = async (
  key: CryptoKey,
  lastSeenAt: number,
  userId: number,
  bookId: number,
): Promise<EncryptedClockState> => {
  const iv = new ArrayBuffer(CLOCK_IV_LENGTH);
  crypto.getRandomValues(new Uint8Array(iv));

  const encodedPayload = new TextEncoder().encode(
    JSON.stringify({
      userId,
      bookId,
      lastSeenAt,
    }),
  );

  const plaintext = copyToArrayBuffer(encodedPayload);
  const ciphertext = await crypto.subtle.encrypt(
    {
      name: "AES-GCM",
      iv,
      tagLength: 128,
    },
    key,
    plaintext,
  );

  return {
    version: CLOCK_STATE_VERSION,
    iv,
    ciphertext,
  };
};

const decryptClockState = async (
  key: CryptoKey,
  clockState: EncryptedClockState,
  expectedUserId: number,
  expectedBookId: number,
  licenseIat: number,
): Promise<number> => {
  if (
    clockState.version !== CLOCK_STATE_VERSION ||
    !(clockState.iv instanceof ArrayBuffer) ||
    clockState.iv.byteLength !== CLOCK_IV_LENGTH ||
    !(clockState.ciphertext instanceof ArrayBuffer)
  ) {
    throw new Error(CLOCK_STATE_ERROR);
  }

  let plaintext: ArrayBuffer;

  try {
    plaintext = await crypto.subtle.decrypt(
      {
        name: "AES-GCM",
        iv: clockState.iv,
        tagLength: 128,
      },
      key,
      clockState.ciphertext,
    );
  } catch {
    throw new Error(CLOCK_STATE_ERROR);
  }

  let payload: unknown;

  try {
    payload = JSON.parse(new TextDecoder().decode(plaintext));
  } catch {
    throw new Error(CLOCK_STATE_ERROR);
  }

  if (typeof payload !== "object" || payload === null) {
    throw new Error(CLOCK_STATE_ERROR);
  }

  const clockPayload = payload as {
    userId?: unknown;
    bookId?: unknown;
    lastSeenAt?: unknown;
  };

  if (
    clockPayload.userId !== expectedUserId ||
    clockPayload.bookId !== expectedBookId ||
    typeof clockPayload.lastSeenAt !== "number" ||
    clockPayload.lastSeenAt < licenseIat
  ) {
    throw new Error(CLOCK_STATE_ERROR);
  }

  return clockPayload.lastSeenAt;
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
  book: Pick<LibraryItem, "bookId" | "title" | "authors" | "type">,
  license: string,
  downloadUrl: string,
) => {
  if (license.trim() === "") {
    throw new Error("License không hợp lệ");
  }

  const verifiedLicense = await verifyLicenseClaims(
    license,
    userId,
    book.bookId,
  );

  const res: Response = await fetch(downloadUrl, {
    credentials: "omit",
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Lỗi tải file");
  }

  const encryptedFile: Blob = await res.blob();

  let clockState: EncryptedClockState | undefined;
  if (verifiedLicense.licenseType === "SUBSCRIPTION") {
    const clockKey = await getOrCreateClockKeyForDownload();

    clockState = await encryptClockState(
      clockKey,
      verifiedLicense.iat,
      userId,
      book.bookId,
    );
  }

  const bookData: BookData = {
    userId,
    bookId: book.bookId,
    metadata: {
      title: book.title,
      authors: book.authors,
      type: book.type,
    },
    license,
    encryptedFile,
    downloadedAt: new Date().toISOString(),
    clockState,
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

async function verifyLicenseClaims(
  license: string,
  expectedUserId: number,
  expectedBookId: number,
): Promise<LibraryLicense> {
  const publicKeyBase64 = process.env.LICENSE_KEY;
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

  if (
    payload.offlineUntil < payload.iat ||
    payload.exp < payload.offlineUntil
  ) {
    throw new Error("Thời hạn license không hợp lệ");
  }

  return {
    ...baseLicense,
    licenseType: "SUBSCRIPTION",
    subId: payload.subId,
    offlineUntil: payload.offlineUntil,
    exp: payload.exp,
  };
}

interface VerifyLicenseResult {
  license: LibraryLicense;
  updatedClockState?: EncryptedClockState;
}

export const verifyLicense = async (
  bookData: BookData,
  expectedUserId: number,
  expectedBookId: number,
): Promise<VerifyLicenseResult> => {
  const license = await verifyLicenseClaims(
    bookData.license,
    expectedUserId,
    expectedBookId,
  );

  if (license.licenseType === "PURCHASED") {
    return {
      license,
    };
  }

  if (bookData.clockState === undefined) {
    throw new Error(CLOCK_STATE_ERROR);
  }

  const clockKey = await getClockKeyForRead();

  const lastSeenAt = await decryptClockState(
    clockKey,
    bookData.clockState,
    expectedUserId,
    expectedBookId,
    license.iat,
  );

  const trustedAt = Math.max(license.iat, lastSeenAt);
  const deviceNow = Math.floor(Date.now() / 1000);
  if (deviceNow < trustedAt - 300) {
    throw new Error("Đồng hồ đã bị chỉnh sửa");
  }

  const effectiveNow = Math.max(deviceNow, trustedAt);
  if (effectiveNow >= license.offlineUntil || effectiveNow >= license.exp) {
    throw new Error("Subscription license đã hết hạn");
  }

  const updatedClockState = await encryptClockState(
    clockKey,
    effectiveNow,
    expectedUserId,
    expectedBookId,
  );

  return {
    license,
    updatedClockState,
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
