package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.entities.Support;
import br.com.ederoliv.goeat_api.repositories.SupportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportRepository supportRepository;

    public Optional<List<Support>> listAllSupportsByPartnerId(UUID partnerId) {
        return supportRepository.findSupportByPartnerId(partnerId);
    }

    public Support registerSupport(Support support) {
        Optional<Support> existingSupport = supportRepository.findByTitle(support.getTitle());

        if (existingSupport.isPresent()) {
            throw new IllegalArgumentException("Support already exists!");
        } else {
            return supportRepository.save(support);
        }

    }
}

