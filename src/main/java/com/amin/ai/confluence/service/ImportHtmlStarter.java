package com.amin.ai.confluence.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static java.lang.String.format;

/**
 * @author <a href="amin.malekpour@e2open.com">Amin Malekpour</a>
 * @version 1.0, 26/Oct/2025
 */
@Component
public class ImportHtmlStarter {

    @Autowired
    private ConfluenceHtmlImporter confluenceHtmlImporter;

    @Autowired
    private WeaviateService weaviateService;

    @Value("${ai.confluence.html.directory}")
    private String htmlDirectory;

    @Scheduled(cron = "${ai.confluence.htmlImporter.cronExpression}")
//    @PostConstruct
    public void importHtmlFiles() throws IOException {
        weaviateService.createSchemaIfNotExists();
        confluenceHtmlImporter.importHtmlDirectory(htmlDirectory);
    }

}
