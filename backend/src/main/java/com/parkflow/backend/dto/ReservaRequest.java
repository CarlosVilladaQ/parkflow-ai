package com.parkflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReservaRequest(
        @NotBlank(message = "La placa es obligatoria")
        String placa,

        @NotBlank(message = "El tipo de vehículo es obligatorio")
        @Pattern(regexp = "(?i)(CARRO|MOTO)", message = "El tipo debe ser CARRO o MOTO")
        String tipoVehiculo,

        @NotBlank(message = "El teléfono es obligatorio")
        String clienteTelefono
) {}
