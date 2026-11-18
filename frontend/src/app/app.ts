import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ParkflowService, Disponibilidad, Ticket } from './parkflow.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private parkflow = inject(ParkflowService);

  dispCarros = signal<Disponibilidad | null>(null);
  dispMotos = signal<Disponibilidad | null>(null);
  ticketsActivos = signal<Ticket[]>([]);

  nuevaPlaca = '';
  nuevoTipo = 'CARRO';
  alertaOperacion = signal('');
  errorOperacion = signal('');
  registrandoEntrada = signal(false);

  mensajeUsuario = '';
  cargandoChat = signal(false);
  mensajesChat = signal<{ emisor: 'usuario' | 'agente'; texto: string }[]>([
    { emisor: 'agente', texto: '¡Hola! Soy el asistente de ParkFlow AI. Pregúntame por cupos, tarifas o dime si quieres reservar un espacio.' }
  ]);

  ngOnInit(): void {
    this.recargarDatos();
  }

  recargarDatos(): void {
    this.parkflow.obtenerDisponibilidad('CARRO').subscribe(d => this.dispCarros.set(d));
    this.parkflow.obtenerDisponibilidad('MOTO').subscribe(d => this.dispMotos.set(d));
    this.parkflow.listarActivos().subscribe(t => this.ticketsActivos.set(t));
  }

  ingresarVehiculo(): void {
    if (!this.nuevaPlaca.trim() || this.registrandoEntrada()) return;
    this.registrandoEntrada.set(true);
    this.errorOperacion.set('');
    this.alertaOperacion.set('');

    this.parkflow.registrarEntrada(this.nuevaPlaca, this.nuevoTipo).subscribe({
      next: (ticket) => {
        this.alertaOperacion.set(`Ingreso registrado: ${ticket.placa} (${ticket.tipoVehiculo})`);
        this.nuevaPlaca = '';
        this.registrandoEntrada.set(false);
        this.recargarDatos();
      },
      error: (err) => {
        this.errorOperacion.set(err.error?.detail || 'Error al registrar el ingreso');
        this.registrandoEntrada.set(false);
      }
    });
  }

  darSalida(placa: string): void {
    this.errorOperacion.set('');
    this.parkflow.registrarSalida(placa).subscribe({
      next: (ticket) => {
        this.alertaOperacion.set(`Salida liquidada para ${ticket.placa}. Total cobrado: $${ticket.totalPagado} COP`);
        this.recargarDatos();
      },
      error: (err) => {
        this.errorOperacion.set(err.error?.detail || 'Error al liquidar salida');
      }
    });
  }

  enviarChat(): void {
    if (!this.mensajeUsuario.trim() || this.cargandoChat()) return;
    const texto = this.mensajeUsuario;
    const historialPrevio = this.mensajesChat();
    this.mensajesChat.update(lista => [...lista, { emisor: 'usuario', texto }]);
    this.mensajeUsuario = '';
    this.cargandoChat.set(true);

    this.parkflow.enviarMensajeAgente(texto, historialPrevio).subscribe({
      next: (res) => {
        this.mensajesChat.update(lista => [...lista, { emisor: 'agente', texto: res.respuesta }]);
        this.cargandoChat.set(false);
        this.recargarDatos();
      },
      error: () => {
        this.mensajesChat.update(lista => [...lista, { emisor: 'agente', texto: 'Error conectando con el agente de IA. Verifica que el servidor Python esté activo.' }]);
        this.cargandoChat.set(false);
      }
    });
  }
}
