# Employee Feedback Triage System

## Overview
OmniCorp Solutions provides enterprise portal software for large organizations. Currently, HR Managers at our client companies manually read thousands of open-ended employee feedback submissions, summarize them, and manually route them to relevant departments (Facilities, IT, Management). This manual triage is slow and error-prone, leading to delayed responses to critical workplace issues. You have been tasked with building a prototype of an automated Feedback Triage System to solve this.

The system must consist of a frontend UI and a backend composed of two small interacting Java services. The frontend will allow employees to submit feedback and HR to view processed results. The backend should consist of a 'Submission API' service that receives the data, and an 'AI Processing' service that handles the business logic. The AI Processing service should integrate with an LLM (using a free-tier API like OpenAI/Anthropic, or a simulated mock if you prefer) to summarize the text and categorize it (e.g., IT, Facilities, HR).

Focus on clean foundational Java code, basic but reliable communication between the two backend services, and a functional frontend integration. The UI does not need to be polished, just capable of demonstrating the end-to-end workflow. Keep the scope manageable; we are looking for solid execution of a distributed concept, not a production-ready enterprise deployment.

## Deliverables
- One public GitHub repository OR a GitHub gist URL containing your complete solution.
- Java Backend: Source code for two interacting services (e.g., Submission Service and AI Processing Service).
- Frontend UI: A basic web interface (React, Next.js, or plain HTML/CSS/JS) that interacts with the backend to submit and view feedback.
- A README.md explaining your design decisions, trade-offs, and clear instructions on how to build, run, and test the multi-service system.
- AI conversation logs (e.g., VS Code GitHub Copilot Chat export, Cursor chat history, or Claude conversation export) committed to the repository, alongside a brief note in the README describing which AI tools were used and where.

## Suggested tools / libraries
- Java 17+
- Spring Boot
- Maven or Gradle
- React + Vite, or plain HTML/CSS/JS with Fetch API
- Docker & Docker Compose (optional but recommended for running multiple services)
- OpenAI API (free tier) or a local mock service

## On AI assistants & follow-up
- We expect you to use AI assistants; you must commit your conversation log (e.g., Copilot, Cursor, or Claude export) to the repository and note your usage in the README.
- Focus on understanding every line of code you submit, regardless of who or what wrote it.
- Be ready to walk us through your code and explain the communication between your services during the technical interview.
- We will ask deep-dive questions on your design choices, prompt engineering, and trade-offs in the follow-up round.

## How to submit
Reply to this email with **one** public GitHub repository or gist URL containing your solution.

<!-- AI-EVALUATOR:PRESCREEN-META {"language":"Java","dueDays":10,"timeBudgetHours":6} -->