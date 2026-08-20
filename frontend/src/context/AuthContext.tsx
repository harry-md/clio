"use client";

import { isAxiosError } from "axios";
import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { Api } from "@/lib/api";
import {
  ACCOUNT_STORE,
  ACTIVE_ACCOUNT_KEY,
  del,
  get,
  type OfflineAccount,
  save,
} from "@/lib/offline";
import type { AuthUser } from "@/lib/types";

interface AuthContextValue {
  user: AuthUser | null;
  offlineAccount: OfflineAccount | null;
  initialized: boolean;
  refreshUser: () => Promise<AuthUser>;
  clearUser: () => Promise<void>;
}

interface AuthProviderProps {
  children: ReactNode;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const getCurrentUser = async (): Promise<AuthUser> => {
  const { data } = await Api.get<AuthUser>("/current-user");
  return data;
};

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<AuthUser | null>(null);

  const [offlineAccount, setOfflineAccount] = useState<OfflineAccount | null>(
    null,
  );

  const [initialized, setInitialized] = useState(false);

  const refreshUser = useCallback(async (): Promise<AuthUser> => {
    const currentUser = await getCurrentUser();
    setUser(currentUser);
    return currentUser;
  }, []);

  const clearUser = useCallback(async () => {
    try {
      await del(ACCOUNT_STORE, ACTIVE_ACCOUNT_KEY);
    } catch {}

    setUser(null);
    setOfflineAccount(null);
  }, []);

  useEffect(() => {
    const restoreUser = async () => {
      try {
        const currentUser = await getCurrentUser();

        setUser(currentUser);
      } catch (error: unknown) {
        setUser(null);

        const status = isAxiosError(error) ? error.response?.status : undefined;

        if (status === 401 || status === 403) {
          await del(ACCOUNT_STORE, ACTIVE_ACCOUNT_KEY).catch(() => undefined);
          setOfflineAccount(null);
          return;
        }

        try {
          const savedAccount = await get(ACCOUNT_STORE, ACTIVE_ACCOUNT_KEY);

          setOfflineAccount(savedAccount ?? null);
        } catch {
          setOfflineAccount(null);
        }
      } finally {
        setInitialized(true);
      }
    };

    void restoreUser();
  }, []);

  useEffect(() => {
    if (!user) {
      return;
    }

    const account: OfflineAccount = {
      userId: user.id,
      username: user.username,
      firstName: user.firstName,
      lastName: user.lastName,
      loggedInAt: new Date().toISOString(),
    };

    setOfflineAccount(account);

    void save(ACCOUNT_STORE, account).catch((error: unknown) => {
      console.error("Lỗi khi lưu offline account", error);
    });
  }, [user]);

  return (
    <AuthContext.Provider
      value={{
        user,
        offlineAccount,
        initialized,
        refreshUser,
        clearUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error("useAuth phải gọi trong AuthProvider");
  }

  return context;
};
