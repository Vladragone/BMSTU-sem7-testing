package com.example.game.service.interfaces;

import com.example.game.dto.UserRequestDTO;
import com.example.game.dto.UserResponseDTO;

public interface IRegistrationService {
    UserResponseDTO register(UserRequestDTO request);
}
