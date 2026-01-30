package com.ketan.QuizByAI.service;

import com.ketan.QuizByAI.exceptionHandler.AiLimitExceedException;
import com.ketan.QuizByAI.exceptionHandler.BadRequestException;
import com.ketan.QuizByAI.model.AiQuizRequestDTO;
import com.ketan.QuizByAI.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class QuizByAiService {

    private static final Logger log = LoggerFactory.getLogger(QuizByAiService.class);
    private final ChatClient chatClient;

    public QuizByAiService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem(defaultSystemPrompt())
                .build();
    }

    public List<Question> generateQuestion(AiQuizRequestDTO dto) {

        if(validateTopic(dto.getTopic())) {
            throw new BadRequestException("Inappropriate Quiz topic!");
        }

        //Structures the response received from ai, if there is some mistake in response is request back to ai with error message
        ParameterizedTypeReference<List<Question>> typeReference = new ParameterizedTypeReference<>() {};
        StructuredOutputValidationAdvisor validationAdvisor = StructuredOutputValidationAdvisor.builder()
                .outputType(typeReference)
                .maxRepeatAttempts(3) // It will try to fix the JSON 3 times before failing
                .build();

        List<Question> questions = null;

        try {
            questions = this.chatClient.prompt()
                    .user(getPrompt(dto))
                    .advisors(new SimpleLoggerAdvisor(), validationAdvisor) //logs request and response automatically and validate response
                    .call()
                    .entity(new ParameterizedTypeReference<List<Question>>() {}); //In simple terms: It creates a "Super Type Token" that preserves the specific generic type (List<Question>) at runtime, preventing Java from losing that information due to Type Erasure.
        } catch (Exception e) {
            String errorMsg = e.getMessage();

            // 1. Handle Daily Quota
            if (errorMsg.contains("quota") || errorMsg.contains("insufficient_quota") || errorMsg.contains("billing")) {
                log.error("Daily/Billing Quota reached for Groq API!");
                throw new AiLimitExceedException("Our AI Guruji has reached his daily limit. Please come back tomorrow!");
            }

            // 2. Handle TPM/RPM
            if (errorMsg.contains("429") || errorMsg.contains("rate limit") || errorMsg.contains("tpm")) {
                log.error("TPM/RPM Limit exceeded for topic: {}", dto.getTopic());
                throw new AiLimitExceedException("Too many requests for Guruji to handle! Please wait for 60 seconds before trying again.");
            }

            // 3. other errors
            log.error("Unexpected AI Error: {}", e.getMessage());
            throw new AiLimitExceedException("Guruji is having some trouble. Try again in a few minutes.");
        }

        if(questions.isEmpty()) {
            throw new BadRequestException("Quiz topic is invalid!");
        }

        return questions;
    }


    private String defaultSystemPrompt() {
        return  "You are an expert Professor. Generate unique quiz questions in strict JSON format. " +
                "Output ONLY a raw JSON array. No markdown. Generate quiz questions in STRICT valid JSON format. " +
                "CRITICAL: Single-line format. No literal newlines in strings. Use '\\n' for internal breaks. " +
                "CRITICAL: Escape double quotes with backslash (\\\") or use single quotes. RFC8259 compliant. " +
                "DIVERSITY RULE: Each question must be distinct. Options must be unique and plausible. DO NOT repeat the same distractors across different questions. " +
                "STOP! If the input topic is gibberish, random letters (like 'asdf'), or nonsensical, " +
                "TOPIC CHECK: If the topic is nonsensical, silly, violent, or illegal, return exactly: []. " +
                "STRUCTURE: Use qno (int), quizId (1), question (string), opt1, opt2, opt3, opt4, and correctOpt (exact string match). No null values.";
    }

    private String getPrompt(AiQuizRequestDTO dto) {
        int randomSeed = new Random().nextInt(10000);

        return String.format(
                "Request Reference: #%d. " +
                "Generate %d unique questions about '%s' in the %s language. " +
                "The difficulty must be strictly %s level. " +
                "VARIETY RULES: " +
                "1. Do not repeat common or basic questions. " +
                "2. Explore various sub-topics, niche facts, and diverse applications within the main topic. " +
                "3. Ensure each question is conceptually distinct from the others. " +
                "CRITICAL: Every single field (question, opt1, opt2, opt3, opt4, correctOpt) " +
                "MUST be written in the %s language only.",
                randomSeed, dto.getCount(), dto.getTopic(), dto.getLanguage(),
                dto.getDifficulty(), dto.getLanguage()
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
