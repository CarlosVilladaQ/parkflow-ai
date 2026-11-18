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

class ChatRequest(BaseModel):
    mensaje: str

@app.get("/api/modelos")
def listar_modelos():
    return [m.name for m in client.models.list() if "flash" in m.name]


@app.post("/api/chat")
def conversar_con_agente(req: ChatRequest):
    modelos_fallback = ["gemini-3.1-flash-lite", "gemini-2.5-flash-lite", "gemini-3.5-flash"]
    ultimo_error = ""

    for modelo in modelos_fallback:
        try:
            response = client.models.generate_content(
                model=modelo,
                contents=req.mensaje,
                config=types.GenerateContentConfig(
                    system_instruction=SYSTEM_INSTRUCTION,
                    tools=[consultar_disponibilidad, consultar_tarifa, crear_reserva],
                    temperature=0.1,
                ),
            )
            return {"respuesta": response.text, "modelo_usado": modelo}
        except Exception as e:
            ultimo_error = str(e)
            if "503" in ultimo_error or "UNAVAILABLE" in ultimo_error:
                continue  # Si hay alta demanda, salta al siguiente modelo automáticamente
            raise HTTPException(status_code=500, detail=ultimo_error)

    raise HTTPException(status_code=503, detail=f"Todos los modelos están congestionados: {ultimo_error}")