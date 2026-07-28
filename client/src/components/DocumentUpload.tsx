import { useState } from "react";
import type { ChangeEvent } from "react";
import { uploadDocument } from "../api/documents";
import type { DocumentSummary } from "../api/documents";
import { useAuth } from "../context/AuthContext";

interface DocumentUploadProps {
  onUploadSuccess: () => void;
}

export default function DocumentUpload({ onUploadSuccess }: DocumentUploadProps) {
  const { token } = useAuth();

  const [isUploading, setIsUploading] = useState(false);
  const [uploaded, setUploaded] = useState<DocumentSummary | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function handleFileChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    e.target.value = ""; // allow re-selecting the same file later
    if (!file || !token) return;

    setError(null);
    setUploaded(null);
    setIsUploading(true);

    try {
      const summary = await uploadDocument(token, file);
      setUploaded(summary);
      onUploadSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Upload failed");
    } finally {
      setIsUploading(false);
    }
  }

  return (
    <div>
      <h2>Documents</h2>
      <input
        type="file"
        accept=".pdf,.docx,.txt"
        onChange={handleFileChange}
        disabled={isUploading}
      />

      {isUploading && <p className="muted-text">Uploading...</p>}
      {uploaded && <p className="muted-text">Uploaded {uploaded.filename}</p>}
      {error && <p className="error-text">{error}</p>}
    </div>
  );
}
