package com.ketan.QuizByAI.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TriviaResponse {

    private int responseCode;
    @JsonProperty("results")
    private List<TriviaQuestion> questionList;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public List<TriviaQuestion> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<TriviaQuestion> questionList) {
        this.questionList = questionList;
    }
}
