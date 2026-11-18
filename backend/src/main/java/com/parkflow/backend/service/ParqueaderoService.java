package com.parkflow.backend.service;

import com.parkflow.backend.dto.DisponibilidadResponse;
import com.parkflow.backend.dto.EntradaRequest;
import com.parkflow.backend.dto.TicketResponse;
import com.parkflow.backend.model.ConfiguracionParqueadero;
import com.parkflow.backend.model.Tarifa;
import com.parkflow.backend.model.Ticket;
import com.parkflow.backend.repository.ConfiguracionRepository;
import com.parkflow.backend.repository.ReservaRepository;
import com.parkflow.backend.repository.TarifaRepository;
import com.parkflow.backend.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ParqueaderoService {

    private static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

    private final ConfiguracionRepository configuracionRepo;
    private final TarifaRepository tarifaRepo;
    private final TicketRepository ticketRepo;
    private final ReservaRepository reservaRepo;

    public ParqueaderoService(ConfiguracionRepository configuracionRepo,
                              TarifaRepository tarifaRepo,
                              TicketRepository ticketRepo,
                              ReservaRepository reservaRepo) {
        this.configuracionRepo = configuracionRepo;
        this.tarifaRepo = tarifaRepo;
        this.ticketRepo = ticketRepo;
        this.reservaRepo = reservaRepo;
    }

    public DisponibilidadResponse consultarDisponibilidad(String tipoVehiculo) {
        String tipo = tipoVehiculo.toUpperCase();
        ConfiguracionParqueadero config = configuracionRepo.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe configuración del parqueadero"));

        int totales = tipo.equals("CARRO") ? config.getCuposTotalesCarros() : config.getCuposTotalesMotos();
        int ocupados = (int) ticketRepo.countByTipoVehiculoAndEstado(tipo, "ACTIVO");
        int reservados = (int) reservaRepo.countByTipoVehiculoAndEstado(tipo, "CONFIRMADA");
        int disponibles = Math.max(0, totales - ocupados - reservados);

        return new DisponibilidadResponse(tipo, totales, ocupados, reservados, disponibles);
    }

    @Transactional
    public TicketResponse registrarEntrada(EntradaRequest request) {
        String placaNormalizada = request.placa().trim().toUpperCase();
        String tipo = request.tipoVehiculo().toUpperCase();

        if (ticketRepo.findByPlacaAndEstado(placaNormalizada, "ACTIVO").isPresent()) {
            throw new IllegalArgumentException("El vehículo con placa " + placaNormalizada + " ya se encuentra dentro del parqueadero.");
        }

        DisponibilidadResponse disp = consultarDisponibilidad(tipo);
        if (disp.cuposDisponibles() <= 0) {
            throw new IllegalStateException("No hay cupos disponibles para " + tipo);
        }

        Ticket ticket = new Ticket();
        ticket.setPlaca(placaNormalizada);
        ticket.setTipoVehiculo(tipo);
        ticket.setHoraEntrada(OffsetDateTime.now(ZONA_COLOMBIA));
        ticket.setEstado("ACTIVO");

        return mapearTicket(ticketRepo.save(ticket));
    }

    @Transactional
    public TicketResponse registrarSalida(String placa) {
        String placaNormalizada = placa.trim().toUpperCase();
        Ticket ticket = ticketRepo.findByPlacaAndEstado(placaNormalizada, "ACTIVO")
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un ticket activo para la placa " + placaNormalizada));

        Tarifa tarifa = tarifaRepo.findByTipoVehiculoAndActivoTrue(ticket.getTipoVehiculo())
                .orElseThrow(() -> new IllegalStateException("No hay tarifa activa configurada para " + ticket.getTipoVehiculo()));

        OffsetDateTime ahora = OffsetDateTime.now(ZONA_COLOMBIA);
        BigDecimal total = calcularMonto(ticket.getHoraEntrada(), ahora, tarifa);

        ticket.setHoraSalida(ahora);
        ticket.setTotalPagado(total);
        ticket.setEstado("PAGADO");

        return mapearTicket(ticketRepo.save(ticket));
    }

    public BigDecimal calcularMonto(OffsetDateTime entrada, OffsetDateTime salida, Tarifa tarifa) {
        long minutos = Duration.between(entrada, salida).toMinutes();
        if (minutos <= 0) {
            return tarifa.getValorFraccion();
        }

        long horasCompletas = minutos / 60;
        long minutosRestantes = minutos % 60;

        BigDecimal cobroHoras = tarifa.getValorHora().multiply(BigDecimal.valueOf(horasCompletas));
        BigDecimal cobroFraccion = BigDecimal.ZERO;

        if (minutosRestantes > 0) {
            long fracciones = (long) Math.ceil(minutosRestantes / 15.0);
            cobroFraccion = tarifa.getValorFraccion().multiply(BigDecimal.valueOf(fracciones));
            if (cobroFraccion.compareTo(tarifa.getValorHora()) > 0) {
                cobroFraccion = tarifa.getValorHora();
            }
        }

        BigDecimal total = cobroHoras.add(cobroFraccion);
        // Si el total supera la tarifa plena del día, se cobra máximo el valor del día
        return total.min(tarifa.getValorDia());
    }

    public List<TicketResponse> listarActivos() {
        return ticketRepo.findByEstadoOrderByHoraEntradaDesc("ACTIVO")
                .stream()
                .map(this::mapearTicket)
                .toList();
    }

    private TicketResponse mapearTicket(Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getPlaca(),
                t.getTipoVehiculo(),
                t.getHoraEntrada(),
                t.getHoraSalida(),
                t.getTotalPagado(),
                t.getEstado()
        );
    }
        public com.parkflow.backend.dto.TarifaResponse consultarTarifa(String tipoVehiculo) {
        String tipo = tipoVehiculo.trim().toUpperCase();
        Tarifa tarifa = tarifaRepo.findByTipoVehiculoAndActivoTrue(tipo)
                .orElseThrow(() -> new IllegalArgumentException("No existe tarifa activa para: " + tipo));
        return new com.parkflow.backend.dto.TarifaResponse(
                tarifa.getTipoVehiculo(),
                tarifa.getValorHora(),
                tarifa.getValorFraccion(),
                tarifa.getValorDia()
        );
    }

    @Transactional
    public com.parkflow.backend.model.Reserva crearReserva(com.parkflow.backend.dto.ReservaRequest request) {
        String tipo = request.tipoVehiculo().trim().toUpperCase();
        DisponibilidadResponse disp = consultarDisponibilidad(tipo);
        if (disp.cuposDisponibles() <= 0) {
            throw new IllegalStateException("No hay cupos disponibles para reservar en este momento para " + tipo);
        }

        com.parkflow.backend.model.Reserva reserva = new com.parkflow.backend.model.Reserva();
        reserva.setPlaca(request.placa().trim().toUpperCase());
        reserva.setTipoVehiculo(tipo);
        reserva.setClienteTelefono(request.clienteTelefono().trim());
        reserva.setFechaHoraInicio(OffsetDateTime.now(ZONA_COLOMBIA));
        reserva.setEstado("CONFIRMADA");

        return reservaRepo.save(reserva);
    }

}
