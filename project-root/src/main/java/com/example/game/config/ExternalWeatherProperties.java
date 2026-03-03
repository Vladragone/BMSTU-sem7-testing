package com.example.game.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "external.weather")
public class ExternalWeatherProperties {

    private String mode = "real";
    private String realBaseUrl = "https://api.open-meteo.com";
    private String mockBaseUrl = "http://localhost:8081";
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 3000;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getRealBaseUrl() {
        return realBaseUrl;
    }

    public void setRealBaseUrl(String realBaseUrl) {
        this.realBaseUrl = realBaseUrl;
    }

    public String getMockBaseUrl() {
        return mockBaseUrl;
    }

    public void setMockBaseUrl(String mockBaseUrl) {
        this.mockBaseUrl = mockBaseUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public String resolveBaseUrl() {
        if ("mock".equalsIgnoreCase(mode)) {
            return mockBaseUrl;
        }
        return realBaseUrl;
    }
}
