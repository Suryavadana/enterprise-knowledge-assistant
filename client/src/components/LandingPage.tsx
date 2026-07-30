import { useEffect, useRef, useState } from "react";
import type { ReactNode } from "react";

interface LandingPageProps {
  onGetStarted: () => void;
}

// Wraps a section so it fades/slides in the first time it scrolls into view,
// then disconnects its observer - the reveal only needs to happen once.
function Reveal({ children, className = "" }: { children: ReactNode; className?: string }) {
  const ref = useRef<HTMLDivElement>(null);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
          observer.disconnect();
        }
      },
      { threshold: 0.15 },
    );
    observer.observe(el);

    return () => observer.disconnect();
  }, []);

  return (
    <div ref={ref} className={`reveal ${isVisible ? "reveal-visible" : ""} ${className}`}>
      {children}
    </div>
  );
}

function MessageCircleIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
    </svg>
  );
}

function FileTextIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
      <polyline points="14 2 14 8 20 8" />
      <line x1="16" y1="13" x2="8" y2="13" />
      <line x1="16" y1="17" x2="8" y2="17" />
      <polyline points="10 9 9 9 8 9" />
    </svg>
  );
}

function CheckCircleIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
      <polyline points="22 4 12 14.01 9 11.01" />
    </svg>
  );
}

function LayersIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polygon points="12 2 2 7 12 12 22 7 12 2" />
      <polyline points="2 17 12 22 22 17" />
      <polyline points="2 12 12 17 22 12" />
    </svg>
  );
}

const FEATURES = [
  {
    icon: MessageCircleIcon,
    title: "Chat naturally",
    description: "Ask questions the way you'd ask a colleague - no special syntax or query language to learn.",
  },
  {
    icon: FileTextIcon,
    title: "Grounded in your documents",
    description: "Answers are drawn from the PDFs, Word docs, and text files your team has already uploaded.",
  },
  {
    icon: CheckCircleIcon,
    title: "Every answer is cited",
    description: "Each response links back to the exact source document, so you can verify it in seconds.",
  },
  {
    icon: LayersIcon,
    title: "Reusable prompt templates",
    description: "Save your best questions as templates and reuse them across conversations.",
  },
];

const STEPS = [
  {
    title: "Upload your documents",
    description: "PDFs, Word docs, or text files - drag them in and they're indexed automatically.",
  },
  {
    title: "Ask anything",
    description: "Type natural language questions, no special syntax required.",
  },
  {
    title: "Get grounded answers",
    description: "Every response cites its source, so you always know where the answer came from.",
  },
];

export default function LandingPage({ onGetStarted }: LandingPageProps) {
  return (
    <div className="landing">
      <section className="landing-hero">
        <p className="landing-eyebrow">AI-Powered Document Intelligence</p>
        <h1 className="landing-title">Enterprise Knowledge Assistant</h1>
        <p className="landing-tagline">
          Ask questions about your documents in plain English. Get answers with verifiable
          sources - every time.
        </p>
        <button type="button" className="landing-cta-btn" onClick={onGetStarted}>
          Get Started
        </button>
        <p className="landing-subtext">No credit card required · Free to try</p>
      </section>

      <Reveal className="landing-preview-wrap">
        <div className="landing-preview">
          <div className="message-row user">
            <div className="message-bubble">
              <span className="message-role">You</span>
              What's our policy on remote work?
            </div>
          </div>
          <div className="message-row assistant">
            <div className="message-bubble">
              <span className="message-role">Assistant</span>
              Employees may work remotely up to three days per week, subject to manager
              approval. Full-remote arrangements require VP sign-off and are reviewed quarterly.
              <div className="citations">
                <span className="citation-chip">employee-handbook.pdf</span>
              </div>
            </div>
          </div>
        </div>
      </Reveal>

      <Reveal>
        <section className="landing-steps-section">
          <h2 className="landing-section-title">How it works</h2>
          <ol className="landing-steps">
            {STEPS.map((step, index) => (
              <li key={step.title} className="landing-step">
                <span className="landing-step-number">{index + 1}</span>
                <h3 className="landing-step-title">{step.title}</h3>
                <p className="landing-step-description">{step.description}</p>
              </li>
            ))}
          </ol>
        </section>
      </Reveal>

      <Reveal>
        <section className="landing-features-section">
          <h2 className="landing-section-title">Everything you need to find answers fast</h2>
          <div className="landing-features">
            {FEATURES.map((feature) => (
              <div key={feature.title} className="card landing-feature-card">
                <div className="landing-feature-icon">
                  <feature.icon />
                </div>
                <h3 className="landing-feature-title">{feature.title}</h3>
                <p className="landing-feature-description">{feature.description}</p>
              </div>
            ))}
          </div>
        </section>
      </Reveal>

      <Reveal>
        <section className="landing-closing-cta">
          <h2 className="landing-section-title">Ready to try it yourself?</h2>
          <button type="button" className="landing-cta-btn" onClick={onGetStarted}>
            Get Started
          </button>
        </section>
      </Reveal>

      <footer className="landing-footer">
        <p className="muted-text">Built with React, Spring Boot, and Gemini</p>
        <p className="muted-text landing-footer-credit">
          Built by Surya Vadana ·{" "}
          <a href="https://github.com/Suryavadana" target="_blank" rel="noopener noreferrer">
            github.com/Suryavadana
          </a>
        </p>
      </footer>
    </div>
  );
}
