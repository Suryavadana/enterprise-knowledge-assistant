import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { createPromptTemplate, deletePromptTemplate, getPromptTemplates } from "../api/promptTemplates";
import type { PromptTemplate } from "../api/promptTemplates";
import { useAuth } from "../context/AuthContext";

interface PromptTemplatesProps {
  onSelectTemplate: (content: string) => void;
}

export default function PromptTemplates({ onSelectTemplate }: PromptTemplatesProps) {
  const { token } = useAuth();

  const [templates, setTemplates] = useState<PromptTemplate[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isOpen, setIsOpen] = useState(false);
  const [isNewFormOpen, setIsNewFormOpen] = useState(false);

  const [newTitle, setNewTitle] = useState("");
  const [newContent, setNewContent] = useState("");
  const [isCreating, setIsCreating] = useState(false);

  useEffect(() => {
    if (!token) return;

    getPromptTemplates(token)
      .then(setTemplates)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load templates"))
      .finally(() => setIsLoading(false));
  }, [token]);

  async function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!token) return;

    setError(null);
    setIsCreating(true);

    try {
      const template = await createPromptTemplate(token, newTitle, newContent);
      setTemplates((prev) => [...prev, template]);
      setNewTitle("");
      setNewContent("");
      setIsNewFormOpen(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create template");
    } finally {
      setIsCreating(false);
    }
  }

  function handleDelete(id: number) {
    if (!token) return;

    setTemplates((prev) => prev.filter((template) => template.id !== id));

    deletePromptTemplate(token, id).catch((err) => {
      setError(err instanceof Error ? err.message : "Failed to delete template");
    });
  }

  return (
    <div className="card templates-panel">
      <button type="button" className="btn" onClick={() => setIsOpen((v) => !v)}>
        Templates {isOpen ? "▲" : "▼"}
      </button>

      {isOpen && (
        <div className="templates-body">
          {isLoading && <p className="muted-text">Loading...</p>}
          {error && <p className="error-text">{error}</p>}

          {templates.length > 0 && (
            <ul className="template-list">
              {templates.map((template) => (
                <li key={template.id} className="template-item">
                  <button
                    type="button"
                    className="template-title"
                    onClick={() => onSelectTemplate(template.content)}
                  >
                    {template.title}
                  </button>
                  <button
                    type="button"
                    className="template-delete"
                    aria-label={`Delete ${template.title}`}
                    onClick={() => handleDelete(template.id)}
                  >
                    ×
                  </button>
                </li>
              ))}
            </ul>
          )}

          <button type="button" className="btn-text" onClick={() => setIsNewFormOpen((v) => !v)}>
            + New template
          </button>

          {isNewFormOpen && (
            <form onSubmit={handleCreate}>
              <div className="form-field">
                <label htmlFor="template-title">Title</label>
                <input
                  id="template-title"
                  type="text"
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  required
                />
              </div>

              <div className="form-field">
                <label htmlFor="template-content">Content</label>
                <textarea
                  id="template-content"
                  value={newContent}
                  onChange={(e) => setNewContent(e.target.value)}
                  rows={3}
                  required
                />
              </div>

              <button type="submit" className="btn btn-primary" disabled={isCreating}>
                {isCreating ? "Adding..." : "Add template"}
              </button>
            </form>
          )}
        </div>
      )}
    </div>
  );
}
