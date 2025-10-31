package com.example.game.service;

import com.example.game.dto.FaqUpdateDTO;
import com.example.game.model.Faq;
import com.example.game.repository.FaqRepository;
import com.example.game.service.interfaces.IFaqService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;

@Service
public class FaqService implements IFaqService {

    private final FaqRepository faqRepository;

    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @Override
    public List<Faq> getAllFaqs() {
        try {
            return faqRepository.findAll();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при получении FAQ");
        }
    }

    @Override
    public Optional<Faq> getFaqById(Long id) {
        return faqRepository.findById(id);
    }

    @Override
    public Faq saveFaq(Faq faq) {
        try {
            return faqRepository.save(faq);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при сохранении FAQ");
        }
    }

    @Override
    public Faq updateFaq(Long id, FaqUpdateDTO updates) {
        return faqRepository.findById(id).map(existing -> {
            if (updates.getQuestion() != null) existing.setQuestion(updates.getQuestion());
            if (updates.getAnswer() != null) existing.setAnswer(updates.getAnswer());
            try {
                return faqRepository.save(existing);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при обновлении FAQ");
            }
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FAQ с id " + id + " не найден"));
    }

    @Override
    public void deleteFaq(Long id) {
        if (!faqRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "FAQ с id " + id + " не найден");
        try {
            faqRepository.deleteById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при удалении FAQ");
        }
    }
}
