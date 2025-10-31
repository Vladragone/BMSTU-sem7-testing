package com.example.game.service.interfaces;

import com.example.game.dto.FaqUpdateDTO;
import com.example.game.model.Faq;
import java.util.List;
import java.util.Optional;

public interface IFaqService {
    List<Faq> getAllFaqs();
    Optional<Faq> getFaqById(Long id);
    Faq saveFaq(Faq faq);
    Faq updateFaq(Long id, FaqUpdateDTO updates);
    void deleteFaq(Long id);
}
