package com.example.game.service.interfaces;

import com.example.game.dto.ProfileRequestDTO;
import com.example.game.model.Profile;

public interface IProfileService {
    Profile getProfile(String username);
    Profile updateProfile(ProfileRequestDTO updates, String username);
}
