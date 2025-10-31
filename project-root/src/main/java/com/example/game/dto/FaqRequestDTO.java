package com.example.game.dto;

import com.example.game.model.Faq;

public class FaqRequestDTO {
    private String question;
    private String answer;
    private Long userId;

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Faq toEntity() {
        Faq f = new Faq();
        f.setQuestion(question);
        f.setAnswer(answer);
        f.setUserId(userId);
        return f;
    }
}
