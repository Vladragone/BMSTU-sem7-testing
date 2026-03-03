package com.example.game.service;

import com.example.game.config.ExternalWeatherProperties;
import com.example.game.dto.LocationWeatherResponseDTO;
import com.example.game.model.Location;
import com.example.game.repository.LocationRepository;
import com.example.game.service.interfaces.ILocationWeatherService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class LocationWeatherService implements ILocationWeatherService {

    private final LocationRepository locationRepository;
    private final ExternalWeatherProperties weatherProperties;
    private final RestTemplate restTemplate;

    public LocationWeatherService(LocationRepository locationRepository,
                                  ExternalWeatherProperties weatherProperties,
                                  RestTemplate restTemplate) {
        this.locationRepository = locationRepository;
        this.weatherProperties = weatherProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public LocationWeatherResponseDTO getCurrentWeatherByLocationId(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Локация не найдена"));

        URI uri = UriComponentsBuilder.fromHttpUrl(weatherProperties.resolveBaseUrl())
                .path("/v1/forecast")
                .queryParam("latitude", location.getLat())
                .queryParam("longitude", location.getLng())
                .queryParam("current", "temperature_2m,wind_speed_10m")
                .build()
                .toUri();

        OpenMeteoResponse externalResponse;
        try {
            externalResponse = restTemplate.getForObject(uri, OpenMeteoResponse.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ошибка вызова внешнего погодного сервиса", e);
        }

        if (externalResponse == null || externalResponse.current == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Пустой ответ внешнего погодного сервиса");
        }

        LocationWeatherResponseDTO response = new LocationWeatherResponseDTO();
        response.setLocationId(locationId);
        response.setSourceMode(weatherProperties.getMode());
        response.setObservedAt(externalResponse.current.time);
        response.setTemperatureC(externalResponse.current.temperature2m);
        response.setWindSpeedKmh(externalResponse.current.windSpeed10m);
        return response;
    }

    private static class OpenMeteoResponse {
        private CurrentWeather current;
    }

    private static class CurrentWeather {
        private String time;
        @JsonProperty("temperature_2m")
        private Double temperature2m;
        @JsonProperty("wind_speed_10m")
        private Double windSpeed10m;
    }
}
