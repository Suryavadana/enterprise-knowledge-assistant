// API layer for authentication endpoints (signup, login).
// Talks to the Spring Boot backend running at BASE_URL.

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

// A user's role, as returned by the backend. TypeScript will only allow
// these two exact strings wherever this type is used.
export type Role = "USER" | "ADMIN";

// Shape of the user object embedded in a successful login response,
// and of the successful signup response itself.
export interface User {
  id: number;
  email: string;
  name: string;
  role: Role;
}

// Body we send to POST /api/auth/signup
export interface SignupRequest {
  email: string;
  password: string;
  name: string;
}

// Body returned by POST /api/auth/signup on success (201).
// It's just a User, so we reuse the type instead of duplicating the fields.
export type SignupResponse = User;

// Body we send to POST /api/auth/login
export interface LoginRequest {
  email: string;
  password: string;
}

// Body returned by POST /api/auth/login on success (200).
export interface LoginResponse {
  token: string;
  user: User;
}

// Shared helper: performs a fetch call and unwraps the JSON body.
//
// - `path` is appended to BASE_URL (e.g. "/api/auth/login").
// - `body` is JSON-stringified and sent as the request body.
// - If the response status is not ok (i.e. not 2xx), the backend sends the
//   error as plain text, so we read it with `.text()` and throw it as a
//   regular JS Error. Callers can catch this with try/catch and show
//   `error.message` to the user.
async function postJson<TResponse>(path: string, body: unknown): Promise<TResponse> {
  const response = await fetch(`${BASE_URL}${path}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<TResponse>;
}

// Registers a new user. Resolves with the created user on success (201),
// or throws an Error (e.g. "Email is already registered") on failure (409).
export async function signup(data: SignupRequest): Promise<SignupResponse> {
  return postJson<SignupResponse>("/api/auth/signup", data);
}

// Logs a user in. Resolves with a token + user on success (200),
// or throws an Error (e.g. "Invalid email or password") on failure (401).
export async function login(data: LoginRequest): Promise<LoginResponse> {
  return postJson<LoginResponse>("/api/auth/login", data);
}
