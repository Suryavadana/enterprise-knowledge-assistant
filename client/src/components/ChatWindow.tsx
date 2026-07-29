import { useState } from "react";
import type { Dispatch, KeyboardEvent, SetStateAction } from "react";
import { sendMessage, submitFeedback } from "../api/chat";
import type { Citation } from "../api/chat";
import { useAuth } from "../context/AuthContext";

export interface ChatMessage {
  role: "user" | "assistant";
  content: string;
  citations?: Citation[];
  messageId?: number;
  feedback?: "UP" | "DOWN";
}

// Feather-style thumb icons. They use stroke="currentColor" (no fill) so the
// feedback-btn CSS classes can drive their color, the same way any other
// text in the design system would be colored.
function ThumbIcon({ direction }: { direction: "up" | "down" }) {
  const upPath = "M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3";
  const downPath = "M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3zm7-13h2.67A2.31 2.31 0 0 1 22 4v7a2.31 2.31 0 0 1-2.33 2H17";

  return (
    <svg
      width="14"
      height="14"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d={direction === "up" ? upPath : downPath} />
    </svg>
  );
}

interface ChatWindowProps {
  messages: ChatMessage[];
  conversationId: number | null;
  setMessages: Dispatch<SetStateAction<ChatMessage[]>>;
  setConversationId: Dispatch<SetStateAction<number | null>>;
}

export default function ChatWindow({
  messages,
  conversationId,
  setMessages,
  setConversationId,
}: ChatWindowProps) {
  const { token } = useAuth();

  const [input, setInput] = useState("");

  // Tracks whether a send is currently in flight, so we can disable the
  // input/button and avoid firing off duplicate requests.
  const [isSending, setIsSending] = useState(false);

  // Holds the error message to display, or null if there isn't one.
  const [error, setError] = useState<string | null>(null);

  async function handleSend() {
    const text = input.trim();
    if (!text || !token) return;

    setError(null);
    setInput("");

    // Optimistic UI: we add the user's message to local state immediately,
    // before the network request even starts, so it shows up on screen
    // right away instead of waiting for a round trip to the server. This
    // makes the chat feel instant. If the request later fails, the
    // optimistic message stays in the list (it's still true that the user
    // sent it) and we surface the failure via the error banner instead of
    // removing it.
    setMessages((prev) => [...prev, { role: "user", content: text }]);
    setIsSending(true);

    try {
      const response = await sendMessage(token, { conversationId, message: text });
      setConversationId(response.conversationId);
      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: response.reply,
          citations: response.citations,
          messageId: response.assistantMessageId,
        },
      ]);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to send message");
    } finally {
      setIsSending(false);
    }
  }

  // Updates the `feedback` field on the one message whose messageId matches,
  // leaving every other message untouched. Only assistant messages ever
  // carry a messageId, so comparing against it directly (rather than also
  // checking message.role) is enough to target the right message - a user
  // message's messageId is always undefined and can never equal the numeric
  // id passed in here.
 function handleFeedback(messageId: number, rating: "UP" | "DOWN") {
  if (!token) return;

  setMessages((prev) =>
    prev.map((message) =>
      message.messageId === messageId ? { ...message, feedback: rating } : message,
    ),
  );

  submitFeedback(token, messageId, rating).catch((err) => {
    setError(err instanceof Error ? err.message : "Failed to submit feedback");
  });
}

  function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter") {
      handleSend();
    }
  }

  return (
    <div className="chat-window">
      <div className="chat-messages">
        {messages.map((message, index) => (
          <div key={index} className={`message-row ${message.role}`}>
            <div className="message-bubble">
              <span className="message-role">
                {message.role === "user" ? "You" : "Assistant"}
              </span>
              {message.content}
              {message.citations && message.citations.length > 0 && (
                <div className="citations">
                  {[...new Set(message.citations.map((c) => c.filename))].map((filename) => (
                    <span key={filename} className="citation-chip">
                      {filename}
                    </span>
                  ))}
                </div>
              )}
              {message.messageId !== undefined && (
                <div className="feedback-row">
                  <button
                    type="button"
                    aria-label="Good response"
                    aria-pressed={message.feedback === "UP"}
                    className={`feedback-btn ${message.feedback === "UP" ? "selected" : ""}`}
                    onClick={() => handleFeedback(message.messageId!, "UP")}
                  >
                    <ThumbIcon direction="up" />
                  </button>
                  <button
                    type="button"
                    aria-label="Bad response"
                    aria-pressed={message.feedback === "DOWN"}
                    className={`feedback-btn ${message.feedback === "DOWN" ? "selected" : ""}`}
                    onClick={() => handleFeedback(message.messageId!, "DOWN")}
                  >
                    <ThumbIcon direction="down" />
                  </button>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {error && <p className="error-text">{error}</p>}

      <div className="chat-input-row">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={isSending}
        />
        <button className="btn btn-primary" onClick={handleSend} disabled={isSending}>
          {isSending ? "Sending..." : "Send"}
        </button>
      </div>
    </div>
  );
}
