package com.example.game.service;

import com.example.game.dto.LoginRequestDTO;
import com.example.game.dto.TokenResponseDTO;
import com.example.game.model.User;
import com.example.game.repository.UserRepository;
import com.example.game.service.interfaces.IAuthService;
import com.example.game.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class AuthService implements IAuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public TokenResponseDTO authenticateUser(LoginRequestDTO req) {
        try {
            var userOpt = userRepository.findByUsername(req.getUsername());
            if (userOpt.isEmpty())
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");

            var user = userOpt.get();
            String hashed = hash(req.getPassword());
            if (!hashed.equals(user.getPassword()))
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");

            String token = JwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());
            return new TokenResponseDTO(token);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Authentication error", e);
        }
    }

    private String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Hashing error", e);
        }
    }
}
