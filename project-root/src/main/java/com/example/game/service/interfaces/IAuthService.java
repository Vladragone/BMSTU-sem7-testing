package com.example.game.service.interfaces;

import com.example.game.dto.LoginRequestDTO;
import com.example.game.dto.TokenResponseDTO;

public interface IAuthService {
    TokenResponseDTO authenticateUser(LoginRequestDTO req);
}
