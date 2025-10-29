package com.amin.ai.confluence.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="amin.malekpour@e2open.com">Amin Malekpour</a>
 * @version 2.0, 26/Oct/2025
 */
@Slf4j
@Service
public class ConfluenceHtmlImporter {

    @Value("${ai.confluence.done.directory}")
    private String doneDirectory;

    private final WeaviateService weaviateService;
    private final EmbeddingModel  embeddingModel;

    public ConfluenceHtmlImporter(WeaviateServiceImpl weaviateService,
                                  EmbeddingModel embeddingModel) {
        this.weaviateService = weaviateService;
        this.embeddingModel = embeddingModel;
    }

    public void importHtmlDirectory(String folderPath) throws IOException {
        File folder = new File(folderPath);
        for (File file : Objects.requireNonNull(folder.listFiles((d, name) -> name.endsWith(".html")))) {
            chunkEmbedSaveHtmlFile(file);

            // Move processed file to "done" directory
            if (file.isFile() && file.exists()) {
                Files.move(file.toPath(), Path.of(doneDirectory, file.getName()));
            }
        }
    }

    /**
     * Chunk, embed, and save HTML file content to Weaviate
     * @param file
     * @throws IOException
     */
    private void chunkEmbedSaveHtmlFile(File file) throws IOException {
        Document doc = Jsoup.parse(file, "UTF-8");
        String title = doc.title();
        Elements sections = doc.select("h2, h3");

        if (sections.isEmpty()) {
            embedAndSaveToWeaviate(title, "main", doc.body().text(), null, null);
        } else {
            for (Element section : sections) {
                String sectionTitle = section.text();
                StringBuilder contentBuilder = new StringBuilder();
                Element next = section.nextElementSibling();

                while (next != null && !next.tagName().matches("h2|h3")) {
                    contentBuilder.append(next.text()).append(" ");
                    next = next.nextElementSibling();
                }

                String content = contentBuilder.toString().trim();
                if (!content.isEmpty()) {
                    embedAndSaveToWeaviate(sectionTitle, file.getName(), content, null, null);
                }
            }
        }
    }

    /**
     * Embed content and save to Weaviate
     * @param title
     * @param url
     * @param content
     * @param tags
     * @param metadataJson
     */
    private void embedAndSaveToWeaviate(String title, String url, String content, String tags, String metadataJson) {
        float[] embedding = embeddingModel.embed(content);
        Map<String, Object> props = new HashMap<>();
        props.put("title", title);
        props.put("url", url);
        props.put("content", content);
        props.put("tags", tags);
        props.put("metadata", metadataJson);
        String formattedDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        props.put("createdAt", formattedDate);
        props.put("updatedAt", formattedDate);
        // confluencePageUpdatedAt: not available from HTML, set null or RFC3339 default
        props.put("confluencePageUpdatedAt", null);
        // weaviateId: will be set by Weaviate, so set null here
        props.put("weaviateId", null);

        log.info("start saving document to Weaviate: {}", title);
        boolean success = weaviateService.saveDocument(props, embedding);

        if (!success) {
            throw new RuntimeException("Failed to save to Weaviate");
        }
        log.info("Successfully saved document to Weaviate: {}", title);
    }
}
