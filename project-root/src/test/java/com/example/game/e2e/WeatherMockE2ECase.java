package com.example.game.e2e;

import com.example.game.dto.LocationGroupRequestDTO;
import com.example.game.dto.LocationGroupResponseDTO;
import com.example.game.dto.LocationRequestDTO;
import com.example.game.dto.LocationResponseDTO;
import com.example.game.dto.LocationWeatherResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/sql/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WeatherMockE2ECase {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getCurrentWeather_usesMockServerContract() {
        String baseUrl = "http://localhost:" + port;

        LocationGroupResponseDTO group = createGroup(baseUrl, "mock_weather_group");
        LocationResponseDTO location = createLocation(baseUrl, 55.7558, 37.6176, group.getId());

        ResponseEntity<LocationWeatherResponseDTO> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/locations/" + location.getId() + "/weather/current",
                LocationWeatherResponseDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mock", response.getBody().getSourceMode());
        assertEquals(21.5, response.getBody().getTemperatureC(), 0.001);
        assertEquals(12.3, response.getBody().getWindSpeedKmh(), 0.001);
        assertEquals("2026-03-02T12:00", response.getBody().getObservedAt());
    }

    private LocationGroupResponseDTO createGroup(String baseUrl, String name) {
        ResponseEntity<LocationGroupResponseDTO> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/location-groups",
                new LocationGroupRequestDTO(name),
                LocationGroupResponseDTO.class
        );
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private LocationResponseDTO createLocation(String baseUrl, double lat, double lng, Long groupId) {
        LocationRequestDTO request = new LocationRequestDTO();
        request.setLat(lat);
        request.setLng(lng);
        request.setGroupId(groupId);

        ResponseEntity<LocationResponseDTO> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/locations",
                request,
                LocationResponseDTO.class
        );
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }
}
