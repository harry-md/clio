"use client";

import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { Api } from "@/lib/api";
import { getOrCreateKey } from "@/lib/offline";
import type { AuthUser } from "@/lib/types";

interface AuthContextValue {
  user: AuthUser | null;
  initialized: boolean;
  setUser: (user: AuthUser | null) => void;
  refreshUser: () => Promise<AuthUser>;
}

interface AuthProviderProps {
  children: ReactNode;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const getCurrentUser = async () => {
  const { data } = await Api.get<AuthUser>("/current-user");
  return data;
};

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [initialized, setInitialized] = useState(false);

  const refreshUser = useCallback(async () => {
    const currentUser = await getCurrentUser();
    setUser(currentUser);
    return currentUser;
  }, []);

  useEffect(() => {
    let active = true;

    const restoreUser = async () => {
      try {
        const { data } = await Api.get<AuthUser>("/current-user");

        if (active) {
          setUser(data);
        }
      } catch {
        if (active) {
          setUser(null);
        }
      } finally {
        if (active) {
          setInitialized(true);
        }
      }
    };

    void restoreUser();

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!user) {
      return;
    }
    void getOrCreateKey(user.id);
  }, [user]);

  return (
    <AuthContext.Provider
      value={{
        user,
        initialized,
        setUser,
        refreshUser,
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
