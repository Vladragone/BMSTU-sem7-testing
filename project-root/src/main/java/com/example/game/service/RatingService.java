package com.example.game.service;

import com.example.game.dto.RatingResponseDTO;
import com.example.game.dto.RatingUserDTO;
import com.example.game.model.Profile;
import com.example.game.repository.ProfileRepository;
import com.example.game.service.interfaces.IRatingService;
import com.example.game.service.interfaces.ITokenParser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RatingService implements IRatingService {

    private final ProfileRepository profileRepository;
    private final ITokenParser tokenParser;

    public RatingService(ProfileRepository profileRepository, ITokenParser tokenParser) {
        this.profileRepository = profileRepository;
        this.tokenParser = tokenParser;
    }

    @Override
    @Transactional(readOnly = true)
    public RatingResponseDTO getSortedRatingAndRank(String token, String sortBy, int limit) {
        try {
            String currentUsername = tokenParser.getUsername(token);
            List<Profile> profiles = profileRepository.findAll();
            if (profiles.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No profiles found");
            }

            Comparator<Profile> comparator = "games".equals(sortBy)
                    ? Comparator.comparingInt(Profile::getGameNum)
                    : Comparator.comparingInt(Profile::getScore);

            List<Profile> sortedProfiles = profiles.stream()
                    .sorted(comparator.reversed())
                    .collect(Collectors.toList());

            List<RatingUserDTO> topUsers = sortedProfiles.stream()
                    .limit(limit)
                    .map(this::toDto)
                    .collect(Collectors.toList());

            int rank = findUserRank(sortedProfiles, currentUsername);
            if (rank <= 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in rating");
            }

            // Добавляем ранги в DTO (по позиции)
            for (int i = 0; i < topUsers.size(); i++) {
                topUsers.get(i).setRank(i + 1);
            }

            return new RatingResponseDTO(topUsers, rank, sortBy);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching ratings", e);
        }
    }

    private RatingUserDTO toDto(Profile profile) {
        return new RatingUserDTO(
                profile.getUser().getUsername(),
                profile.getScore(),
                profile.getGameNum(),
                0
        );
    }

    private int findUserRank(List<Profile> profiles, String username) {
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getUser().getUsername().equals(username)) {
                return i + 1;
            }
        }
        return -1;
    }
}
