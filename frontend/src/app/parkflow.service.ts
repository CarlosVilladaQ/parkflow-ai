import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Disponibilidad {
  tipoVehiculo: string;
  cuposTotales: number;
  cuposOcupados: number;
  cuposReservados: number;
  cuposDisponibles: number;
}

export interface Ticket {
  id: number;
  placa: string;
  tipoVehiculo: string;
  horaEntrada: string;
  horaSalida: string | null;
  totalPagado: number | null;
  estado: string;
}

@Injectable({ providedIn: 'root' })
export class ParkflowService {
  private http = inject(HttpClient);
  private backendUrl = 'http://localhost:8080/api/parqueadero';
  private agentUrl = 'http://localhost:8000/api/chat';

  obtenerDisponibilidad(tipo: string): Observable<Disponibilidad> {
    return this.http.get<Disponibilidad>(`${this.backendUrl}/disponibilidad/${tipo}`);
  }

  listarActivos(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.backendUrl}/activos`);
  }

  listarHistorial(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.backendUrl}/historial`);
  }

  registrarEntrada(placa: string, tipoVehiculo?: string): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.backendUrl}/entradas`, { placa, tipoVehiculo });
  }

  registrarSalida(placa: string): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.backendUrl}/salidas/${placa}`, {});
  }

  enviarMensajeAgente(mensaje: string, historial: { emisor: string; texto: string }[]): Observable<{ respuesta: string; modelo_usado: string }> {
    return this.http.post<{ respuesta: string; modelo_usado: string }>(this.agentUrl, { mensaje, historial });
  }

}
