package com.amin.ai.confluence.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import com.amin.ai.confluence.service.RagServiceImpl;

/**
 * @author <a href="amin.malekpour@e2open.com">Amin Malekpour</a>
 * @version 1.0, 26/Oct/2025
 */
@RestController
@RequestMapping("/rag")
public class RagController {
    private final RagServiceImpl ragServiceImpl;

    public RagController(RagServiceImpl ragServiceImpl) {
        this.ragServiceImpl = ragServiceImpl;
    }

    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestBody String question) {
        String answer = ragServiceImpl.ask(question);
        return ResponseEntity.ok(answer);
    }
}
