package com.amin.ai.confluence.service;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;

@Slf4j
@Service
public class WeaviateService {

    private static final int SEMANTIC_SEARCH_LIMIT = 6;
    private final WeaviateClient client;

    public WeaviateService(WeaviateClient client) {
        this.client = client;
    }

    public void createSchemaIfNotExists() {
        var existing = client.schema().getter().run();
        boolean found = existing.getResult().getClasses().stream()
                .anyMatch(c -> "ConfluenceDoc".equals(c.getClassName()));
        if (!found) {
            try {
                var classObj = WeaviateClass.builder()
                        .className("ConfluenceDoc")
                        .description("Confluence document chunk")
                        .vectorizer("none")
                        .properties(java.util.List.of(
                            io.weaviate.client.v1.schema.model.Property.builder().name("title").dataType(java.util.List.of("text")).build(),
                            io.weaviate.client.v1.schema.model.Property.builder().name("url").dataType(java.util.List.of("text")).build(),
                            io.weaviate.client.v1.schema.model.Property.builder().name("content").dataType(java.util.List.of("text")).build(),
                            io.weaviate.client.v1.schema.model.Property.builder().name("weaviateId").dataType(java.util.List.of("text")).build(),
                            io.weaviate.client.v1.schema.model.Property.builder().name("createdAt").dataType(java.util.List.of("date")).build(),
                            io.weaviate.client.v1.schema.model.Property.builder().name("updatedAt").dataType(java.util.List.of("date")).build(),
                            io.weaviate.client.v1.schema.model.Property.builder().name("confluencePageUpdatedAt").dataType(java.util.List.of("date")).build(),
                            io.weaviate.client.v1.schema.model.Property.builder().name("tags").dataType(java.util.List.of("text")).build(),
                            io.weaviate.client.v1.schema.model.Property.builder().name("metadata").dataType(java.util.List.of("text")).build()
                        ))
                        .build();
                var result = client.schema().classCreator()
                        .withClass(classObj)
                        .run();
                if (result.getResult() == null) {
                    log.error("Failed to create schema: {}", result.getError());
                }
            } catch (Exception e) {
                log.error("Exception during schema creation", e);
            }
        }
    }

    public boolean saveDocument(Map<String, Object> props, float[] embedding) {
        Float[] vector = new Float[embedding.length];
        for (int i = 0; i < embedding.length; i++) {
            vector[i] = embedding[i];
        }
        try {
            var result = client.data().creator()
                    .withClassName("ConfluenceDoc")
                    .withProperties(props)
                    .withVector(vector)
                    .run();
            if (result.getResult() == null) {
                log.error("Failed to save document: {}\nProperties: {}\nVector: {}", result.getError(), props, vector);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Exception during document save. Properties: {} Vector: {}", props, vector, e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> semanticSearch(float[] queryEmbedding) {
        Float[] vector = new Float[queryEmbedding.length];
        for (int i = 0; i < queryEmbedding.length; i++) {
            vector[i] = queryEmbedding[i];
        }
        NearVectorArgument nearVector = NearVectorArgument.builder().vector(vector).build();
        var result = client.graphQL().get()
            .withClassName("ConfluenceDoc")
            .withFields(
                Field.builder().name("title").build(),
                Field.builder().name("url").build(),
                Field.builder().name("content").build(),
                Field.builder().name("tags").build(),
                Field.builder().name("metadata").build(),
                Field.builder().name("createdAt").build(),
                Field.builder().name("updatedAt").build()
            )
            .withNearVector(nearVector)
            .withLimit(SEMANTIC_SEARCH_LIMIT)
            .run();
        if (result.getResult() == null) {
            log.error("Weaviate semantic search failed: {}", result.getError());
            return List.of();
        }
        // Log the actual structure for debugging
        log.info("Weaviate response structure: {}", result.getResult());
        // Try to extract docs from the result
        Object dataObj = result.getResult().getData();
        if (dataObj instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) dataObj;
            Object getObj = dataMap.get("Get");
            if (getObj instanceof Map) {
                Map<?, ?> getMap = (Map<?, ?>) getObj;
                Object confluenceDocObj = getMap.get("ConfluenceDoc");
                if (confluenceDocObj instanceof List<?>) {
                    List<?> docList = (List<?>) confluenceDocObj;
                    // Each element is a LinkedTreeMap with 7 key-value pairs
                    return docList.stream()
                        .filter(Map.class::isInstance)
                        .map(m -> (Map<String, Object>) m)
                        .collect(Collectors.toList());
                }
            }
        }
        log.error("Could not find ConfluenceDoc in Weaviate response: {}", result.getResult());
        return List.of();
    }
}
