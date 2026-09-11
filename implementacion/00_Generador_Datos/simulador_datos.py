import json
import random
from datetime import datetime, timedelta

# ==============================================================================
# SIMULADOR ESTOCÁSTICO DE PATRONES DE NAVEGACIÓN (Generador Sintético)
# ERP BASE: Gestión de Artículos (Roles Reales extraídos del Frontend)
# ==============================================================================

ARCHIVO_SALIDA = "logs_sinteticos_ppi.ndjson"
NUM_SESIONES_A_GENERAR = 50000

FECHA_INICIO = datetime(2026, 1, 1, 8, 0, 0)
DIAS_SIMULACION = 90

# 1. Definición de Arquetipos (2 usuarios fijos por cada rol del ERP)
# Se han expandido las rutas para aprovechar todas las ramas del menú de cada rol
ARQUETIPOS = {
    # ------------------ ROL: VENTAS ------------------
    "Vendedor_Experto": {
        "user_id": "us_vta_001", "rol": "Ventas", "prob_aparicion": 0.20,
        "horas_trabajo": (8, 16), "tiempo_lectura": (0.5, 0.1), "prob_error": 0.05,
        "rutas": [
            ["/dashboard", "/ventas", "/ventas/registro", "/ventas/pago", "/ventas/exito"],
            ["/dashboard", "/ventas", "/ventas/descuentos", "/ventas/registro", "/ventas/exito"],
            ["/dashboard", "/procesos", "/procesos/movimientos"], # Revisa stock
            ["/dashboard", "/documentos", "/documentos/bloque", "/dashboard"] # Revisa facturas emitidas
        ]
    },
    "Vendedor_Nuevo": {
        "user_id": "us_vta_002", "rol": "Ventas", "prob_aparicion": 0.10,
        "horas_trabajo": (10, 18), "tiempo_lectura": (3.0, 1.0), "prob_error": 0.25,
        "rutas": [
            ["/dashboard", "/ventas", "/dashboard", "/ventas", "/ventas/registro"],
            ["/dashboard", "/ventas", "/ventas/descuentos", "/dashboard"],
            ["/dashboard", "/mi-perfil", "/dashboard"] # Navegación perdida
        ]
    },
    
    # ------------------ ROL: COMPRAS ------------------
    "Comprador_Rutinario": {
        "user_id": "us_cmp_001", "rol": "Compras", "prob_aparicion": 0.15,
        "horas_trabajo": (9, 17), "tiempo_lectura": (1.2, 0.3), "prob_error": 0.08,
        "rutas": [
            ["/dashboard", "/compra", "/compra/orden", "/compra/orden/aprobar"],
            ["/dashboard", "/compra", "/compra/registro", "/compra/registro/guardar"],
            ["/dashboard", "/configuraciones", "/configuraciones/catalogo", "/dashboard"] # Consulta de proveedores
        ]
    },
    "Comprador_Lento": {
        "user_id": "us_cmp_002", "rol": "Compras", "prob_aparicion": 0.10,
        "horas_trabajo": (14, 20), "tiempo_lectura": (3.5, 0.8), "prob_error": 0.20,
        "rutas": [
            ["/dashboard", "/compra", "/compra/registro", "/compra/orden", "/compra/registro"],
            ["/dashboard", "/reportes", "/reportes/compras", "/dashboard"]
        ]
    },
    
    # ------------------ ROL: ALMACÉN ------------------
    "Almacenero_Rapido": {
        "user_id": "us_alm_001", "rol": "Almacen", "prob_aparicion": 0.15,
        "horas_trabajo": (7, 15), "tiempo_lectura": (0.7, 0.2), "prob_error": 0.05,
        "rutas": [
            ["/dashboard", "/procesos", "/procesos/movimientos", "/procesos/movimientos/mixto"],
            ["/dashboard", "/configuraciones", "/configuraciones/puntos", "/dashboard"],
            ["/dashboard", "/procesos", "/procesos/movimientos", "/procesos/movimientos/detalle"]
        ]
    },
    "Almacenero_Auditor": {
        "user_id": "us_alm_002", "rol": "Almacen", "prob_aparicion": 0.10,
        "horas_trabajo": (12, 21), "tiempo_lectura": (2.5, 0.6), "prob_error": 0.10,
        "rutas": [
            ["/dashboard", "/configuraciones", "/configuraciones/catalogo", "/atributos", "/configuraciones/catalogo"],
            ["/dashboard", "/configuraciones", "/configuraciones/catalogo", "/unidad-medida", "/dashboard"],
            ["/dashboard", "/reportes", "/reportes/inventario"]
        ]
    },
    
    # ------------------ ROL: ADMINISTRADOR ------------------
    "Admin_Seguridad": {
        "user_id": "us_adm_001", "rol": "Administrador", "prob_aparicion": 0.10,
        "horas_trabajo": (9, 12), "tiempo_lectura": (0.6, 0.2), "prob_error": 0.02,
        "rutas": [
            ["/dashboard", "/configuraciones", "/configuraciones/seguridad", "/configuraciones/seguridad/roles"],
            ["/dashboard", "/configuraciones", "/configuraciones/seguridad", "/configuraciones/seguridad/permisos"],
            ["/dashboard", "/configuraciones", "/configuraciones/seguridad", "/configuraciones/seguridad/usuarios"]
        ]
    },
    "Admin_Configurador": {
        "user_id": "us_adm_002", "rol": "Administrador", "prob_aparicion": 0.10,
        "horas_trabajo": (15, 19), "tiempo_lectura": (1.5, 0.4), "prob_error": 0.05,
        "rutas": [
            ["/dashboard", "/documentos", "/documentos/formato", "/configuraciones/sucursales", "/documentos/bloque"],
            ["/dashboard", "/documentos", "/documentos/punto-documento", "/dashboard"],
            ["/dashboard", "/configuraciones", "/configuraciones/sucursales", "/configuraciones/sucursales/editar"]
        ]
    }
}

def obtener_arquetipo_aleatorio():
    rand = random.random()
    acumulado = 0
    for key, arq in ARQUETIPOS.items():
        acumulado += arq["prob_aparicion"]
        if rand <= acumulado:
            return key, arq
    return list(ARQUETIPOS.items())[0]

def generar_tiempo_clic(media, desv_estandar):
    return max(0.1, random.gauss(media, desv_estandar))

def generar_fecha_aleatoria(horas_permitidas):
    dia_random = random.randint(0, DIAS_SIMULACION)
    hora_random = random.randint(horas_permitidas[0], horas_permitidas[1] - 1)
    minuto_random, segundo_random = random.randint(0, 59), random.randint(0, 59)
    return FECHA_INICIO + timedelta(days=dia_random, hours=hora_random, minutes=minuto_random, seconds=segundo_random)

def simular_sesiones():
    logs = []
    print(f"Simulando {NUM_SESIONES_A_GENERAR} sesiones para el ERP de Artículos...")
    
    for i in range(NUM_SESIONES_A_GENERAR):
        nombre_arq, arq = obtener_arquetipo_aleatorio()
        session_id = f"sess_{i}_{datetime.now().microsecond}"
        
        tiempo_actual = generar_fecha_aleatoria(arq["horas_trabajo"])
        ruta_base = random.choice(arq["rutas"])
        ruta_anterior = ""
        
        for paso_esperado in ruta_base:
            if random.random() < arq["prob_error"]:
                # Simula un error y regreso
                error_ruta = "/dashboard" if random.random() < 0.5 else ruta_anterior
                espera_error = generar_tiempo_clic(arq["tiempo_lectura"][0] * 1.5, arq["tiempo_lectura"][1])
                tiempo_actual += timedelta(seconds=espera_error)
                
                logs.append({
                    "timestamp": tiempo_actual.isoformat() + "Z",
                    "user_id": arq["user_id"],
                    "role": arq["rol"],
                    "session_id": session_id,
                    "elemento_texto": "Boton Retorno",
                    "route": error_ruta,
                    "from_route": ruta_anterior,
                    "tipo_evento": "clic_navegacion"
                })
                ruta_anterior = error_ruta
                tiempo_actual += timedelta(seconds=generar_tiempo_clic(1.5, 0.5))

            # Flujo correcto
            espera = generar_tiempo_clic(arq["tiempo_lectura"][0], arq["tiempo_lectura"][1])
            tiempo_actual += timedelta(seconds=espera)
            
            logs.append({
                "timestamp": tiempo_actual.isoformat() + "Z",
                "user_id": arq["user_id"],
                "role": arq["rol"],
                "session_id": session_id,
                "elemento_texto": paso_esperado.split('/')[-1].capitalize() or "Home",
                "route": paso_esperado,
                "from_route": ruta_anterior,
                "tipo_evento": "clic_navegacion"
            })
            ruta_anterior = paso_esperado
            
    logs.sort(key=lambda x: x["timestamp"])
    
    with open(ARCHIVO_SALIDA, 'w', encoding='utf-8') as f:
        for log in logs:
            f.write(json.dumps(log) + '\n')
            
    print(f"✅ ¡Simulación completa! {len(logs)} logs guardados en {ARCHIVO_SALIDA}")

if __name__ == "__main__":
    simular_sesiones()
