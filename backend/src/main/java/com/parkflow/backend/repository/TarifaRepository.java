package com.parkflow.backend.repository;

import com.parkflow.backend.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {
    Optional<Tarifa> findByTipoVehiculoAndActivoTrue(String tipoVehiculo);
}
