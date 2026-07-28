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

  if (isLoading) return <p className="muted-text">Loading...</p>;

  if (!token) {
    return (
      <div className="auth-page">
        {showSignup ? <SignupForm /> : <LoginForm />}
        <button className="btn-text" onClick={() => setShowSignup(!showSignup)}>
          {showSignup ? "Already have an account? Log in" : "Don't have an account? Sign up"}
        </button>
      </div>
    );
  }

  return (
    <div className="app-shell">
      <Sidebar
        setMessages={setMessages}
        setConversationId={setConversationId}
        activeConversationId={conversationId}
      />
      <div className="main-content">
        <div className="topbar">
          <button className="btn" onClick={logout}>Log out</button>
        </div>
        <div className="card">
          <DocumentUpload onUploadSuccess={() => setDocumentRefreshTrigger((prev) => prev + 1)} />
          <DocumentList refreshTrigger={documentRefreshTrigger} />
        </div>
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