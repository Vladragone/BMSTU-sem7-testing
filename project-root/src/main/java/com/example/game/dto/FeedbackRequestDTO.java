package com.example.game.dto;

import com.example.game.model.Feedback;

public class FeedbackRequestDTO {
    private Long userId;
    private Integer rating;
    private String problem;
    private String description;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getProblem() { return problem; }
    public void setProblem(String problem) { this.problem = problem; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Feedback toEntity() {
        Feedback f = new Feedback();
        f.setUserId(userId);
        f.setRating(rating);
        f.setProblem(problem);
        f.setDescription(description);
        return f;
    }
}
