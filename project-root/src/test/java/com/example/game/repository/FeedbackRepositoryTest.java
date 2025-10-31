package com.example.game.repository;

import com.example.game.model.Feedback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FeedbackRepositoryTest {

    @Autowired
    private FeedbackRepository feedbackRepository;

    private Feedback feedback;

    @BeforeEach
    void setUp() {
        feedback = new Feedback();
        feedback.setUserId(2L);
        feedback.setRating(5);
        feedback.setProblem("Bug");
        feedback.setDescription("Fix it");
        feedbackRepository.save(feedback);
    }

    @Test
    void findAll_returnsFeedbacks() {
        List<Feedback> list = feedbackRepository.findAll();
        assertEquals(1, list.size());
    }

    @Test
    void findById_notFound() {
        Optional<Feedback> result = feedbackRepository.findById(999L);
        assertTrue(result.isEmpty());
    }
}
