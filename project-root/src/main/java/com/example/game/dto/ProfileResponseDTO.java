package com.example.game.dto;

import java.time.LocalDateTime;

public class ProfileResponseDTO {
    private Long id;
    private String username;
    private int score;
    private int gameNum;
    private LocalDateTime regDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getGameNum() { return gameNum; }
    public void setGameNum(int gameNum) { this.gameNum = gameNum; }

    public LocalDateTime getRegDate() { return regDate; }
    public void setRegDate(LocalDateTime regDate) { this.regDate = regDate; }
}
