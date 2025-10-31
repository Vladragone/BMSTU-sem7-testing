package com.example.game.service.interfaces;

import com.example.game.model.User;

public interface IUserService {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    User findUserByUsername(String username);
}
