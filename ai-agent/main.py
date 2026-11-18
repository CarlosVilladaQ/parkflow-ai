import os
import requests
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from dotenv import load_dotenv
from google import genai
from google.genai import types

load_dotenv()

app = FastAPI(title="ParkFlow AI Agent")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8080/api/parqueadero")
client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))

# --- HERRAMIENTAS (TOOLS) CONECTADAS AL BACKEND JAVA ---

def consultar_disponibilidad(tipo_vehiculo: str) -> dict:
    """Consulta en tiempo real los cupos disponibles para CARRO o MOTO en el parqueadero."""
    resp = requests.get(f"{BACKEND_URL}/disponibilidad/{tipo_vehiculo.upper()}")
    return resp.json()

def consultar_tarifa(tipo_vehiculo: str) -> dict:
    """Obtiene las tarifas oficiales vigentes (hora, fracción de 15 min y día) para CARRO o MOTO."""
    resp = requests.get(f"{BACKEND_URL}/tarifas/{tipo_vehiculo.upper()}")
    return resp.json()

def crear_reserva(placa: str, tipo_vehiculo: str, cliente_telefono: str) -> dict:
    """Registra una reserva real en el sistema. Solo llamar cuando el usuario confirme placa, tipo y teléfono."""
    payload = {
        "placa": placa.upper(),
        "tipoVehiculo": tipo_vehiculo.upper(),
        "clienteTelefono": cliente_telefono
    }
    resp = requests.post(f"{BACKEND_URL}/reservas", json=payload)
    if resp.status_code != 201:
        return {"error": resp.json().get("detail", "No se pudo completar la reserva")}
    return resp.json()

# --- GUARDRAILS E INSTRUCCIONES DEL SISTEMA ---

SYSTEM_INSTRUCTION = """
Eres el asistente virtual oficial de 'Parqueadero Central ParkFlow' en Ibagué, Colombia.
Tu objetivo es ayudar a los conductores a consultar disponibilidad de cupos, conocer tarifas y hacer reservas.

REGLAS ESTRICTAS (GUARDRAILS):
1. NUNCA inventes tarifas ni disponibilidad. Usa SIEMPRE las herramientas `consultar_disponibilidad` y `consultar_tarifa`.
2. Los precios están en Pesos Colombianos (COP). Formatea los montos claramente (ej. $4.000 COP).
3. Antes de ejecutar `crear_reserva`, asegúrate de tener tres datos obligatorios del cliente: placa del vehículo, tipo de vehículo (CARRO o MOTO) y número de teléfono. Si falta alguno, pídelo amablemente.
4. Si una herramienta devuelve error o 0 cupos disponibles, informa al cliente con honestidad y no confirmes ninguna reserva.
5. Mantén tus respuestas breves, amables y profesionales.
"""

class MensajeHistorial(BaseModel):
    emisor: str
    texto: str

class ChatRequest(BaseModel):
    mensaje: str
    historial: list[MensajeHistorial] = []

@app.post("/api/chat")
def conversar_con_agente(req: ChatRequest):
    modelos_fallback = ["gemini-3.1-flash-lite", "gemini-3.5-flash-lite", "gemini-3.5-flash"]
    ultimo_error = ""

    # Construimos el contexto con los últimos mensajes para que el agente tenga memoria
    contexto_conversacion = ""
    for item in req.historial[-6:]:
        rol = "Cliente" if item.emisor == "usuario" else "Asistente"
        contexto_conversacion += f"{rol}: {item.texto}\n"
    prompt_completo = f"{contexto_conversacion}Cliente: {req.mensaje}"

    for modelo in modelos_fallback:
        try:
            response = client.models.generate_content(
                model=modelo,
                contents=prompt_completo,
                config=types.GenerateContentConfig(
                    system_instruction=SYSTEM_INSTRUCTION,
                    tools=[consultar_disponibilidad, consultar_tarifa, crear_reserva],
                    temperature=0.1,
                ),
            )
            texto_final = response.text or "Listo, operación procesada correctamente."
            return {"respuesta": texto_final, "modelo_usado": modelo}
                except Exception as e:
            ultimo_error = str(e)
            if any(code in ultimo_error for code in ["503", "UNAVAILABLE", "429", "404", "NOT_FOUND"]):
                continue
            return {"respuesta": "Tuve un inconveniente temporal procesando tu solicitud. Por favor intenta de nuevo.", "modelo_usado": modelo}


    return {"respuesta": "El servicio de IA tiene alta demanda en este instante. Por favor intenta en unos segundos.", "modelo_usado": "none"}
