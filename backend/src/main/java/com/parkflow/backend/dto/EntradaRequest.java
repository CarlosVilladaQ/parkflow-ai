package com.parkflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EntradaRequest(
        @NotBlank(message = "La placa es obligatoria")
        String placa,

        @NotBlank(message = "El tipo de vehículo es obligatorio")
        @Pattern(regexp = "(?i)(CARRO|MOTO)", message = "El tipo debe ser CARRO o MOTO")
        String tipoVehiculo,
) {}
