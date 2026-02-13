# Quiz by API (QuizByAiService)

**Quiz by API** is a Spring Boot microservice that dynamically generates quizzes using AI and real-time trivia data. It powers the **AI Quiz** and **Global Topics** features of the QuizKrida platform.

This service is focused purely on quiz generation. It does **not** store users, results, or sessions — it returns ready-to-play questions on demand.

> Integrated frontend: https://quizkrida.vercel.app/
> Base API URL: `/api/v1`

---

## Overview

Quiz by API acts as an independent quiz engine inside the QuizKrida ecosystem. It supports:

* AI-generated quizzes on any custom topic
* Difficulty and language selection
* Global trivia topic quizzes
* Real-time question fetching
* Stateless quiz delivery

The service is designed to be lightweight, scalable, and easily integrable with multiple frontends.

---

## Tech Stack

* Java
* Spring Boot
* RESTful API architecture
* AI question generation service
* **Open Trivia DB API integration**

---

## Architecture Role

This service functions as a dedicated quiz generation microservice:

```
Frontend → Quiz by API → AI / Trivia Providers
```

It is independent from the main QuizKrida backend and focuses exclusively on generating question sets.

---

## Trivia Database Integration

This service integrates with **Open Trivia DB**, a public trivia question API, to provide curated global quiz topics.

Trivia quizzes are fetched in real time and returned as standardized `Question` objects. These quizzes are stateless and are not stored in the backend.

Trivia DB enables:

* Category-based quiz selection
* Randomized question sets
* Global knowledge topics
* Instant quiz generation without database storage

---

## API Endpoints

All endpoints are prefixed with:

```
/api/v1
```

---

### API Info

**GET `/`**

Returns a message pointing to the official frontend.

Response:

```
"You are on API URL. Please visit official site: https://myquizapp-psi.vercel.app/"
```

---

### Health Check

**GET `/Health`**

Checks if the service is running.

Response: `200 OK`

---

## AI Quiz Generation

### Generate Quiz

**POST `/Generate`**

Generates quiz questions using AI.

Request Body: `AiQuizRequestDTO`

Typical request fields:

* topic
* difficulty level
* language
* number of questions

Response:

```
List<Question>
```

This endpoint powers the **Quiz by AI** feature.

---

## Global Trivia Topics

### List Available Topics

**GET `/Topics`**

Returns available trivia categories from Open Trivia DB.

Response:

```
Map<Integer, String>
```

Where:

* key = topic ID
* value = topic name

---

### Fetch Trivia Questions

**GET `/Live/{topicId}`**

Returns trivia questions for the selected topic.

Response:

```
List<Question>
```

This endpoint powers the **Global Topics** quiz mode.

---

## Project Structure

```
src/
 ├── controller/   REST API controllers
 ├── service/      AI and trivia integration logic
 ├── model/        DTOs and question models
 └── config/       Application configuration
```

Architecture flow:

```
Controller → Service → External Providers
```

---

## Running the Project

### Prerequisites

* Java 17+
* Maven
* Internet access (for AI and trivia providers)

### Steps

```
git clone https://github.com/KetanB6/QuizByAiService
cd QuizByAiService
mvn spring-boot:run
```

Server runs on:

```
http://localhost:8080/api/v1
```

---

## Environment Configuration

Configure external service credentials in:

```
src/main/resources/application.properties
```

This may include:

* AI API keys
* Request limits
* Timeout settings
* Server port

---

## Use Cases

This microservice supports:

* Instant AI quiz generation
* Educational practice sessions
* Global knowledge quizzes
* Multi-language quiz experiences

---

## Roadmap

Planned improvements:

* Response caching
* Rate limiting
* Authentication layer
* Performance optimization
* Expanded topic coverage

---

## Author

**Ketan Bidave**

Part of the QuizKrida ecosystem — enabling AI-powered interactive quizzes.
