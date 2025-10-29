package com.amin.ai.confluence.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="amin.malekpour@e2open.com">Amin Malekpour</a>
 * @version 1.0, 26/Oct/2025
 */
@Slf4j
@Service
public class RagServiceImpl implements RagService {
    private final EmbeddingModel  embeddingModel;
    private final WeaviateService weaviateService;
    private final ChatClient      chatClient;

    public RagServiceImpl(EmbeddingModel embeddingModel, WeaviateServiceImpl weaviateService, ChatClient chatClient) {
        this.embeddingModel = embeddingModel;
        this.weaviateService = weaviateService;
        this.chatClient = chatClient;
    }

    /**
     * Ask a question using RAG approach
     * @param question
     * @return
     */
    @Override
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
        log.info("\nRAG Prompt: \n{}", promptText);

        // 4. Send to Ollama (Mistral) and get answer
        Prompt prompt = new Prompt(promptText);
        var response = chatClient.prompt(prompt).call().chatResponse();
        assert response != null;
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