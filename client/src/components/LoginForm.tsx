import { useState } from "react";
import type { FormEvent } from "react";
//import { login } from "../api/auth";
import { useAuth } from "../context/AuthContext"

export default function LoginForm() {
  
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  // Tracks whether the login request is currently in flight, so we can
  // disable the submit button and avoid duplicate submissions.
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Holds the error message to display, or null if there isn't one.
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();

    setError(null);
    setIsSubmitting(true);

    try {
  // The context's login() throws the same Error (with the backend's
  // message) on failure. On success, it has already saved the token to
  // localStorage and updated user/token in context state - nothing left
  // for us to do here.
  await login(email, password);
} catch (err) {
  setError(err instanceof Error ? err.message : "Login failed");
} finally {
  setIsSubmitting(false);
}
  }

  return (
    <div className="auth-card">
      <h1 className="auth-logo">Enterprise Knowledge Assistant</h1>
      <p className="auth-subtitle">Sign in to your account</p>
      <form onSubmit={handleSubmit}>
        <div className="form-field">
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>

        <div className="form-field">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        {error && <p className="auth-error">{error}</p>}

        <button type="submit" className="btn btn-primary btn-block" disabled={isSubmitting}>
          {isSubmitting ? "Logging in..." : "Log in"}
        </button>
      </form>
    </div>
  );
}
