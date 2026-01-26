# QuizByAI Service

An independent AI-powered quiz generation backend service designed as a stateless microservice.

This service was originally part of the main **Online Quiz System** but was later separated into its own standalone service to improve:
- architecture clarity
- cost efficiency
- scalability
- maintainability
- system stability

The service focuses only on AI-based quiz generation and does not contain any database or persistence layer.

---

## Purpose

The purpose of this service is to provide AI-generated quizzes based on:
- Topic  
- Difficulty level   
- Language  

It acts as a pure generation service, not a storage system.

---

## Service Evolution

### Initial Design
Originally implemented as **Module 3** inside the main Online Quiz System:
1. Create Quiz  
2. Play Quiz  
3. AI-based Quiz Generation  

### Refactored Design
The AI module was extracted into an independent service due to:
- No requirement for persistent storage  
- External AI dependency isolation  
- API cost control  
- Independent scaling needs  
- Cleaner system architecture  

This resulted in a dedicated AI microservice:

**quizbyai-service**

---

## Architecture

### Service Characteristics
- Stateless backend service  
- No database dependency  
- External AI API integration  
- REST-based API design  
- Service isolation from core system  

---

## System Positioning

```text
Frontend
   ├── online-quiz-system  → Core quiz logic (DB-based)
   └── quizbyai-service    → AI quiz generation (Stateless service)
