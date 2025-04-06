package br.com.ederoliv.goeat_api.services;


import br.com.ederoliv.goeat_api.entities.Menu;
import br.com.ederoliv.goeat_api.repositories.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    public Menu findById(UUID id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found"));
    }



}