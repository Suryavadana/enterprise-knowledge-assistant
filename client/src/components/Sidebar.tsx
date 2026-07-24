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
    <div>
      <button onClick={handleNewConversation}>+ New conversation</button>

      {isLoading && <p>Loading...</p>}
      {error && <p style={{ color: "red" }}>{error}</p>}

      <ul style={{ listStyle: "none", padding: 0 }}>
        {conversations.map((conversation) => (
          <li key={conversation.id}>
            <a
              href="#"
              onClick={(e) => {
                e.preventDefault();
                handleSelect(conversation);
              }}
              style={{
                fontWeight: conversation.id === activeConversationId ? "bold" : "normal",
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
