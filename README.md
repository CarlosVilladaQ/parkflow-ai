# ParkFlow AI

Sistema de gestión operativa para parqueaderos con control de cupos en tiempo real, liquidación automatizada de tarifas y un asistente conversacional para consultas y reservas.

El proyecto nace de la necesidad real de agilizar el control de entrada/salida de vehículos y reducir el tiempo dedicado a responder consultas frecuentes sobre disponibilidad y precios por canales digitales.

---

## Arquitectura y Stack

La solución está dividida en tres servicios independientes sobre una base de datos relacional:

```text
[ Angular (Frontend) ] ──► [ Spring Boot (API REST) ] ──► [ PostgreSQL ]
                                      ▲
                                      │ (Tool Calling)
                           [ FastAPI + Gemini SDK ]
```

* **Backend (`backend/`):** Java 21, Spring Boot, Spring Data JPA, Bean Validation, JUnit 5 y Mockito.
* **Frontend (`frontend/`):** Angular (Standalone Components, Signals), TypeScript y CSS.
* **Servicio de Agente (`ai-agent/`):** Python, FastAPI y Google GenAI SDK.
* **Base de Datos (`database/`):** PostgreSQL.

---

## Decisiones Técnicas Clave

* **Validación financiera en el servidor:** El modelo de lenguaje nunca calcula tarifas ni decide si hay cupos. Toda consulta o reserva pasa por herramientas (*Function Calling*) conectadas directamente a los endpoints de Spring Boot, evitando inconsistencias en cobros o sobreventa de espacios.
* **Cálculo de tarifas y pruebas unitarias:** La lógica de cobro soporta fracciones de 15 minutos, tarifa por hora y tope máximo diario, validada mediante pruebas unitarias en `ParqueaderoServiceTest` con zona horaria `America/Bogota`.
* **Contratos y manejo de errores:** Uso de Java `Records` como DTOs para desacoplar la capa de persistencia y respuestas de error estandarizadas mediante `@RestControllerAdvice` (RFC 7807).
* **Tolerancia a fallos en el agente:** El servicio en Python mantiene contexto de conversación e implementa rotación automática de modelos (*fallback*) en caso de saturación o indisponibilidad temporal de la API externa.

---

## Instalación y Ejecución Local

### 1. Base de datos
Crea una base de datos llamada `parkflow_db` en PostgreSQL y ejecuta el script `database/init.sql` para generar las tablas (`configuracion_parqueadero`, `tarifas`, `tickets`, `reservas`) y la carga inicial de tarifas.

### 2. Backend (Puerto 8080)
Configura tus credenciales locales en `backend/src/main/resources/application.properties` y ejecuta:
```bash
cd backend
./mvnw spring-boot:run
```
Para correr las pruebas unitarias:
```bash
./mvnw test
```

### 3. Servicio de Agente IA (Puerto 8000)
Crea un archivo `.env` dentro de `ai-agent/` con tu clave `GEMINI_API_KEY` y ejecuta:
```bash
cd ai-agent
python -m venv venv
# En Windows: .\venv\Scripts\activate | En Linux/Mac: source venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --port 8000
```

### 4. Frontend (Puerto 4200)
```bash
cd frontend
npm install
ng serve
```
Abre `http://localhost:4200` en tu navegador.
g