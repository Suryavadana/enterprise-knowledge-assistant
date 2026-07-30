import { useAuth } from './context/AuthContext';
import LoginForm from './components/LoginForm';
import SignupForm from './components/SignupForm';
import ChatWindow from './components/ChatWindow';
import type { ChatMessage } from './components/ChatWindow';
import Sidebar from './components/Sidebar';
import DocumentUpload from './components/DocumentUpload';
import DocumentList from './components/DocumentList';
import LandingPage from './components/LandingPage';
import { useState } from 'react';

// Simple hamburger icon, only shown below 768px (see .sidebar-toggle in
// index.css) to open the sidebar drawer.
function MenuIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <line x1="3" y1="6" x2="21" y2="6" />
      <line x1="3" y1="12" x2="21" y2="12" />
      <line x1="3" y1="18" x2="21" y2="18" />
    </svg>
  );
}

function App() {
  const { token, logout, isLoading } = useAuth();
  const [showLanding, setShowLanding] = useState(true);
  const [showSignup, setShowSignup] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [conversationId, setConversationId] = useState<number | null>(null);
  const [documentRefreshTrigger, setDocumentRefreshTrigger] = useState(0);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  if (showLanding) {
    return <LandingPage onGetStarted={() => setShowLanding(false)} />;
  }

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
      <div
        className={`sidebar-backdrop ${isSidebarOpen ? "open" : ""}`}
        onClick={() => setIsSidebarOpen(false)}
      />
      <Sidebar
        setMessages={setMessages}
        setConversationId={setConversationId}
        activeConversationId={conversationId}
        isOpen={isSidebarOpen}
        onClose={() => setIsSidebarOpen(false)}
      />
      <div className="main-content">
        <div className="topbar">
          <button
            type="button"
            className="btn sidebar-toggle"
            aria-label="Toggle menu"
            onClick={() => setIsSidebarOpen((prev) => !prev)}
          >
            <MenuIcon />
          </button>
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