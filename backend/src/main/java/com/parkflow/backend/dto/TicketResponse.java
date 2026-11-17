package com.parkflow.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TicketResponse(
        Long id,
        String placa,
        String tipoVehiculo,
        OffsetDateTime horaEntrada,
        OffsetDateTime horaSalida,
        BigDecimal totalPagado,
        String estado
) {}
