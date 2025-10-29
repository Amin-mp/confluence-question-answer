package com.amin.ai.confluence.config;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="amin.malekpour@e2open.com">Amin Malekpour</a>
 * @version 1.0, 26/Oct/2025
 */
@Configuration
public class WeaviateConfig {

    @Value("${weaviate.ip-port}")
    private String weaviateIpPort;

    @Bean
    public WeaviateClient weaviateClient() {
        Config config = new Config("http", weaviateIpPort);
        return new WeaviateClient(config);
    }
}
