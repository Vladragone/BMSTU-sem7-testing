package com.example.game.dto;

public class LocationWeatherResponseDTO {
    private Long locationId;
    private String sourceMode;
    private String observedAt;
    private Double temperatureC;
    private Double windSpeedKmh;
    private Boolean isDay;
    private String dayPhase;
    private Integer weatherCode;
    private String weatherCondition;

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getSourceMode() {
        return sourceMode;
    }

    public void setSourceMode(String sourceMode) {
        this.sourceMode = sourceMode;
    }

    public String getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(String observedAt) {
        this.observedAt = observedAt;
    }

    public Double getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(Double temperatureC) {
        this.temperatureC = temperatureC;
    }

    public Double getWindSpeedKmh() {
        return windSpeedKmh;
    }

    public void setWindSpeedKmh(Double windSpeedKmh) {
        this.windSpeedKmh = windSpeedKmh;
    }

    public Boolean getIsDay() {
        return isDay;
    }

    public void setIsDay(Boolean isDay) {
        this.isDay = isDay;
    }

    public String getDayPhase() {
        return dayPhase;
    }

    public void setDayPhase(String dayPhase) {
        this.dayPhase = dayPhase;
    }

    public Integer getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(Integer weatherCode) {
        this.weatherCode = weatherCode;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }
}
