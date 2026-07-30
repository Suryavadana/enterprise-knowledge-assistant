// API layer for the conversations endpoints.
// Talks to the Spring Boot backend running at BASE_URL.

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

// Item returned by GET /api/conversations
export interface ConversationSummary {
  id: number;
  title: string | null;
  createdAt: string;
}

// Item returned by GET /api/conversations/{id}/messages
export interface MessageDto {
  id: number;
  role: "USER" | "ASSISTANT";
  content: string;
  createdAt: string;
}

// Shared helper: performs an authenticated GET call and unwraps the JSON
// body. If the response status is not ok (i.e. not 2xx), the backend sends
// the error as plain text, so we read it with `.text()` and throw it as a
// regular JS Error. Callers can catch this with try/catch and show
// `error.message` to the user.
async function getJson<TResponse>(path: string, token: string): Promise<TResponse> {
  const response = await fetch(`${BASE_URL}${path}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<TResponse>;
}

// Lists the current user's conversations. Resolves with an array of
// conversation summaries on success (200).
export async function getConversations(token: string): Promise<ConversationSummary[]> {
  return getJson<ConversationSummary[]>("/api/conversations", token);
}

// Lists the messages in a single conversation. Resolves with an array of
// messages on success (200), or throws an Error (e.g. "Forbidden",
// "Not Found") on failure.
export async function getMessages(token: string, conversationId: number): Promise<MessageDto[]> {
  return getJson<MessageDto[]>(`/api/conversations/${conversationId}/messages`, token);
}
