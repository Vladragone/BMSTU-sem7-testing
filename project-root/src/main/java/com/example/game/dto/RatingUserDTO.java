package com.example.game.dto;

public class RatingUserDTO {
    private String username;
    private int score;
    private int gameNum;
    private int rank;

    public RatingUserDTO() {}

    public RatingUserDTO(String username, int score, int gameNum, int rank) {
        this.username = username;
        this.score = score;
        this.gameNum = gameNum;
        this.rank = rank;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getGameNum() { return gameNum; }
    public void setGameNum(int gameNum) { this.gameNum = gameNum; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
}
