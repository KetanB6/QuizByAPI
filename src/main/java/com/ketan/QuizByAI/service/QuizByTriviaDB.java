package com.ketan.QuizByAI.service;

import com.ketan.QuizByAI.exceptionHandler.BadRequestException;
import com.ketan.QuizByAI.model.AiQuizRequestDTO;
import com.ketan.QuizByAI.model.Question;
import com.ketan.QuizByAI.model.TriviaQuestion;
import com.ketan.QuizByAI.model.TriviaResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class QuizByTriviaDB {

    private static final Logger log = LoggerFactory.getLogger(QuizByTriviaDB.class);
    private final WebClient webClient;

    @Autowired
    public QuizByTriviaDB() {
        this.webClient = WebClient.builder()
                .baseUrl("https://opentdb.com")
                .build();
    }


    public List<Question> getQuestion(int topicId) {

        String uri = "/api.php?amount=10&category="+topicId+"&type=multiple";

        TriviaResponse response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(TriviaResponse.class)
                .doOnError(e -> log.error("Failed to retrieve questions from Trivia API. Error: {}", e.getMessage()))
                .block();

        return convertedList(response, topicId);
    }

    private List<Question> convertedList(TriviaResponse response, int topicId) {

        if(response == null || response.getResponseCode() != 0) {
            log.error("Trivia API failed!");
            throw new BadRequestException("Unable to fetch questions!");
        }

        List<TriviaQuestion> questionList = response.getQuestionList();
        if(questionList == null || questionList.isEmpty()) {
            log.error("No questions found for topic: {}", getTriviaTopics().get(topicId));
            throw new BadRequestException("No questions found!");
        }

        List<Question> questionsToSend = new ArrayList<>();
        int qno = 1;

        for(TriviaQuestion t: questionList) {

            Question question = new Question();
            List<String> options = new ArrayList<>(4);

            options.addAll(t.getIncorrect_options());
            options.add(t.getCorrectOption());
            options.replaceAll(StringEscapeUtils::unescapeHtml4);
            Collections.shuffle(options);

            question.setQno(qno++);
            question.setQuizId(1);
            question.setQuestion(StringEscapeUtils.unescapeHtml4(t.getQuestion()));
            question.setCorrectOpt(StringEscapeUtils.unescapeHtml4(t.getCorrectOption()));
            question.setOpt1(options.getFirst());
            question.setOpt2(options.get(1));
            question.setOpt3(options.get(2));
            question.setOpt4(options.getLast());

            questionsToSend.add(question);
        }

        log.info("Questions got from Open Trivia DB!");
        return questionsToSend;
    }

    public Map<Integer, String> getTriviaTopics() {
        return  Map.ofEntries(
                Map.entry(9, "General Knowledge"),
                Map.entry(10, "Entertainment: Books"),
                Map.entry(11, "Entertainment: Film"),
                Map.entry(12, "Entertainment: Music"),
                Map.entry(13, "Entertainment: Musicals & Theatres"),
                Map.entry(14, "Entertainment: Television"),
                Map.entry(15, "Entertainment: Video Games"),
                Map.entry(16, "Entertainment: Board Games"),
                Map.entry(17, "Science & Nature"),
                Map.entry(18, "Science: Computers"),
                Map.entry(19, "Science: Mathematics"),
                Map.entry(20, "Mythology"),
                Map.entry(21, "Sports"),
                Map.entry(22, "Geography"),
                Map.entry(23, "History"),
                Map.entry(24, "Politics"),
                Map.entry(25, "Art"),
                Map.entry(26, "Celebrities"),
                Map.entry(27, "Animals"),
                Map.entry(28, "Vehicles"),
                Map.entry(29, "Entertainment: Comics"),
                Map.entry(30, "Science: Gadgets"),
                Map.entry(31, "Entertainment: Japanese Anime & Manga"),
                Map.entry(32, "Entertainment: Cartoon & Animations")
        );
    }
}
