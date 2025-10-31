package com.example.game.service;

import com.example.game.model.User;
import com.example.game.repository.UserRepository;
import com.example.game.service.interfaces.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            return userRepository.existsByEmail(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking email existence", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        try {
            return userRepository.existsByUsername(username);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking username existence", e);
        }
    }

    @Override
    public User findUserByUsername(String username) {
        try {
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching user", e);
        }
    }
}
