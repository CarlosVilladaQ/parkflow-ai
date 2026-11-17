package com.parkflow.backend.dto;

public record DisponibilidadResponse(
        String tipoVehiculo,
        int cuposTotales,
        int cuposOcupados,
        int cuposReservados,
        int cuposDisponibles
) {}
