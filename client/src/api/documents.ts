// API layer for the document upload endpoint.
// Talks to the Spring Boot backend running at BASE_URL.

// TODO: make this configurable via an environment variable (e.g. import.meta.env.VITE_API_BASE_URL)
const BASE_URL = "http://localhost:8080";

// Body returned by POST /api/documents/upload on success (200).
export interface DocumentSummary {
  id: number;
  filename: string;
  fileType: string;
  uploadedAt: string;
}

// Uploads a document, authenticated with the given bearer token. Resolves
// with the stored document's summary on success (200), or throws an Error
// (e.g. "Unsupported file type", "Failed to extract text") on failure (400).
//
// We send this as multipart/form-data via FormData rather than JSON: we're
// deliberately NOT setting a Content-Type header ourselves - see
// DocumentUpload.tsx for why.
export async function uploadDocument(token: string, file: File): Promise<DocumentSummary> {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(`${BASE_URL}/api/documents/upload`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData,
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<DocumentSummary>;
}

// Lists the current user's uploaded documents. Resolves with an array of
// document summaries on success (200).
export async function getDocuments(token: string): Promise<DocumentSummary[]> {
  const response = await fetch(`${BASE_URL}/api/documents`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<DocumentSummary[]>;
}
