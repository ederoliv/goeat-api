package br.com.ederoliv.goeat_api.repositories;

import br.com.ederoliv.goeat_api.entities.OperatingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperatingHoursRepository extends JpaRepository<OperatingHours, Long> {
    List<OperatingHours> findByPartnerId(UUID partnerId);
    Optional<OperatingHours> findByPartnerIdAndDayOfWeek(UUID partnerId, DayOfWeek dayOfWeek);
    void deleteByPartnerId(UUID partnerId);
}