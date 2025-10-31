package com.example.game.dto;

public class GameSessionRequestDTO {
    private Long userId;
    private Long locationGroupId;
    private Integer totalRounds;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLocationGroupId() {
        return locationGroupId;
    }

    public void setLocationGroupId(Long locationGroupId) {
        this.locationGroupId = locationGroupId;
    }

    public Integer getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(Integer totalRounds) {
        this.totalRounds = totalRounds;
    }
}
