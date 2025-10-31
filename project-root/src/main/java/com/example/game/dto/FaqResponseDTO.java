package com.example.game.dto;

public class FaqResponseDTO {
    private Long id;
    private String question;
    private String answer;
    private Long userId;

    public FaqResponseDTO() {}

    public FaqResponseDTO(Long id, String question, String answer, Long userId) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
