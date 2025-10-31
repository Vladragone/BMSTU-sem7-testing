package com.example.game.service.interfaces;

import com.example.game.model.Feedback;
import java.util.List;

public interface IFeedbackService {
    Feedback saveFeedback(Feedback feedback);
    List<Feedback> getAllFeedbacks();
    Feedback getFeedbackById(Long id);
}
