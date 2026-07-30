// API layer for the prompt template endpoints.
// Talks to the Spring Boot backend running at BASE_URL.

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export interface PromptTemplate {
  id: number;
  title: string;
  content: string;
  createdAt: string;
}

// Lists the current user's prompt templates, authenticated with the given
// bearer token. Resolves with an array of templates on success (200), or
// throws an Error on failure.
export async function getPromptTemplates(token: string): Promise<PromptTemplate[]> {
  const response = await fetch(`${BASE_URL}/api/prompt-templates`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<PromptTemplate[]>;
}

// Creates a new prompt template, authenticated with the given bearer token.
// Resolves with the created template on success (200), or throws an Error on
// failure.
export async function createPromptTemplate(
  token: string,
  title: string,
  content: string,
): Promise<PromptTemplate> {
  const response = await fetch(`${BASE_URL}/api/prompt-templates`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ title, content }),
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<PromptTemplate>;
}

// Deletes a prompt template, authenticated with the given bearer token.
// Resolves on success (200), or throws an Error on failure.
export async function deletePromptTemplate(token: string, id: number): Promise<void> {
  const response = await fetch(`${BASE_URL}/api/prompt-templates/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }
}
