# ParkFlow AI — Reglas de Arquitectura y Desarrollo

## Contexto del Proyecto
Sistema de gestión de parqueaderos con control de cupos, tarifas dinámicas y un agente conversacional para consultas y reservas.

## Stack Tecnológico
- **Backend:** Java 21, SpringBoot 3.x, Spring Data JPA, PostgreSQL.
- **Frontend:** Angular (Standalone Components).
- **Agente IA:** Python 3.11+, FastAPI, Function Calling / Tools.

## Reglas Estrictas de Ingeniería (Guardrails)
1. **Lógica de Cobros y Cupos:** Todo cálculo de tarifas y validación de disponibilidad se ejecuta exclusivamente en el Backend (Java). El agente de IA NUNCA calcula precios ni inventa disponibilidad.
2. **Arquitectura Backend:** Usar patrón Controlador -> Servicio -> Repositorio. Usar siempre DTOs (Records de Java), nunca exponer entidades JPA directamente.
3. **Pruebas Obligatorias:** Toda lógica en `TarifaService` o `ReservaService` debe tener pruebas unitarias en JUnit 5 cubriendo casos borde.
4. **Zona Horaria:** Usar estrictamente `America/Bogota` para todos los registros de entrada, salida y reservas.
