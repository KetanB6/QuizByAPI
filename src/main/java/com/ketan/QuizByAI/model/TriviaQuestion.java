package com.ketan.QuizByAI.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TriviaQuestion {
    private String category;
    private String type;
    private String difficulty;
    private String question;

    @JsonProperty("correct_answer")
    private String correctOption;

    @JsonProperty("incorrect_answers")
    private List<String> incorrect_options;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    public List<String> getIncorrect_options() {
        return incorrect_options;
    }

    public void setIncorrect_options(List<String> incorrect_options) {
        this.incorrect_options = incorrect_options;
    }
}
