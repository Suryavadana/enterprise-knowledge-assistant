// API layer for the chat endpoint.
// Talks to the Spring Boot backend running at BASE_URL.

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

// Body we send to POST /api/chat
export interface ChatRequest {
  conversationId: number | null;
  message: string;
}

export interface Citation {
  filename: string;
  documentId: number;
  chunkIndex: number;
}

// Body returned by POST /api/chat on success (200).
export interface ChatResponse {
  conversationId: number;
  reply: string;
  citations: Citation[];
  assistantMessageId: number;
}

// Sends a chat message, authenticated with the given bearer token. Resolves
// with the assistant's reply on success (200), or throws an Error (e.g.
// "Forbidden", "Not Found") on failure.
export async function sendMessage(token: string, data: ChatRequest): Promise<ChatResponse> {
  const response = await fetch(`${BASE_URL}/api/chat`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<ChatResponse>;
}

// Submits feedback for a specific assistant message, authenticated with the
// given bearer token. Resolves on success (200), or throws an Error on
// failure.
export async function submitFeedback(
  token: string,
  messageId: number,
  rating: "UP" | "DOWN",
): Promise<void> {
  const response = await fetch(`${BASE_URL}/api/messages/${messageId}/feedback`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ rating, comment: null }),
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }
}
