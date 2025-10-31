package com.example.game.service;

import com.example.game.model.User;
import com.example.game.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
    }

    @Test
    void existsByEmail_success() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        boolean result = userService.existsByEmail("john@example.com");
        assertTrue(result);
        verify(userRepository).existsByEmail("john@example.com");
    }

    @Test
    void existsByEmail_error() {
        when(userRepository.existsByEmail(any())).thenThrow(new RuntimeException("DB error"));
        assertThrows(ResponseStatusException.class, () -> userService.existsByEmail("test@example.com"));
    }

    @Test
    void existsByUsername_success() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        boolean result = userService.existsByUsername("john");
        assertFalse(result);
        verify(userRepository).existsByUsername("john");
    }

    @Test
    void existsByUsername_error() {
        when(userRepository.existsByUsername(any())).thenThrow(new RuntimeException("DB fail"));
        assertThrows(ResponseStatusException.class, () -> userService.existsByUsername("bob"));
    }

    @Test
    void findUserByUsername_success() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        User result = userService.findUserByUsername("john");
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).findByUsername("john");
    }

    @Test
    void findUserByUsername_notFound() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
        User result = userService.findUserByUsername("bob");
        assertNull(result);
    }

    @Test
    void findUserByUsername_error() {
        when(userRepository.findByUsername(any())).thenThrow(new RuntimeException("DB issue"));
        assertThrows(ResponseStatusException.class, () -> userService.findUserByUsername("john"));
    }
}
