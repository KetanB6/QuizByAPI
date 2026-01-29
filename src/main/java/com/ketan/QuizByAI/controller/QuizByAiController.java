package com.ketan.QuizByAI.controller;

import com.ketan.QuizByAI.model.AiQuizRequestDTO;
import com.ketan.QuizByAI.model.Question;
import com.ketan.QuizByAI.service.QuizByAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@CrossOrigin(origins = "https://myquizapp-psi.vercel.app")
@CrossOrigin("*")
public class QuizByAiController {

    @Autowired
    private QuizByAiService service;

    @GetMapping("/Health")
    public ResponseEntity<Void> health() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/Generate")
    public ResponseEntity<List<Question>> generateQuiz(@RequestBody AiQuizRequestDTO dto) {
        return ResponseEntity.ok(service.generateQuestion(dto));
    }

}
