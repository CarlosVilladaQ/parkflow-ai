package com.parkflow.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tarifas")
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tipo_vehiculo", nullable = false, unique = true, length = 20)
    private String tipoVehiculo;

    @Column(name = "valor_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorHora;

    @Column(name = "valor_fraccion", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFraccion;

    @Column(name = "valor_dia", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDia;

    @Column(name = "activo")
    private Boolean activo = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }

    public BigDecimal getValorHora() { return valorHora; }
    public void setValorHora(BigDecimal valorHora) { this.valorHora = valorHora; }

    public BigDecimal getValorFraccion() { return valorFraccion; }
    public void setValorFraccion(BigDecimal valorFraccion) { this.valorFraccion = valorFraccion; }

    public BigDecimal getValorDia() { return valorDia; }
    public void setValorDia(BigDecimal valorDia) { this.valorDia = valorDia; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
