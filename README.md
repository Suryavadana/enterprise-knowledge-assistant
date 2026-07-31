# 🧠 Enterprise Knowledge Assistant

**A full-stack AI application that answers questions from your own documents — with citations and a confidence score.**

[![Live Demo](https://img.shields.io/badge/Live_Demo-Visit_App-4a6fa5?style=for-the-badge)](https://Suryavadana.github.io/enterprise-knowledge-assistant/)
[![CI](https://img.shields.io/github/actions/workflow/status/Suryavadana/enterprise-knowledge-assistant/ci.yml?branch=main&style=flat-square&label=CI)](https://github.com/Suryavadana/enterprise-knowledge-assistant/actions)
![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)
![React](https://img.shields.io/badge/React-TypeScript-61DAFB?style=flat-square)
![License](https://img.shields.io/badge/license-MIT-lightgrey?style=flat-square)

**🔗 [Try it live](https://Suryavadana.github.io/enterprise-knowledge-assistant/)**
*(hosted on free-tier infrastructure — first request may take 30–50s to wake up)*

## Overview

Upload a PDF, Word document, or text file, then ask questions about it in plain English. Instead of a generic chatbot guessing from general knowledge, this app:

1. Extracts and chunks your document's text
2. Converts each chunk into a vector embedding (Google Gemini)
3. Stores embeddings in a vector database (ChromaDB)
4. Retrieves the most semantically relevant chunks for your question — not just keyword matches
5. Feeds that context to the LLM so it answers **from your documents**
6. Shows exactly which document and passage backed the answer
7. Scores how well-grounded the answer actually is, so you know when to trust it

## Features

| Feature | Description |
|---|---|
| 🔐 **JWT Authentication** | Secure signup/login, protected routes |
| 💬 **AI Chat** | Persistent conversation history |
| 📄 **Document Upload** | PDF, DOCX, and TXT with automatic text extraction |
| 🔍 **RAG Pipeline** | Chunking, embeddings, and vector search — built from scratch, no LangChain |
| 📎 **Source Citations** | Every answer links to the exact document and passage it came from |
| 🎯 **Grounding Confidence** | HIGH / MEDIUM / LOW badge showing how well an answer is supported |
| 👍 **Feedback** | Thumbs up/down on any response |
| 📋 **Prompt Templates** | Save and reuse common prompts |
| 📱 **Responsive Design** | Mobile-friendly with a collapsible sidebar |
| 🔄 **Self-Healing** | Automatically recovers if the vector store loses data on ephemeral hosting |

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React · TypeScript · Vite |
| Backend | Spring Boot 4 · Java 21 |
| Database | PostgreSQL |
| Vector Store | ChromaDB |
| AI | Google Gemini API (chat + embeddings) |
| Auth | JWT · Spring Security · BCrypt |
| DevOps | Docker (multi-stage) · Docker Compose · GitHub Actions |
| Hosting | GitHub Pages · Render · Neon *(all free tier)* |

## Architecture

```
Browser
   │
   ▼
React Frontend  (GitHub Pages)
   │  REST API + JWT
   ▼
Spring Boot Backend  (Render) ────► PostgreSQL  (Neon)
   │
   ├──► Google Gemini API   (chat + embeddings)
   └──► ChromaDB  (Render)  — vector similarity search
```

## Running Locally

**Prerequisites:** Java 21 · Maven · Node.js 20+ · PostgreSQL · Python 3 · a [Gemini API key](https://aistudio.google.com)

```bash
git clone https://github.com/Suryavadana/enterprise-knowledge-assistant.git
cd enterprise-knowledge-assistant
cp server/server/src/main/resources/application.properties.example server/server/src/main/resources/application.properties
# fill in your own values in application.properties
```

**Start ChromaDB:**
```bash
pip install chromadb
chroma run --path ./chroma-data --port 8000 --host 0.0.0.0
```

**Start the backend:**
```bash
cd server/server && mvn spring-boot:run
```

**Start the frontend:**
```bash
cd client && npm install && npm run dev
```

Visit `http://localhost:5173`.

**Or run everything with Docker Compose:**
```bash
cp .env.example .env   # fill in your secrets
docker-compose up
```

## Testing

```bash
cd server/server && mvn test
```

## CI/CD

Every push runs an automated pipeline that builds and tests the backend, builds the frontend, and verifies both Docker images build correctly. The frontend auto-deploys to GitHub Pages on every push to `main`.

## Project Structure

```
enterprise-knowledge-assistant/
├── client/              React + TypeScript frontend
├── server/server/       Spring Boot backend
├── docker-compose.yml   Local multi-service orchestration
└── .github/workflows/   CI and deployment pipelines
```

---



**Built by [Surya Vadana](https://github.com/Suryavadana)**

*A learning project built to deeply understand full-stack architecture and retrieval-augmented generation — implementing the RAG pipeline from first principles rather than relying on a higher-level framework.*
