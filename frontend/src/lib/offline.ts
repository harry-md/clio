import { Api } from "@/lib/api";

const DATABASE_NAME = "clio-offline";
const DATABASE_VERSION = 1;

const DEVICE_KEYS_STORE = "device-keys";
const OFFLINE_BOOKS_STORE = "offline-books";
const OFFLINE_BOOK_OWNER_INDEX = "owner";

const DEVICE_KEY_ID = "browser-device-key";

interface DeviceKeyRecord {
  id: string;
  privateKey: CryptoKey;
  publicKeySpki: string;
}

interface DownloadResponse {
  downloadUrl: string;
  urlExpiredAt: string;
  license: string;
}

export interface OfflineBookRecord {
  owner: string;
  bookId: number;
  encryptedFile: Blob;
  license: string;
  downloadedAt: string;
}

let databasePromise: Promise<IDBDatabase> | null = null;
let deviceKeyPromise: Promise<DeviceKeyRecord> | null = null;

function requestToPromise<T>(request: IDBRequest<T>): Promise<T> {
  return new Promise((resolve, reject) => {
    request.onsuccess = () => {
      resolve(request.result);
    };

    request.onerror = () => {
      reject(
        request.error ?? new Error("Không thể thực hiện thao tác IndexedDB"),
      );
    };
  });
}

function transactionToPromise(transaction: IDBTransaction): Promise<void> {
  return new Promise((resolve, reject) => {
    transaction.oncomplete = () => {
      resolve();
    };

    transaction.onerror = () => {
      reject(transaction.error ?? new Error("IndexedDB transaction thất bại"));
    };

    transaction.onabort = () => {
      reject(transaction.error ?? new Error("IndexedDB transaction bị hủy"));
    };
  });
}

function openOfflineDatabase(): Promise<IDBDatabase> {
  if (databasePromise) {
    return databasePromise;
  }

  if (!globalThis.indexedDB) {
    return Promise.reject(new Error("Trình duyệt không hỗ trợ IndexedDB"));
  }

  databasePromise = new Promise((resolve, reject) => {
    const request = indexedDB.open(DATABASE_NAME, DATABASE_VERSION);

    request.onupgradeneeded = () => {
      const database = request.result;

      if (!database.objectStoreNames.contains(DEVICE_KEYS_STORE)) {
        database.createObjectStore(DEVICE_KEYS_STORE, {
          keyPath: "id",
        });
      }

      if (!database.objectStoreNames.contains(OFFLINE_BOOKS_STORE)) {
        const offlineBooksStore = database.createObjectStore(
          OFFLINE_BOOKS_STORE,
          {
            keyPath: ["owner", "bookId"],
          },
        );

        offlineBooksStore.createIndex(OFFLINE_BOOK_OWNER_INDEX, "owner", {
          unique: false,
        });
      }
    };

    request.onsuccess = () => {
      const database = request.result;

      database.onversionchange = () => {
        database.close();
      };

      resolve(database);
    };

    request.onerror = () => {
      reject(request.error ?? new Error("Không thể mở IndexedDB"));
    };

    request.onblocked = () => {
      reject(new Error("IndexedDB đang bị khóa bởi một tab khác"));
    };
  });

  return databasePromise;
}

function arrayBufferToBase64(buffer: ArrayBuffer): string {
  const bytes = new Uint8Array(buffer);
  let binary = "";

  for (const byte of bytes) {
    binary += String.fromCharCode(byte);
  }

  return btoa(binary);
}

async function readDeviceKey(): Promise<DeviceKeyRecord | undefined> {
  const database = await openOfflineDatabase();
  const transaction = database.transaction(DEVICE_KEYS_STORE, "readonly");

  const request = transaction
    .objectStore(DEVICE_KEYS_STORE)
    .get(DEVICE_KEY_ID) as IDBRequest<DeviceKeyRecord | undefined>;

  const record = await requestToPromise(request);
  await transactionToPromise(transaction);

  return record;
}

async function createDeviceKey(): Promise<DeviceKeyRecord> {
  const keyPair = (await crypto.subtle.generateKey(
    {
      name: "RSA-OAEP",
      modulusLength: 2048,
      publicExponent: new Uint8Array([1, 0, 1]),
      hash: "SHA-256",
    },
    false,
    ["encrypt", "decrypt"],
  )) as CryptoKeyPair;

  const publicKeySpkiBuffer = await crypto.subtle.exportKey(
    "spki",
    keyPair.publicKey,
  );

  const record: DeviceKeyRecord = {
    id: DEVICE_KEY_ID,
    privateKey: keyPair.privateKey,
    publicKeySpki: arrayBufferToBase64(publicKeySpkiBuffer),
  };

  const database = await openOfflineDatabase();
  const transaction = database.transaction(DEVICE_KEYS_STORE, "readwrite");

  transaction.objectStore(DEVICE_KEYS_STORE).put(record);

  await transactionToPromise(transaction);

  return record;
}

async function loadOrCreateDeviceKey(): Promise<DeviceKeyRecord> {
  const existingKey = await readDeviceKey();

  if (existingKey) {
    return existingKey;
  }

  return createDeviceKey();
}

export function ensureDeviceKey(): Promise<DeviceKeyRecord> {
  if (!deviceKeyPromise) {
    deviceKeyPromise = loadOrCreateDeviceKey().catch((error: unknown) => {
      deviceKeyPromise = null;
      throw error;
    });
  }

  return deviceKeyPromise;
}

export async function getDevicePrivateKey(): Promise<CryptoKey> {
  const deviceKey = await ensureDeviceKey();
  return deviceKey.privateKey;
}

async function storeOfflineBook(record: OfflineBookRecord): Promise<void> {
  const database = await openOfflineDatabase();
  const transaction = database.transaction(OFFLINE_BOOKS_STORE, "readwrite");

  transaction.objectStore(OFFLINE_BOOKS_STORE).put(record);

  await transactionToPromise(transaction);
}

export async function getDownloadedBookIds(
  owner: string,
): Promise<Set<number>> {
  const database = await openOfflineDatabase();
  const transaction = database.transaction(OFFLINE_BOOKS_STORE, "readonly");

  const index = transaction
    .objectStore(OFFLINE_BOOKS_STORE)
    .index(OFFLINE_BOOK_OWNER_INDEX);

  const keys = await requestToPromise(index.getAllKeys(owner));

  await transactionToPromise(transaction);

  const bookIds = keys.flatMap((key) => {
    if (Array.isArray(key) && typeof key[1] === "number") {
      return [key[1]];
    }

    return [];
  });

  return new Set(bookIds);
}

export async function downloadBookForOffline(
  owner: string,
  bookId: number,
): Promise<OfflineBookRecord> {
  const deviceKey = await ensureDeviceKey();

  const { data } = await Api.post<DownloadResponse>("/libraries/download", {
    bookId,
    publicKeySpki: deviceKey.publicKeySpki,
  });

  const urlExpiration = Date.parse(data.urlExpiredAt);

  if (Number.isNaN(urlExpiration) || urlExpiration <= Date.now()) {
    throw new Error("Đường dẫn tải sách đã hết hạn");
  }

  const response = await fetch(data.downloadUrl);

  if (!response.ok) {
    throw new Error(`Không thể tải file sách (${response.status})`);
  }

  const encryptedFile = await response.blob();

  if (encryptedFile.size === 0) {
    throw new Error("File sách tải về bị rỗng");
  }

  const record: OfflineBookRecord = {
    owner,
    bookId,
    encryptedFile,
    license: data.license,
    downloadedAt: new Date().toISOString(),
  };

  await storeOfflineBook(record);

  return record;
}
