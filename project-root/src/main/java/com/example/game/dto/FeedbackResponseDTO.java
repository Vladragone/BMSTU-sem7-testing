package com.example.game.dto;

public class FeedbackResponseDTO {
    private Long id;
    private Long userId;
    private Integer rating;
    private String problem;
    private String description;

    public FeedbackResponseDTO() {}

    public FeedbackResponseDTO(Long id, Long userId, Integer rating, String problem, String description) {
        this.id = id;
        this.userId = userId;
        this.rating = rating;
        this.problem = problem;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getProblem() { return problem; }
    public void setProblem(String problem) { this.problem = problem; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
