package com.example.game.repository;

import com.example.game.model.Faq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FaqRepositoryTest {

    @Autowired
    private FaqRepository faqRepository;

    private Faq faq;

    @BeforeEach
    void setUp() {
        faq = new Faq();
        faq.setQuestion("Q?");
        faq.setAnswer("A!");
        faq.setUserId(1L);
        faqRepository.save(faq);
    }

    @Test
    void findAll_returnsList() {
        List<Faq> faqs = faqRepository.findAll();
        assertFalse(faqs.isEmpty());
        assertEquals("Q?", faqs.get(0).getQuestion());
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        Optional<Faq> notFound = faqRepository.findById(999L);
        assertTrue(notFound.isEmpty());
    }
}
