package com.amin.ai.confluence.service;

import java.util.List;
import java.util.Map;

public interface WeaviateService {

    void createSchemaIfNotExists();
    boolean saveDocument(Map<String, Object> props, float[] embedding);
    List<Map<String, Object>> semanticSearch(float[] queryEmbedding);
}
