package com.amin.ai.confluence.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagService {
    private final EmbeddingModel embeddingModel;
    private final WeaviateService weaviateService;
    private final ChatClient chatClient;

    public RagService(EmbeddingModel embeddingModel, WeaviateService weaviateService, ChatClient chatClient) {
        this.embeddingModel = embeddingModel;
        this.weaviateService = weaviateService;
        this.chatClient = chatClient;
    }

    public String ask(String question) {
        // 1. Embed the question
        float[] questionEmbedding = embeddingModel.embed(question);

        // 2. Retrieve relevant context from Weaviate
        List<Map<String, Object>> docs = weaviateService.semanticSearch(questionEmbedding);
        String context = docs.stream()
                .map(doc -> doc.get("content").toString())
                .collect(Collectors.joining("\n---\n"));

        // 3. Build prompt
        String promptText = "Context:\n" + context + "\n\nQuestion: " + question;

        // 4. Send to Ollama (Mistral:7b) and get answer
        Prompt prompt = new Prompt(promptText);
        var response = chatClient.prompt(prompt).call().chatResponse();
        var results = response.getResults();
        if (results == null || results.isEmpty()) {
            return "No response from chat model.";
        }
        var output = results.get(0).getOutput();
        // Try getMessage(), fallback to toString()
        if (output != null) {
            try {
                // If getMessage() exists
                return output.getText();
            } catch (Exception e) {
                // Fallback to toString()
                return output.toString();
            }
        }
        return "No output from chat model.";
    }
}