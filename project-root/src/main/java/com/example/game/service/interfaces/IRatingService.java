package com.example.game.service.interfaces;

import com.example.game.dto.RatingResponseDTO;

public interface IRatingService {
    RatingResponseDTO getSortedRatingAndRank(String token, String sortBy, int limit);
}
