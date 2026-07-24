import { createContext, useContext, useState } from "react";
import type { ReactNode } from "react";
import { login as loginApi, signup as signupApi } from "../api/auth";
import type { User } from "../api/auth";
import { saveToken, getToken, clearToken } from "../utils/tokenStorage";

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  signup: (email: string, password: string, name: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

// The context itself just holds a value of type AuthContextType (or
// undefined if no Provider is above it in the tree). Components don't read
// this directly - they go through the useAuth() hook below.
const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);

  // Lazy initializer: the function passed to useState only runs once, on
  // the very first render, to compute the starting value. Since getToken()
  // just reads localStorage synchronously, this avoids any gap where token
  // briefly starts as null and then flips a moment later.
  const [token, setToken] = useState<string | null>(() => getToken());

  // NOTE: this is currently always false, since restoring the token above
  // is synchronous - there's no actual async work to "wait" for yet.
  // This will become real once we add a step that validates the restored
  // token against the backend (e.g. calling GET /api/auth/me on mount) -
  // at that point this becomes a real useState with a setter again.
  const isLoading = false;

  async function login(email: string, password: string) {
    const response = await loginApi({ email, password });
    saveToken(response.token);
    setToken(response.token);
    setUser(response.user);
  }

  async function signup(email: string, password: string, name: string) {
    await signupApi({ email, password, name });
    await login(email, password);
  }

  function logout() {
    clearToken();
    setToken(null);
    setUser(null);
  }

  const value: AuthContextType = { user, token, login, signup, logout, isLoading };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// Custom hook for consuming the context. Components call useAuth() instead
// of useContext(AuthContext) directly, for two reasons:
//
// 1. It's the only place that needs to import AuthContext, so callers don't
//    have to know it exists.
// 2. The "throw if undefined" check turns a silent bug into a loud one.
//    Without a Provider above it, useContext(AuthContext) would just return
//    `undefined` - and every field access (user.name, login(...)) would
//    fail with a confusing runtime error far from the real cause (forgetting
//    to wrap the app in <AuthProvider>). Throwing here fails immediately,
//    at the point of misuse, with a message that says exactly what's wrong.
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}