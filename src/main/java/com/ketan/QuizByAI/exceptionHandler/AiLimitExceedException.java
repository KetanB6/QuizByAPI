package com.ketan.QuizByAI.exceptionHandler;

public class AiLimitExceedException extends RuntimeException {
    public AiLimitExceedException(String message) {
        super(message);
    }
}
