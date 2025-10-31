package com.example.game.service;

import com.example.game.dto.FaqUpdateDTO;
import com.example.game.model.Faq;
import com.example.game.repository.FaqRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FaqServiceTest {

    @Mock
    private FaqRepository faqRepository;

    @InjectMocks
    private FaqService faqService;

    private Faq faq;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        faq = new Faq();
        faq.setId(1L);
        faq.setQuestion("Как играть?");
        faq.setAnswer("Нажми cтарт");
        faq.setUserId(10L);
    }

    @Test
    void getAllFaqs_returnsList() {
        when(faqRepository.findAll()).thenReturn(List.of(faq));
        var result = faqService.getAllFaqs();
        assertEquals(1, result.size());
        assertEquals("Как играть?", result.get(0).getQuestion());
        verify(faqRepository).findAll();
    }

    @Test
    void getAllFaqs_throwsException() {
        when(faqRepository.findAll()).thenThrow(new RuntimeException("DB error"));
        assertThrows(ResponseStatusException.class, () -> faqService.getAllFaqs());
    }

    @Test
    void getFaqById_found() {
        when(faqRepository.findById(1L)).thenReturn(Optional.of(faq));
        var result = faqService.getFaqById(1L);
        assertTrue(result.isPresent());
        assertEquals("Как играть?", result.get().getQuestion());
    }

    @Test
    void getFaqById_notFound() {
        when(faqRepository.findById(99L)).thenReturn(Optional.empty());
        var result = faqService.getFaqById(99L);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveFaq_success() {
        when(faqRepository.save(faq)).thenReturn(faq);
        var saved = faqService.saveFaq(faq);
        assertEquals(faq.getQuestion(), saved.getQuestion());
    }

    @Test
    void saveFaq_throwsError() {
        when(faqRepository.save(any())).thenThrow(new RuntimeException("DB fail"));
        assertThrows(ResponseStatusException.class, () -> faqService.saveFaq(faq));
    }

    @Test
    void updateFaq_success() {
        FaqUpdateDTO updates = new FaqUpdateDTO();
        updates.setQuestion("Как начать?");
        updates.setAnswer("Просто нажми старт");
        when(faqRepository.findById(1L)).thenReturn(Optional.of(faq));
        when(faqRepository.save(any())).thenReturn(faq);
        Faq result = faqService.updateFaq(1L, updates);
        assertEquals("Как начать?", result.getQuestion());
        verify(faqRepository).save(any(Faq.class));
    }

    @Test
    void updateFaq_notFound() {
        when(faqRepository.findById(999L)).thenReturn(Optional.empty());
        FaqUpdateDTO updates = new FaqUpdateDTO();
        assertThrows(ResponseStatusException.class, () -> faqService.updateFaq(999L, updates));
    }

    @Test
    void deleteFaq_success() {
        when(faqRepository.existsById(1L)).thenReturn(true);
        doNothing().when(faqRepository).deleteById(1L);
        assertDoesNotThrow(() -> faqService.deleteFaq(1L));
        verify(faqRepository).deleteById(1L);
    }

    @Test
    void deleteFaq_notFound() {
        when(faqRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> faqService.deleteFaq(1L));
    }
}
