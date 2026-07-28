import { useEffect, useState } from "react";
import type { Dispatch, SetStateAction } from "react";
import { getConversations, getMessages } from "../api/conversations";
import type { ConversationSummary } from "../api/conversations";
import type { ChatMessage } from "./ChatWindow";
import { useAuth } from "../context/AuthContext";

interface SidebarProps {
  setMessages: Dispatch<SetStateAction<ChatMessage[]>>;
  setConversationId: Dispatch<SetStateAction<number | null>>;
  activeConversationId: number | null;
}

export default function Sidebar({
  setMessages,
  setConversationId,
  activeConversationId,
}: SidebarProps) {
  const { token } = useAuth();

  const [conversations, setConversations] = useState<ConversationSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) return;

    getConversations(token)
      .then(setConversations)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load conversations"))
      .finally(() => setIsLoading(false));
  }, [token, activeConversationId]); // re-fetch whenever the active conversation changes

  async function handleSelect(conversation: ConversationSummary) {
    if (!token) return;

    setError(null);

    try {
      const messages = await getMessages(token, conversation.id);

      // The backend's MessageDto uses uppercase "USER" / "ASSISTANT" for
      // role (matching the Java enum), but ChatWindow's ChatMessage expects
      // lowercase "user" / "assistant". This map bridges that mismatch -
      // without it, ChatWindow's role === "user" checks would silently
      // fail to match and every message would render as an assistant
      // message.
      const mapped: ChatMessage[] = messages.map((message) => ({
        role: message.role === "USER" ? "user" : "assistant",
        content: message.content,
      }));

      setMessages(mapped);
      setConversationId(conversation.id);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load conversation");
    }
  }

  function handleNewConversation() {
    setMessages([]);
    setConversationId(null);
  }

  return (
    <div className="sidebar">
      <button className="btn" onClick={handleNewConversation}>+ New conversation</button>

      {isLoading && <p className="muted-text">Loading...</p>}
      {error && <p className="error-text">{error}</p>}

      <ul className="sidebar-list">
        {conversations.map((conversation) => (
          <li
            key={conversation.id}
            className={conversation.id === activeConversationId ? "sidebar-item active" : "sidebar-item"}
          >
            <a
              href="#"
              onClick={(e) => {
                e.preventDefault();
                handleSelect(conversation);
              }}
            >
              Conversation {conversation.id} - {new Date(conversation.createdAt).toLocaleString()}
            </a>
          </li>
        ))}
      </ul>
    </div>
  );
}
