package com.amin.ai.confluence.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.config.RequestConfig;

/**
 * @author <a href="amin.malekpour@e2open.com">Amin Malekpour</a>
 * @version 1.0, 26/Oct/2025
 */
@Configuration
public class ChatClientConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public RestTemplate restTemplate() {
        RequestConfig config = RequestConfig.custom()
            .setResponseTimeout(600, java.util.concurrent.TimeUnit.SECONDS)
            .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
            HttpClients.custom().setDefaultRequestConfig(config).build()
        );
        return new RestTemplate(factory);
    }
}
