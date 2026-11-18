package com.parkflow.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record EntradaRequest(
        @NotBlank(message = "La placa es obligatoria")
        String placa,
        String tipoVehiculo
) {}
