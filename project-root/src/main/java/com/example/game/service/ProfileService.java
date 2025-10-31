package com.example.game.service;

import com.example.game.dto.ProfileRequestDTO;
import com.example.game.model.Profile;
import com.example.game.repository.ProfileRepository;
import com.example.game.service.interfaces.IProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class ProfileService implements IProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public Profile getProfile(String username) {
        try {
            return profileRepository.findByUserUsername(username).orElse(null);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching profile", e);
        }
    }

    @Override
    public Profile updateProfile(ProfileRequestDTO updates, String username) {
        try {
            Optional<Profile> optionalProfile = profileRepository.findByUserUsername(username);
            if (optionalProfile.isEmpty()) {
                return null;
            }

            Profile profile = optionalProfile.get();

            if (updates.getScore() != null) {
                profile.setScore(profile.getScore() + updates.getScore());
                profile.setGameNum(profile.getGameNum() + 1);
            }

            return profileRepository.save(profile);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating profile", e);
        }
    }
}
