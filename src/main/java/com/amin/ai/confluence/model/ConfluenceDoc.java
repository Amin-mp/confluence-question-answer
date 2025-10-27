package com.amin.ai.confluence.model;


import lombok.*;
import java.time.LocalDateTime;

/**
 * @author <a href="amin.malekpour@e2open.com">Amin Malekpour</a>
 * @version 1.0, 26/Oct/2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfluenceDoc {
    private Long id;

    private String title;

    private String url;

    private String content;

    // reference to object stored in Weaviate
    private String weaviateId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confluencePageUpdatedAt;

    private String tags;

    private String metadata;

    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
