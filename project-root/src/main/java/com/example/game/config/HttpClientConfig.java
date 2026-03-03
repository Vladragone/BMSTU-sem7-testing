package com.example.game.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate(ExternalWeatherProperties weatherProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(weatherProperties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(weatherProperties.getReadTimeoutMs());
        return new RestTemplate(requestFactory);
    }
}
