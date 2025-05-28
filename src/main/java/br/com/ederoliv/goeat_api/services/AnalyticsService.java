package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.dto.analytics.BestsellersResponseDTO;
import br.com.ederoliv.goeat_api.dto.analytics.DailySalesDTO;
import br.com.ederoliv.goeat_api.dto.analytics.ProductBestsellerDTO;
import br.com.ederoliv.goeat_api.dto.analytics.SalesTimelineResponseDTO;
import br.com.ederoliv.goeat_api.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final AuthenticationUtilsService authenticationUtilsService;

    public SalesTimelineResponseDTO getSalesTimeline(Integer period, Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        log.info("Gerando dados de sales-timeline para parceiro: {} - Período: {} dias", partnerId, period);

        // Calcular datas
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(period - 1); // -1 porque incluímos hoje

        log.info("Período calculado: {} até {}", startDate, endDate);

        // Buscar dados do banco
        List<DailySalesDTO> salesData = orderRepository.findDailySalesForPeriod(
                partnerId, startDate, endDate);

        log.info("Dados encontrados no banco: {} registros", salesData.size());

        // Criar mapa com os dados do banco para facilitar acesso
        Map<LocalDate, DailySalesDTO> salesMap = salesData.stream()
                .collect(Collectors.toMap(DailySalesDTO::orderDate, dto -> dto));

        // Preparar listas para resposta
        List<String> labels = new ArrayList<>();
        List<Integer> revenue = new ArrayList<>();
        List<Integer> orders = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        // Percorrer todos os dias do período
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // Adicionar label formatado
            labels.add(currentDate.format(formatter));

            // Verificar se existe dados para este dia
            DailySalesDTO dayData = salesMap.get(currentDate);
            if (dayData != null) {
                // Dados existem para este dia
                revenue.add(dayData.dailyRevenue() != null ? dayData.dailyRevenue() : 0);
                orders.add(dayData.dailyOrders() != null ? dayData.dailyOrders() : 0);
            } else {
                // Não há dados para este dia - adicionar zeros
                revenue.add(0);
                orders.add(0);
            }

            currentDate = currentDate.plusDays(1);
        }

        log.info("Resposta gerada com {} dias de dados", labels.size());
        log.info("Faturamento total do período: {} centavos",
                revenue.stream().mapToInt(Integer::intValue).sum());
        log.info("Total de pedidos do período: {}",
                orders.stream().mapToInt(Integer::intValue).sum());

        return new SalesTimelineResponseDTO(labels, revenue, orders);
    }

    public BestsellersResponseDTO getProductsBestsellers(Integer period, Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        log.info("Gerando dados de products-bestsellers para parceiro: {} - Período: {} dias", partnerId, period);

        // Calcular datas
        LocalDateTime endDateTime = LocalDateTime.now();
        LocalDateTime startDateTime = endDateTime.minusDays(period);

        log.info("Período calculado: {} até {}", startDateTime, endDateTime);

        // Buscar dados do banco
        List<ProductBestsellerDTO> bestsellersData = orderRepository.findProductsBestsellersByPeriod(
                partnerId, startDateTime, endDateTime);

        log.info("Produtos encontrados no banco: {} registros", bestsellersData.size());

        // Limitar aos top 15 produtos (o frontend usa top 10, mas mantemos margem)
        List<ProductBestsellerDTO> topProducts = bestsellersData.stream()
                .limit(15)
                .collect(Collectors.toList());

        log.info("Produtos após limite: {} registros", topProducts.size());

        // Log de debug para verificar dados
        if (!topProducts.isEmpty()) {
            ProductBestsellerDTO topProduct = topProducts.get(0);
            log.info("Produto mais vendido: {} - Quantidade: {} - Receita: {} centavos",
                    topProduct.name(), topProduct.quantity(), topProduct.revenue());
        }

        BestsellersResponseDTO response = new BestsellersResponseDTO(topProducts);

        log.info("Resposta de products-bestsellers gerada com sucesso para parceiro: {}", partnerId);
        return response;
    }
}