package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class AppConfig {

    @Value("${config.base.endpoint}")
    private String url;

    @Bean
    @LoadBalanced
    public WebClient.Builder registerWebClient () {
        log.info("URL: " + url);
        return WebClient.builder().baseUrl(url);
    }
}