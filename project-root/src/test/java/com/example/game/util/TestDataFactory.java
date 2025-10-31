package com.example.game.util;

import com.example.game.dto.LoginRequestDTO;
import com.example.game.dto.TokenResponseDTO;

public class TestDataFactory {

    public static LoginRequestDTO createLoginRequest(String username, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }

    public static TokenResponseDTO createTokenResponse(String token) {
        TokenResponseDTO dto = new TokenResponseDTO();
        dto.setToken(token);
        return dto;
    }
}
