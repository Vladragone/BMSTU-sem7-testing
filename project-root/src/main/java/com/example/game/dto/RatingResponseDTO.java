package com.example.game.dto;

import java.util.List;

public class RatingResponseDTO {
    private List<RatingUserDTO> topUsers;
    private int currentUserRank;
    private String sortBy;

    public RatingResponseDTO(List<RatingUserDTO> topUsers, int currentUserRank, String sortBy) {
        this.topUsers = topUsers;
        this.currentUserRank = currentUserRank;
        this.sortBy = sortBy;
    }

    public List<RatingUserDTO> getTopUsers() { return topUsers; }
    public void setTopUsers(List<RatingUserDTO> topUsers) { this.topUsers = topUsers; }

    public int getCurrentUserRank() { return currentUserRank; }
    public void setCurrentUserRank(int currentUserRank) { this.currentUserRank = currentUserRank; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
}
