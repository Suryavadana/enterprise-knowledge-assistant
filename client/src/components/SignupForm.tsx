import { useState } from "react";
import type { FormEvent } from "react";
import { useAuth } from "../context/AuthContext";

// A "controlled input" means React state is the single source of truth for
// the input's value, instead of the DOM. We give the <input> its value from
// state (value={email}) and update that state on every keystroke
// (onChange={(e) => setEmail(e.target.value)}). This way `email` always
// reflects exactly what's in the field, and we can read it directly when
// the form is submitted.
export default function SignupForm() {
  const { signup } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");

  // Tracks whether the signup request is currently in flight, so we can
  // disable the submit button and avoid duplicate submissions.
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Holds the error message to display, or null if there isn't one.
  const [error, setError] = useState<string | null>(null);

 async function handleSubmit(e: FormEvent<HTMLFormElement>) {
  e.preventDefault();
  setError(null);
  setIsSubmitting(true);
  try {
    await signup(email, password, name);
  } catch (err) {
    setError(err instanceof Error ? err.message : "Signup failed");
  } finally {
    setIsSubmitting(false);
  }
  }

  return (
    <div className="auth-card">
      <h1>Sign up</h1>
      <form onSubmit={handleSubmit}>
        <div className="form-field">
          <label htmlFor="name">Name</label>
          <input
            id="name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </div>

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

        {error && <p className="error-text">{error}</p>}

        <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
          {isSubmitting ? "Signing up..." : "Sign up"}
        </button>
      </form>
    </div>
  );
}
