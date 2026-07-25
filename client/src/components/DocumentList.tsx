import { useEffect, useState } from "react";
import { getDocuments } from "../api/documents";
import type { DocumentSummary } from "../api/documents";
import { useAuth } from "../context/AuthContext";

interface DocumentListProps {
  refreshTrigger: number;
}

export default function DocumentList({ refreshTrigger }: DocumentListProps) {
  const { token } = useAuth();

  const [documents, setDocuments] = useState<DocumentSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) return;

    getDocuments(token)
      .then(setDocuments)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load documents"))
      .finally(() => setIsLoading(false));
  }, [token, refreshTrigger]); // re-fetch whenever the parent bumps refreshTrigger (e.g. after a new upload)

  return (
    <div>
      {isLoading && <p>Loading...</p>}
      {error && <p style={{ color: "red" }}>{error}</p>}

      <ul style={{ listStyle: "none", padding: 0 }}>
        {documents.map((document) => (
          <li key={document.id}>
            {document.filename} ({document.fileType}) -{" "}
            {new Date(document.uploadedAt).toLocaleString()}
          </li>
        ))}
      </ul>
    </div>
  );
}
