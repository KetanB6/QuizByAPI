package com.ketan.QuizByAI.controller;

import com.ketan.QuizByAI.model.AiQuizRequestDTO;
import com.ketan.QuizByAI.model.Question;
import com.ketan.QuizByAI.service.QuizByAiService;
import com.ketan.QuizByAI.service.QuizByTriviaDB;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
//@CrossOrigin(origins = "https://quizkrida.vercel.app")
@CrossOrigin("*")
@RequestMapping("/api/v1")
public class QuizByAiController {

    private final QuizByTriviaDB quizByTriviaDB;
    private final QuizByAiService quizByAiService;

    public QuizByAiController (QuizByTriviaDB topicValidation, QuizByAiService quizByAiService) {
        this.quizByTriviaDB = topicValidation;
        this.quizByAiService = quizByAiService;
    }

    @GetMapping
    public String apiInfo() {
        return "You are on API URL. Please visit official site: https://myquizapp-psi.vercel.app/";
    }

    @GetMapping("/Health")
    public ResponseEntity<Void> health() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/Generate")
    public ResponseEntity<List<Question>> generateQuiz(@RequestBody AiQuizRequestDTO dto) {
        return ResponseEntity.ok(quizByAiService.generateQuestion(dto));
    }

    @GetMapping("/Topics")
    public ResponseEntity<Map<Integer, String>> topics() {
        return ResponseEntity.ok(quizByTriviaDB.getTriviaTopics());
    }

    @GetMapping("/Live/{topicId}")
    public ResponseEntity<List<Question>> triviaQuestions(@PathVariable int topicId) {
        return ResponseEntity.ok(quizByTriviaDB.getQuestion(topicId));
    }

}
