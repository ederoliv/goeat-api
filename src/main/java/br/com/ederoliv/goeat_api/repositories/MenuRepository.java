package br.com.ederoliv.goeat_api.repositories;

import br.com.ederoliv.goeat_api.entities.Menu;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

}
