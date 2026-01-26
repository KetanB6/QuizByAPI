package com.ketan.QuizByAI.service;

import com.ketan.QuizByAI.exceptionHandler.AiLimitExceedException;
import com.ketan.QuizByAI.exceptionHandler.BadRequestException;
import com.ketan.QuizByAI.model.AiQuizRequestDTO;
import com.ketan.QuizByAI.model.Question;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizByAiService {

    private static final Logger log = LoggerFactory.getLogger(QuizByAiService.class);
    private final ChatClient chatClient;

    public QuizByAiService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem(defaultSystemPrompt())
                .build();
    }

    @CircuitBreaker(name="aiService", fallbackMethod="handleAiException")
    public List<Question> generateQuestion(AiQuizRequestDTO dto) {

        if(validateTopic(dto.getTopic())) {
            throw new BadRequestException("Inappropriate Quiz topic!");
        }

        ParameterizedTypeReference<List<Question>> typeReference = new ParameterizedTypeReference<>() {};
        //Structures the response received from ai, if there is some mistake in response is request back to ai with error message
        StructuredOutputValidationAdvisor validationAdvisor = StructuredOutputValidationAdvisor.builder()
                .outputType(typeReference)
                .maxRepeatAttempts(3) // It will try to fix the JSON 3 times before failing
                .build();

        List<Question> questions = this.chatClient.prompt()
                .user(getPrompt(dto))
                .advisors(new SimpleLoggerAdvisor(), validationAdvisor) //logs request and response automatically and validate response
                .call()
                .entity(new ParameterizedTypeReference<List<Question>>() {}); //In simple terms: It creates a "Super Type Token" that preserves the specific generic type (List<Question>) at runtime, preventing Java from losing that information due to Type Erasure.


        if(questions.isEmpty()) {
            throw new BadRequestException("Quiz topic is invalid!");
        }

        return questions;
    }

    //If our AI fails to replay due to limit exceed this method will be executed
    public List<Question> handleAiException(AiQuizRequestDTO dto, Throwable t) {
        if (t instanceof BadRequestException) {
            throw (BadRequestException) t;
        }
        log.error(t.getMessage());
        throw new AiLimitExceedException("Our AI professor is taking a short break. Try again shortly!");
    }

    private String defaultSystemPrompt() {
        return  "You are an expert Professor. Generate unique quiz questions in strict JSON format. " +
                "Output ONLY a raw JSON array. No markdown. " +
                "CRITICAL: Single-line format. No literal newlines in strings. Use '\\n' for internal breaks. " +
                "CRITICAL: Escape double quotes with backslash (\\\") or use single quotes. RFC8259 compliant. " +
                "DIVERSITY RULE: Each question must be distinct. Options must be unique and plausible. DO NOT repeat the same distractors across different questions. " +
                "STOP! If the input topic is gibberish, random letters (like 'asdf'), or nonsensical, " +
                "TOPIC CHECK: If the topic is nonsensical, silly, violent, or illegal, return exactly: []. " +
                "STRUCTURE: Use qno (int), quizId (1), question (string), opt1, opt2, opt3, opt4, and correctOpt (exact string match). No null values.";
    }

    private String getPrompt(AiQuizRequestDTO dto) {
        String difficulty = dto.getDifficulty().equalsIgnoreCase("Combined")
                ? "a balanced mix of Easy, Moderate, and Difficult levels (roughly 1/3 each)"
                : dto.getDifficulty() + " level";

        return String.format(
                "Generate %d questions about '%s' in the %s language. " +
                        "The difficulty must be %s. " +
                        "Difficulty Guidelines: " +
                        "- Easy: Basic facts and common knowledge. " +
                        "- Moderate: Requires conceptual understanding. " +
                        "- Difficult: Requires deep analysis or specific data. " +
                        "CRITICAL: Every single field (question, opt1, opt2, opt3, opt4, correctOpt) " +
                        "MUST be written in the %s language only.",
                dto.getCount(), dto.getTopic(), dto.getLanguage(), difficulty, dto.getLanguage()
        );
    }

    public boolean validateTopic(String topic) {
        List<String> forbiddenTerms = List.of(
                // 1. Profanity & Slurs
                "fuck", "shit", "bitch", "asshole", "dick", "pussy", "nigger", "faggot",

                // 2. Violence & Harm
                "violence", "murder", "terrorism", "bomb", "suicide", "torture", "rape", "war crimes", "genocide",

                // 3. Illegal Activities
                "illegal", "drugs", "cocaine", "heroin", "meth", "theft", "hacking", "fraud", "scam",

                // 4. Explicit/Adult Content
                "explicit", "porn", "erotic", "nudity", "hentai", "xxx",

                // 5. Sensitive/Divisive Topics (Optional but recommended for schools)
                "racism", "hate speech", "extremism", "white supremacy", "nazism"
        );

        if(topic == null || topic.isBlank())
            return false;

        if (topic.matches(".*[bcdfghjklmnpqrstvwxyz]{5,}.*")) {
            log.warn("Gibberish detected: {}", topic);
            return true;
        }

        return forbiddenTerms.stream().anyMatch(topic.toLowerCase()::contains);
    }
}
