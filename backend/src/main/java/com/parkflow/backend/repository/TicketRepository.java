package com.parkflow.backend.repository;

import com.parkflow.backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    long countByTipoVehiculoAndEstado(String tipoVehiculo, String estado);
    Optional<Ticket> findByPlacaAndEstado(String placa, String estado);
    List<Ticket> findByEstadoOrderByHoraEntradaDesc(String estado);
}
