import { useAuth } from './context/AuthContext';
import LoginForm from './components/LoginForm';
import SignupForm from './components/SignupForm';
import ChatWindow from './components/ChatWindow';
import type { ChatMessage } from './components/ChatWindow';
import Sidebar from './components/Sidebar';
import DocumentUpload from './components/DocumentUpload';
import DocumentList from './components/DocumentList';
import { useState } from 'react';

function App() {
  const { token, logout, isLoading } = useAuth();
  const [showSignup, setShowSignup] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [conversationId, setConversationId] = useState<number | null>(null);
  const [documentRefreshTrigger, setDocumentRefreshTrigger] = useState(0);

  if (isLoading) return <p>Loading...</p>;

  if (!token) {
    return (
      <div>
        {showSignup ? <SignupForm /> : <LoginForm />}
        <button onClick={() => setShowSignup(!showSignup)}>
          {showSignup ? "Already have an account? Log in" : "Don't have an account? Sign up"}
        </button>
      </div>
    );
  }

  return (
    <div style={{ display: "flex" }}>
      <Sidebar
        setMessages={setMessages}
        setConversationId={setConversationId}
        activeConversationId={conversationId}
      />
      <div>
        <button onClick={logout}>Log out</button>
        <DocumentUpload onUploadSuccess={() => setDocumentRefreshTrigger((prev) => prev + 1)} />
        <DocumentList refreshTrigger={documentRefreshTrigger} />
        <ChatWindow
          messages={messages}
          conversationId={conversationId}
          setMessages={setMessages}
          setConversationId={setConversationId}
        />
      </div>
    </div>
  );
}

export default App;