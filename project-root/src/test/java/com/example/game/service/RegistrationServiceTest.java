package com.example.game.service;

import com.example.game.dto.UserRequestDTO;
import com.example.game.dto.UserResponseDTO;
import com.example.game.service.interfaces.IRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {

    @Mock
    private IRegistrationService registrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_success() {
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("username");
        request.setEmail("email@test.com");
        request.setPassword("pass");

        UserResponseDTO response = new UserResponseDTO(1L, "username", "email@test.com", "USER");

        when(registrationService.register(request)).thenReturn(response);

        UserResponseDTO result = registrationService.register(request);

        assertEquals("username", result.getUsername());
        assertEquals("email@test.com", result.getEmail());
        assertEquals("USER", result.getRole());
        verify(registrationService, times(1)).register(request);
    }

    @Test
    void register_failure() {
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("username");
        request.setEmail("email@test.com");
        request.setPassword("pass");

        when(registrationService.register(request))
                .thenThrow(new RuntimeException("Email already exists"));

        assertThrows(RuntimeException.class, () -> registrationService.register(request));
        verify(registrationService, times(1)).register(request);
    }
}
