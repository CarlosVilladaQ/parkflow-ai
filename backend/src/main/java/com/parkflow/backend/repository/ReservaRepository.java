package com.parkflow.backend.repository;

import com.parkflow.backend.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    long countByTipoVehiculoAndEstado(String tipoVehiculo, String estado);
    List<Reserva> findByEstadoOrderByFechaHoraInicioAsc(String estado);
}
