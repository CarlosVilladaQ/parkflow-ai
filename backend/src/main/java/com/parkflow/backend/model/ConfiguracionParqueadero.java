package com.parkflow.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracion_parqueadero")
public class ConfiguracionParqueadero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_parqueadero", nullable = false, length = 100)
    private String nombreParqueadero;

    @Column(name = "cupos_totales_carros", nullable = false)
    private Integer cuposTotalesCarros;

    @Column(name = "cupos_totales_motos", nullable = false)
    private Integer cuposTotalesMotos;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombreParqueadero() { return nombreParqueadero; }
    public void setNombreParqueadero(String nombreParqueadero) { this.nombreParqueadero = nombreParqueadero; }

    public Integer getCuposTotalesCarros() { return cuposTotalesCarros; }
    public void setCuposTotalesCarros(Integer cuposTotalesCarros) { this.cuposTotalesCarros = cuposTotalesCarros; }

    public Integer getCuposTotalesMotos() { return cuposTotalesMotos; }
    public void setCuposTotalesMotos(Integer cuposTotalesMotos) { this.cuposTotalesMotos = cuposTotalesMotos; }
}
