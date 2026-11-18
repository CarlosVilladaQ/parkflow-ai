package com.parkflow.backend.controller;

import com.parkflow.backend.dto.DisponibilidadResponse;
import com.parkflow.backend.dto.EntradaRequest;
import com.parkflow.backend.dto.TicketResponse;
import com.parkflow.backend.service.ParqueaderoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parqueadero")
@CrossOrigin(origins = "*") // Permite que Angular y el Agente IA se conecten sin bloqueo CORS
public class ParqueaderoController {

    private final ParqueaderoService parqueaderoService;

    public ParqueaderoController(ParqueaderoService parqueaderoService) {
        this.parqueaderoService = parqueaderoService;
    }

    @GetMapping("/disponibilidad/{tipoVehiculo}")
    public ResponseEntity<DisponibilidadResponse> obtenerDisponibilidad(@PathVariable String tipoVehiculo) {
        return ResponseEntity.ok(parqueaderoService.consultarDisponibilidad(tipoVehiculo));
    }

    @PostMapping("/entradas")
    public ResponseEntity<TicketResponse> registrarEntrada(@Valid @RequestBody EntradaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parqueaderoService.registrarEntrada(request));
    }

    @PostMapping("/salidas/{placa}")
    public ResponseEntity<TicketResponse> registrarSalida(@PathVariable String placa) {
        return ResponseEntity.ok(parqueaderoService.registrarSalida(placa));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<TicketResponse>> listarVehiculosActivos() {
        return ResponseEntity.ok(parqueaderoService.listarActivos());
    }

    @GetMapping("/historial")
    public ResponseEntity<List<TicketResponse>> listarHistorialSalidas() {
        return ResponseEntity.ok(parqueaderoService.listarHistorialSalidas());
    }

    @GetMapping("/tarifas/{tipoVehiculo}")
    public ResponseEntity<com.parkflow.backend.dto.TarifaResponse> obtenerTarifa(@PathVariable String tipoVehiculo) {
        return ResponseEntity.ok(parqueaderoService.consultarTarifa(tipoVehiculo));
    }

    @PostMapping("/reservas")
    public ResponseEntity<com.parkflow.backend.model.Reserva> crearReserva(@Valid @RequestBody com.parkflow.backend.dto.ReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parqueaderoService.crearReserva(request));
    }

}
