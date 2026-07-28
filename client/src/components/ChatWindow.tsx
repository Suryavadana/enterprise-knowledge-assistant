import { useState } from "react";
import type { Dispatch, KeyboardEvent, SetStateAction } from "react";
import { sendMessage } from "../api/chat";
import type { Citation } from "../api/chat";
import { useAuth } from "../context/AuthContext";

export interface ChatMessage {
  role: "user" | "assistant";
  content: string;
  citations?: Citation[];
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
        { role: "assistant", content: response.reply, citations: response.citations },
      ]);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to send message");
    } finally {
      setIsSending(false);
    }
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
