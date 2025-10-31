package com.example.game.service;

import com.example.game.model.Feedback;
import com.example.game.repository.FeedbackRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private Feedback feedback;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        feedback = new Feedback();
        feedback.setId(1L);
        feedback.setUserId(2L);
        feedback.setRating(5);
        feedback.setProblem("Bug");
        feedback.setDescription("Something went wrong");
    }

    @Test
    void saveFeedback_success() {
        when(feedbackRepository.save(feedback)).thenReturn(feedback);
        Feedback result = feedbackService.saveFeedback(feedback);
        assertEquals(5, result.getRating());
        verify(feedbackRepository).save(feedback);
    }

    @Test
    void saveFeedback_error() {
        when(feedbackRepository.save(any())).thenThrow(new RuntimeException("DB fail"));
        assertThrows(ResponseStatusException.class, () -> feedbackService.saveFeedback(feedback));
    }

    @Test
    void getAllFeedbacks_success() {
        when(feedbackRepository.findAll()).thenReturn(List.of(feedback));
        List<Feedback> result = feedbackService.getAllFeedbacks();
        assertEquals(1, result.size());
        verify(feedbackRepository).findAll();
    }

    @Test
    void getAllFeedbacks_error() {
        when(feedbackRepository.findAll()).thenThrow(new RuntimeException("DB error"));
        assertThrows(ResponseStatusException.class, () -> feedbackService.getAllFeedbacks());
    }

    @Test
    void getFeedbackById_success() {
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        Feedback result = feedbackService.getFeedbackById(1L);
        assertEquals("Bug", result.getProblem());
    }

    @Test
    void getFeedbackById_notFound() {
        when(feedbackRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> feedbackService.getFeedbackById(1L));
    }
}
