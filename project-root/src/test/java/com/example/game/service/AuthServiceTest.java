package com.example.game.service;

import com.example.game.dto.LoginRequestDTO;
import com.example.game.dto.TokenResponseDTO;
import com.example.game.service.interfaces.IAuthService;
import com.example.game.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private IAuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticateUser_success() {
        LoginRequestDTO request = TestDataFactory.createLoginRequest("user", "pass");
        TokenResponseDTO expectedResponse = TestDataFactory.createTokenResponse("token123");
        when(authService.authenticateUser(request)).thenReturn(expectedResponse);
        TokenResponseDTO actualResponse = authService.authenticateUser(request);
        assertNotNull(actualResponse);
        assertEquals("token123", actualResponse.getToken());
        verify(authService, times(1)).authenticateUser(request);
    }

    @Test
    void authenticateUser_failure() {
        LoginRequestDTO request = TestDataFactory.createLoginRequest("user", "wrong");
        when(authService.authenticateUser(request))
                .thenThrow(new RuntimeException("Invalid credentials"));
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.authenticateUser(request)
        );
        assertEquals("Invalid credentials", exception.getMessage());
        verify(authService, times(1)).authenticateUser(request);
    }
}
