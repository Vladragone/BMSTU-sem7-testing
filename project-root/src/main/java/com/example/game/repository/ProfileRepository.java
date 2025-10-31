package com.example.game.repository;

import com.example.game.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserUsername(String username);
    List<Profile> findTop10ByOrderByScoreDesc();
}
