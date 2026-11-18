package com.parkflow.backend.dto;

import java.math.BigDecimal;

public record TarifaResponse(
        String tipoVehiculo,
        BigDecimal valorHora,
        BigDecimal valorFraccion,
        BigDecimal valorDia
) {}
