package com.example.game.dto;

public class GameRoundRequestDTO {
    private Long sessionId;
    private Long locationId;
    private Double guessLat;
    private Double guessLng;
    private Integer score;
    private Integer roundNumber;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public Double getGuessLat() { return guessLat; }
    public void setGuessLat(Double guessLat) { this.guessLat = guessLat; }

    public Double getGuessLng() { return guessLng; }
    public void setGuessLng(Double guessLng) { this.guessLng = guessLng; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getRoundNumber() { return roundNumber; }
    public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }
}
