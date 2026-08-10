import { type DBSchema, type IDBPDatabase, openDB } from "idb";

const DB_NAME = "offline";
const DB_VERSION = 1;
const KEY_STORE = "device-keys";

interface AccountKey {
  id: number;
  privateKey: CryptoKey;
  publicKeySpki: string;
  createdAt: string;
}

interface CryptoDB extends DBSchema {
  "device-keys": {
    key: number;
    value: AccountKey;
  };
}

let dbPromise: Promise<IDBPDatabase<CryptoDB>> | null = null;

const getDatabase = (): Promise<IDBPDatabase<CryptoDB>> => {
  if (typeof globalThis.indexedDB === "undefined") {
    return Promise.reject(new Error("Trình duyệt không hỗ trợ IndexedDB"));
  }

  if (dbPromise === null) {
    dbPromise = openDB<CryptoDB>(DB_NAME, DB_VERSION, {
      upgrade(db) {
        if (!db.objectStoreNames.contains(KEY_STORE)) {
          db.createObjectStore(KEY_STORE);
        }
      },
    }).catch((error: unknown) => {
      dbPromise = null;
      throw error;
    });
  }
  return dbPromise;
};

const saveKey = async (accountKey: AccountKey): Promise<void> => {
  const db = await getDatabase();
  await db.add(KEY_STORE, accountKey, accountKey.id);
};

const getKey = async (userId: number): Promise<AccountKey | undefined> => {
  const db = await getDatabase();
  return await db.get(KEY_STORE, userId);
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

  const publicKeySpki = btoa(
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
  const key = await getKey(userId);
  if (key !== undefined) {
    return key;
  }

  const accountKey = await generateKey(userId);
  await saveKey(accountKey);
  return accountKey;
};
