package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.dto.operatingHours.OperatingHoursFullResponseDTO;
import br.com.ederoliv.goeat_api.dto.operatingHours.OperatingHoursRequestDTO;
import br.com.ederoliv.goeat_api.dto.operatingHours.OperatingHoursResponseDTO;
import br.com.ederoliv.goeat_api.dto.operatingHours.OperatingHoursUpdateDTO;
import br.com.ederoliv.goeat_api.entities.OperatingHours;
import br.com.ederoliv.goeat_api.entities.Partner;
import br.com.ederoliv.goeat_api.repositories.OperatingHoursRepository;
import br.com.ederoliv.goeat_api.repositories.PartnerRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperatingHoursService {

    private final OperatingHoursRepository operatingHoursRepository;
    private final PartnerRepository partnerRepository;
    private final AuthenticationUtilsService authenticationUtilsService;
    private final OperatingHoursCacheService cacheService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Verifica se o restaurante está aberto no momento atual
     */
    public boolean isPartnerOpenNow(UUID partnerId) {
        // Primeiro, verificar se o status está em cache
        Boolean cachedStatus = cacheService.getOpenStatus(partnerId);
        if (cachedStatus != null) {
            return cachedStatus;
        }

        // Se não estiver em cache, buscar do banco de dados
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

        // Se o parceiro estiver definido como fechado manualmente, já retornamos false
        // Usa o getter que retorna o valor default se for nulo
        if (!partner.isOpen()) {
            // Armazenar no cache e retornar
            cacheService.setOpenStatus(partnerId, false);
            return false;
        }

        // Obtém o dia da semana e a hora atual
        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
        LocalTime currentTime = LocalTime.now();

        // Busca a configuração de horário para o dia atual
        Optional<OperatingHours> todayHours = operatingHoursRepository
                .findByPartnerIdAndDayOfWeek(partnerId, currentDay);

        // Se não tiver configuração para hoje ou se estiver configurado como fechado, retorna false
        if (todayHours.isEmpty() || !todayHours.get().isOpen()) {
            // Armazenar no cache e retornar
            cacheService.setOpenStatus(partnerId, false);
            return false;
        }

        // Verifica se o horário atual está dentro do horário de funcionamento
        LocalTime openingTime = todayHours.get().getOpeningTime();
        LocalTime closingTime = todayHours.get().getClosingTime();

        // Se o horário não estiver definido, considera como fechado
        if (openingTime == null || closingTime == null) {
            // Armazenar no cache e retornar
            cacheService.setOpenStatus(partnerId, false);
            return false;
        }

        // Está aberto se o horário atual estiver entre o horário de abertura e fechamento
        boolean isOpen = !currentTime.isBefore(openingTime) && !currentTime.isAfter(closingTime);

        // Armazenar no cache e retornar
        cacheService.setOpenStatus(partnerId, isOpen);
        return isOpen;
    }

    /**
     * Obtém todos os horários de funcionamento de um parceiro
     */
    public OperatingHoursFullResponseDTO getOperatingHours(UUID partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

        List<OperatingHours> hours = operatingHoursRepository.findByPartnerId(partnerId);

        // Se não tiver horários configurados, inicializa com valores padrão
        if (hours.isEmpty()) {
            hours = initializeDefaultHours(partner);
        }

        List<OperatingHoursResponseDTO> schedulesDTO = hours.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        // Verifica se está aberto agora
        boolean isOpenNow = isPartnerOpenNow(partnerId);

        return new OperatingHoursFullResponseDTO(
                schedulesDTO,
                isOpenNow,
                partner.isOpen()
        );
    }

    /**
     * Cria ou atualiza os horários de funcionamento para um parceiro
     */
    @Transactional
    public OperatingHoursFullResponseDTO updateOperatingHours(
            OperatingHoursUpdateDTO updateDTO,
            Authentication authentication) {

        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

        List<OperatingHours> existingHours = operatingHoursRepository.findByPartnerId(partnerId);

        // Mapeia os horários existentes por dia da semana para facilitar a atualização
        java.util.Map<DayOfWeek, OperatingHours> hoursByDay = existingHours.stream()
                .collect(Collectors.toMap(OperatingHours::getDayOfWeek, h -> h));

        List<OperatingHours> updatedHours = new ArrayList<>();

        // Processa cada dia da atualização
        for (OperatingHoursUpdateDTO.OperatingHoursDayDTO dayDTO : updateDTO.schedules()) {
            OperatingHours hours = hoursByDay.getOrDefault(dayDTO.dayOfWeek(), new OperatingHours());

            hours.setDayOfWeek(dayDTO.dayOfWeek());
            hours.setOpen(dayDTO.isOpen());
            hours.setPartner(partner);

            // Só atualiza os horários se o dia estiver marcado como aberto
            if (dayDTO.isOpen()) {
                try {
                    hours.setOpeningTime(parseTime(dayDTO.openingTime()));
                    hours.setClosingTime(parseTime(dayDTO.closingTime()));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException(
                            "Formato de horário inválido. Use o formato HH:mm para " + dayDTO.dayOfWeek());
                }
            }

            updatedHours.add(hours);
        }

        // Salva todos os horários atualizados
        operatingHoursRepository.saveAll(updatedHours);

        // Invalidar o cache deste parceiro
        invalidatePartnerCache(partnerId);

        // Retorna os dados atualizados
        return getOperatingHours(partnerId);
    }

    /**
     * Atualiza manualmente o status de aberto/fechado do restaurante
     */
    @Transactional
    public void updatePartnerOpenStatus(boolean isOpen, Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

        // Usa o setter seguro que nunca aceita null
        partner.setOpen(isOpen);
        partnerRepository.save(partner);

        // Invalidar o cache deste parceiro
        invalidatePartnerCache(partnerId);

        log.info("Status do parceiro {} atualizado para: {}", partnerId, isOpen ? "Aberto" : "Fechado");
    }

    /**
     * Inicializa os horários padrão para um parceiro (8h às 22h de segunda a sexta, 10h às 23h sábado, fechado domingo)
     */
    @Transactional
    public List<OperatingHours> initializeDefaultHours(Partner partner) {
        List<OperatingHours> defaultHours = new ArrayList<>();

        // Segunda a sexta: 8h às 22h
        for (DayOfWeek day : new DayOfWeek[]{
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {

            OperatingHours hours = new OperatingHours();
            hours.setDayOfWeek(day);
            hours.setOpen(true);
            hours.setOpeningTime(LocalTime.of(8, 0));
            hours.setClosingTime(LocalTime.of(22, 0));
            hours.setPartner(partner);

            defaultHours.add(hours);
        }

        // Sábado: 10h às 23h
        OperatingHours saturday = new OperatingHours();
        saturday.setDayOfWeek(DayOfWeek.SATURDAY);
        saturday.setOpen(true);
        saturday.setOpeningTime(LocalTime.of(10, 0));
        saturday.setClosingTime(LocalTime.of(23, 0));
        saturday.setPartner(partner);
        defaultHours.add(saturday);

        // Domingo: fechado
        OperatingHours sunday = new OperatingHours();
        sunday.setDayOfWeek(DayOfWeek.SUNDAY);
        sunday.setOpen(false);
        sunday.setOpeningTime(LocalTime.of(10, 0));
        sunday.setClosingTime(LocalTime.of(20, 0));
        sunday.setPartner(partner);
        defaultHours.add(sunday);

        return operatingHoursRepository.saveAll(defaultHours);
    }

    /**
     * Converte OperatingHours para DTO
     */
    private OperatingHoursResponseDTO mapToResponseDTO(OperatingHours hours) {
        return new OperatingHoursResponseDTO(
                hours.getId(),
                hours.getDayOfWeek(),
                hours.isOpen(),
                hours.getOpeningTime() != null ? hours.getOpeningTime().format(TIME_FORMATTER) : null,
                hours.getClosingTime() != null ? hours.getClosingTime().format(TIME_FORMATTER) : null
        );
    }

    /**
     * Converte string de horário para LocalTime
     */
    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        return LocalTime.parse(timeStr, TIME_FORMATTER);
    }

    /**
     * Adiciona um horário para um dia específico
     */
    @Transactional
    public OperatingHoursResponseDTO addOrUpdateOperatingHours(
            UUID partnerId,
            DayOfWeek dayOfWeek,
            OperatingHoursRequestDTO requestDTO) {

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não encontrado"));

        // Busca o horário existente ou cria um novo
        OperatingHours hours = operatingHoursRepository
                .findByPartnerIdAndDayOfWeek(partnerId, dayOfWeek)
                .orElse(new OperatingHours());

        hours.setDayOfWeek(dayOfWeek);
        hours.setOpen(requestDTO.isOpen());
        hours.setPartner(partner);

        // Só atualiza os horários se o dia estiver marcado como aberto
        if (requestDTO.isOpen()) {
            try {
                hours.setOpeningTime(parseTime(requestDTO.openingTime()));
                hours.setClosingTime(parseTime(requestDTO.closingTime()));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "Formato de horário inválido. Use o formato HH:mm");
            }
        }

        OperatingHours savedHours = operatingHoursRepository.save(hours);

        // Invalidar o cache deste parceiro
        invalidatePartnerCache(partnerId);

        return mapToResponseDTO(savedHours);
    }

    /**
     * Método para limpar o cache quando os horários são atualizados
     */
    private void invalidatePartnerCache(UUID partnerId) {
        cacheService.invalidateCache(partnerId);
    }
}