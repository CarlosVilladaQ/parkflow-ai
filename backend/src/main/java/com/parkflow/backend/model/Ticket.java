package com.parkflow.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "placa", nullable = false, length = 10)
    private String placa;

    @Column(name = "tipo_vehiculo", nullable = false, length = 20)
    private String tipoVehiculo;

    @Column(name = "hora_entrada", nullable = false)
    private OffsetDateTime horaEntrada;

    @Column(name = "hora_salida")
    private OffsetDateTime horaSalida;

    @Column(name = "total_pagado", precision = 10, scale = 2)
    private BigDecimal totalPagado;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }

    public OffsetDateTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(OffsetDateTime horaEntrada) { this.horaEntrada = horaEntrada; }

    public OffsetDateTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(OffsetDateTime horaSalida) { this.horaSalida = horaSalida; }

    public BigDecimal getTotalPagado() { return totalPagado; }
    public void setTotalPagado(BigDecimal totalPagado) { this.totalPagado = totalPagado; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
