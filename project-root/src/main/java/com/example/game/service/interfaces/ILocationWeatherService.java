package com.example.game.service.interfaces;

import com.example.game.dto.LocationWeatherResponseDTO;

public interface ILocationWeatherService {
    LocationWeatherResponseDTO getCurrentWeatherByLocationId(Long locationId);
}
