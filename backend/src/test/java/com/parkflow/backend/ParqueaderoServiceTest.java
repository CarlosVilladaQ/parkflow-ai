package com.parkflow.backend;

import com.parkflow.backend.dto.EntradaRequest;
import com.parkflow.backend.model.ConfiguracionParqueadero;
import com.parkflow.backend.model.Tarifa;
import com.parkflow.backend.repository.ConfiguracionRepository;
import com.parkflow.backend.repository.ReservaRepository;
import com.parkflow.backend.repository.TarifaRepository;
import com.parkflow.backend.repository.TicketRepository;
import com.parkflow.backend.service.ParqueaderoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ParqueaderoServiceTest {

    private ConfiguracionRepository configRepo;
    private TarifaRepository tarifaRepo;
    private TicketRepository ticketRepo;
    private ReservaRepository reservaRepo;
    private ParqueaderoService service;

    @BeforeEach
    void setUp() {
        configRepo = Mockito.mock(ConfiguracionRepository.class);
        tarifaRepo = Mockito.mock(TarifaRepository.class);
        ticketRepo = Mockito.mock(TicketRepository.class);
        reservaRepo = Mockito.mock(ReservaRepository.class);
        service = new ParqueaderoService(configRepo, tarifaRepo, ticketRepo, reservaRepo);
    }

    @Test
    void calcularMonto_debeCobrarUnaHoraYDosFracciones() {
        Tarifa tarifaCarro = new Tarifa();
        tarifaCarro.setValorHora(new BigDecimal("4000.00"));
        tarifaCarro.setValorFraccion(new BigDecimal("1500.00"));
        tarifaCarro.setValorDia(new BigDecimal("25000.00"));

        OffsetDateTime entrada = OffsetDateTime.of(2026, 11, 17, 8, 0, 0, 0, ZoneOffset.ofHours(-5));
        OffsetDateTime salida = entrada.plusMinutes(80); // 1 hora y 20 minutos (2 fracciones de 15 min)

        BigDecimal total = service.calcularMonto(entrada, salida, tarifaCarro);

        // 4000 (1h) + 3000 (2 fracciones) = 7000
        assertEquals(new BigDecimal("7000.00"), total);
    }

    @Test
    void calcularMonto_noDebeSuperarTarifaPlenaDiaria() {
        Tarifa tarifaCarro = new Tarifa();
        tarifaCarro.setValorHora(new BigDecimal("4000.00"));
        tarifaCarro.setValorFraccion(new BigDecimal("1500.00"));
        tarifaCarro.setValorDia(new BigDecimal("25000.00"));

        OffsetDateTime entrada = OffsetDateTime.of(2026, 11, 17, 6, 0, 0, 0, ZoneOffset.ofHours(-5));
        OffsetDateTime salida = entrada.plusHours(10); // 10 horas serían 40.000, pero el tope es 25.000

        BigDecimal total = service.calcularMonto(entrada, salida, tarifaCarro);

        assertEquals(new BigDecimal("25000.00"), total);
    }

    @Test
    void registrarEntrada_debeFallarSiParqueaderoEstaLleno() {
        ConfiguracionParqueadero config = new ConfiguracionParqueadero();
        config.setCuposTotalesCarros(10);
        config.setCuposTotalesMotos(20);

        when(configRepo.findAll()).thenReturn(List.of(config));
        when(ticketRepo.findByPlacaAndEstado("ABC123", "ACTIVO")).thenReturn(Optional.empty());
        when(ticketRepo.countByTipoVehiculoAndEstado("CARRO", "ACTIVO")).thenReturn(10L);
        when(reservaRepo.countByTipoVehiculoAndEstado("CARRO", "CONFIRMADA")).thenReturn(0L);

        EntradaRequest request = new EntradaRequest("ABC123", "CARRO");

        assertThrows(IllegalStateException.class, () -> service.registrarEntrada(request));
    }
}
