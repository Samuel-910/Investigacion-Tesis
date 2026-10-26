"""
SIMULADOR ESTOCÁSTICO DE NAVEGACIÓN — MENÚ REAL DEL ERP
=======================================================

Versión del generador de implementacion/00_Generador_Datos/simulador_datos.py,
pero usando SOLO las 60 funciones reales del menú del ERP (catalog.json de la
app del estudio) y los 4 roles del estudio (Ventas, Compras, Almacen, Administrador).

Qué simula (igual que la app del estudio):
  - Menú acordeón: abrir una función cuesta 2 clics (grupo → ítem) o 3 clics
    (grupo → submenú → ítem). Solo se ven los grupos permitidos al rol (RBAC).
  - Cada usuario tiene un perfil de uso personal tipo Zipf sobre sus funciones
    (igual que personalProfile() de la app) + flujos de trabajo típicos de su
    arquetipo (p. ej. Clientes → Registrar venta → Comprobantes electrónicos).
  - Errores de navegación: visitas a una función equivocada antes de llegar a la
    correcta (más frecuentes con menos experiencia y en ítems más profundos).
  - Tiempos: búsqueda según la posición del ítem en el menú estático + tiempo en
    la función; algunos accesos duran < 3 s (accidentales, OE1 los filtra).

Salida: mismo formato que estudio-menus/tools/export_logs.py (logs_navegacion.ndjson),
así OE1 puede mezclar logs sintéticos y reales sin cambios.

  datos_sinteticos/logs_sinteticos_erp.ndjson
  datos_sinteticos/participantes_sinteticos.csv
  datos_sinteticos/task_results_sinteticos.csv

Uso:
  python simulador_erp_real.py [--usuarios-por-rol 50] [--sesiones 30] [--semilla 42]
"""
import argparse
import csv
import json
import math
import random
from datetime import datetime, timedelta, timezone
from pathlib import Path

AQUI = Path(__file__).resolve().parent
RAIZ = AQUI.parent
CONFIG_ERP = RAIZ / "erp_config_universal.json"
SALIDA_DIR = RAIZ / "datos_sinteticos"

FECHA_INICIO = datetime(2026, 6, 1, tzinfo=timezone.utc)

# ──────────────────────────────────────────────────────────────────────────────
# 1. NIVELES DE EXPERIENCIA (mismos valores que la app: ninguna/basica/frecuente)
# ──────────────────────────────────────────────────────────────────────────────
EXPERIENCIA = {
    #             prob   seg/clic (media, sd)  seg/ítem escaneado  prob_error  confirmar (s)
    "ninguna":   {"p": 0.30, "clic": (2.2, 0.6), "scan": 0.45, "error": 0.22, "confirmar": (4.0, 1.5)},
    "basica":    {"p": 0.40, "clic": (1.5, 0.4), "scan": 0.30, "error": 0.12, "confirmar": (3.0, 1.0)},
    "frecuente": {"p": 0.30, "clic": (0.9, 0.25), "scan": 0.18, "error": 0.05, "confirmar": (2.2, 0.7)},
}

# ──────────────────────────────────────────────────────────────────────────────
# 2. ARQUETIPOS POR ROL — solo rutas reales del menú del ERP
#    "nucleo":  funciones principales en orden de importancia típica
#    "flujos":  cadenas de trabajo (se recorren en orden)
# ──────────────────────────────────────────────────────────────────────────────
ARQUETIPOS = {
    # ------------------------------ VENTAS (g5, g6, g3, g8) -------------------
    "Vendedor_Mostrador": {
        "rol": "Ventas", "peso": 0.6, "horas": (8, 17),
        "nucleo": ["venta/venta", "venta/clientes", "almacen/productos", "venta/cotizaciones",
                   "venta/listado", "venta/comprobante-electronico", "venta/descuentos/lista",
                   "venta/devoluciones"],
        "flujos": [
            ["venta/clientes", "venta/venta", "venta/comprobante-electronico"],
            ["venta/cotizaciones", "venta/venta"],
            ["almacen/productos", "venta/venta"],
            ["venta/listado", "venta/devoluciones", "venta/notas-credito"],
            ["venta/descuentos/lista", "venta/venta"],
        ],
    },
    "Vendedor_Supervisor": {
        "rol": "Ventas", "peso": 0.4, "horas": (9, 19),
        "nucleo": ["venta/listado", "reportes/ventas", "venta/comprobante-electronico",
                   "venta/anulaciones", "venta/notas-credito", "facturacion/resumenes",
                   "venta/descuentos/convenio", "reportes/descuento"],
        "flujos": [
            ["venta/listado", "venta/anulaciones", "facturacion/anulaciones"],
            ["venta/comprobante-electronico", "facturacion/resumenes"],
            ["reportes/ventas", "reportes/descuento"],
            ["venta/descuentos/lista", "venta/descuentos/convenio"],
            ["venta/listado", "venta/notas-credito", "reportes/nota-credito"],
        ],
    },
    # ------------------------------ COMPRAS (g4, g3, g8) ----------------------
    "Comprador_Rutinario": {
        "rol": "Compras", "peso": 0.6, "horas": (8, 17),
        "nucleo": ["compra/orden", "compra/registrar", "compra/proveedores", "almacen/productos",
                   "compra/intercambio", "reportes/compras"],
        "flujos": [
            ["compra/proveedores", "compra/orden", "compra/registrar"],
            ["almacen/productos", "compra/orden"],
            ["compra/registrar", "compra/intercambio"],
            ["compra/orden", "compra/registrar", "reportes/compras"],
        ],
    },
    "Comprador_Financiero": {
        "rol": "Compras", "peso": 0.4, "horas": (9, 18),
        "nucleo": ["compra/pagos-pendientes", "compra/estado-cuenta", "compra/proveedores",
                   "reportes/compras", "compra/registrar", "reportes/stock-valorizado"],
        "flujos": [
            ["compra/pagos-pendientes", "compra/estado-cuenta"],
            ["compra/proveedores", "compra/estado-cuenta", "compra/pagos-pendientes"],
            ["reportes/compras", "reportes/stock-valorizado"],
        ],
    },
    # ------------------------------ ALMACÉN (g8, g2, g3) ----------------------
    "Almacenero_Operativo": {
        "rol": "Almacen", "peso": 0.6, "horas": (7, 16),
        "nucleo": ["procesos/historial-movimientos", "almacen/productos", "procesos/transferencias",
                   "almacen/almacenes", "procesos/detalle-documentos", "procesos/atributos-diverso"],
        "flujos": [
            ["almacen/productos", "procesos/historial-movimientos"],
            ["almacen/almacenes", "procesos/transferencias", "procesos/detalle-documentos"],
            ["procesos/historial-movimientos", "procesos/detalle-documentos"],
            ["almacen/productos", "procesos/atributos-diverso"],
        ],
    },
    "Almacenero_Auditor": {
        "rol": "Almacen", "peso": 0.4, "horas": (10, 19),
        "nucleo": ["reportes/kardex", "reportes/stock-valorizado", "almacen/productos",
                   "procesos/historial-movimientos", "procesos/reimprimir-comprobantes",
                   "reportes/correlatividad"],
        "flujos": [
            ["almacen/productos", "reportes/kardex", "reportes/stock-valorizado"],
            ["procesos/historial-movimientos", "reportes/kardex"],
            ["procesos/detalle-documentos", "procesos/reimprimir-comprobantes"],
        ],
    },
    # ------------------------------ ADMINISTRADOR (g1..g9) --------------------
    "Admin_Seguridad": {
        "rol": "Administrador", "peso": 0.35, "horas": (8, 13),
        "nucleo": ["configuracion/seguridad/users", "configuracion/seguridad/roles",
                   "configuracion/seguridad/permisos", "configuracion/seguridad/admin",
                   "configuracion/seguridad/cambio-clave", "configuracion/sucursales"],
        "flujos": [
            ["configuracion/seguridad/users", "configuracion/seguridad/roles",
             "configuracion/seguridad/permisos"],
            ["configuracion/seguridad/roles", "configuracion/seguridad/permisos"],
            ["configuracion/seguridad/users", "configuracion/seguridad/cambio-clave"],
        ],
    },
    "Admin_Configurador": {
        "rol": "Administrador", "peso": 0.35, "horas": (9, 18),
        "nucleo": ["configuracion/sucursales", "configuracion/puntos-venta", "documentos/formatos",
                   "configuracion/puntos-atributos", "documentos", "documentos/bloques",
                   "documentos/puntos", "configuracion/empresas", "configuracion/catalogo-productos"],
        "flujos": [
            ["configuracion/sucursales", "configuracion/puntos-venta", "configuracion/puntos-atributos"],
            ["documentos", "documentos/bloques", "documentos/formatos"],
            ["configuracion/puntos-venta", "documentos/puntos"],
            ["configuracion/catalogo-productos", "configuracion/atributos-producto"],
        ],
    },
    "Admin_Finanzas": {
        "rol": "Administrador", "peso": 0.30, "horas": (14, 20),
        "nucleo": ["caja/arqueos", "reportes/cajas", "caja/general", "caja/chica",
                   "caja/medio-pago", "caja/cuentas", "procesos/aprobaciones", "caja/diferencia-costos"],
        "flujos": [
            ["caja/arqueos", "caja/general", "reportes/cajas"],
            ["caja/chica", "caja/general"],
            ["procesos/aprobaciones", "reportes/documentos-anulados"],
            ["caja/cuentas", "caja/medio-pago"],
        ],
    },
}


# ──────────────────────────────────────────────────────────────────────────────
# 3. MENÚ REAL (desde erp_config_universal.json, generado por construir_config_erp.py)
# ──────────────────────────────────────────────────────────────────────────────
class MenuERP:
    def __init__(self, path: Path):
        cfg = json.loads(path.read_text(encoding="utf-8"))
        self.menu = cfg["MENU_ERP"]
        self.permisos = cfg["PERMISOS_ROL"]
        self.tareas = cfg["TAREAS_ROL"]
        self.info = {}  # ruta -> {grupo, titulo, pos_grupo, pos_item, clics}
        for g in self.menu:
            for it in g["children"]:
                self.info[it["ruta"]] = {
                    "grupo": g["id"], "titulo": it["titulo"],
                    "pos_item": it["orden_default"], "clics": 3 if it.get("submenu") else 2,
                    "submenu": it.get("submenu"),
                }

    def rutas_rol(self, rol):
        ok = set(self.permisos[rol])
        return [r for r, i in self.info.items() if i["grupo"] in ok]

    def pos_grupo(self, rol, ruta):
        """Posición del grupo de la ruta dentro del menú estático que ve el rol."""
        visibles = [g["id"] for g in self.menu if g["id"] in set(self.permisos[rol])]
        return visibles.index(self.info[ruta]["grupo"]) + 1

    def validar(self):
        errores = []
        for nom, a in ARQUETIPOS.items():
            permitidas = set(self.rutas_rol(a["rol"]))
            for r in a["nucleo"] + [x for f in a["flujos"] for x in f]:
                if r not in self.info:
                    errores.append(f"{nom}: '{r}' no existe en el menú del ERP")
                elif r not in permitidas:
                    errores.append(f"{nom}: '{r}' no está permitida para el rol {a['rol']}")
        if errores:
            raise ValueError("Arquetipos inválidos:\n  " + "\n  ".join(errores))


# ──────────────────────────────────────────────────────────────────────────────
# 4. USUARIO SINTÉTICO
# ──────────────────────────────────────────────────────────────────────────────
def crear_usuario(uid, nombre_arq, menu: MenuERP, rnd: random.Random):
    arq = ARQUETIPOS[nombre_arq]
    rol = arq["rol"]
    exp = rnd.choices(list(EXPERIENCIA), weights=[e["p"] for e in EXPERIENCIA.values()])[0]

    # Funciones principales: núcleo del arquetipo + tareas del rol en la app (sin repetir)
    nucleo = list(dict.fromkeys(arq["nucleo"] + menu.tareas.get(rol, [])))
    # Perturbación personal: cada usuario intercambia algunas posiciones (perfil propio)
    for _ in range(rnd.randint(1, 4)):
        i = rnd.randrange(len(nucleo))
        j = min(len(nucleo) - 1, max(0, i + rnd.choice([-2, -1, 1, 2])))
        nucleo[i], nucleo[j] = nucleo[j], nucleo[i]

    s = rnd.uniform(0.9, 1.4)  # exponente Zipf (la app usa s = 1)
    pesos = {r: 1 / (k + 1) ** s for k, r in enumerate(nucleo)}

    # Funciones secundarias: el resto de lo permitido, con poca probabilidad total
    secundarias = [r for r in menu.rutas_rol(rol) if r not in pesos]
    p_sec = rnd.uniform(0.04, 0.12)
    tot = sum(pesos.values())
    perfil = {r: (w / tot) * (1 - p_sec) for r, w in pesos.items()}
    for r in secundarias:
        perfil[r] = p_sec / len(secundarias)

    return {
        "user_id": uid, "rol": rol, "arquetipo": nombre_arq, "experiencia": exp,
        "perfil": perfil,
        "flujos": arq["flujos"],
        "prob_flujo": rnd.uniform(0.35, 0.65),
        "horas": arq["horas"],
        "zipf_s": round(s, 3),
    }


def elegir(perfil, rnd, excluir=None):
    rutas = [r for r in perfil if r != excluir]
    return rnd.choices(rutas, weights=[perfil[r] for r in rutas])[0]


def secuencia_tareas(u, n, rnd):
    """Secuencia de funciones de una sesión: mezcla de flujos de trabajo y perfil Zipf."""
    out, flujo = [], []
    while len(out) < n:
        if flujo:
            r = flujo.pop(0)
        elif rnd.random() < u["prob_flujo"]:
            flujo = list(rnd.choice(u["flujos"]))
            r = flujo.pop(0)
        else:
            r = elegir(u["perfil"], rnd, excluir=out[-1] if out else None)
        if out and r == out[-1]:  # la app evita repetir la misma tarea seguida
            continue
        out.append(r)
    return out


def gauss_pos(rnd, media_sd, minimo=0.2):
    return max(minimo, rnd.gauss(*media_sd))


def ruta_equivocada(ruta, u, menu: MenuERP, rnd):
    """Función equivocada: 65 % un ítem vecino del mismo grupo, 35 % otro grupo permitido."""
    grupo = menu.info[ruta]["grupo"]
    permitidas = menu.rutas_rol(u["rol"])
    mismo = [r for r in permitidas if menu.info[r]["grupo"] == grupo and r != ruta]
    otros = [r for r in permitidas if menu.info[r]["grupo"] != grupo]
    if mismo and (rnd.random() < 0.65 or not otros):
        pos = menu.info[ruta]["pos_item"]
        mismo.sort(key=lambda r: abs(menu.info[r]["pos_item"] - pos))
        return rnd.choice(mismo[:3])
    return rnd.choice(otros) if otros else ruta


# ──────────────────────────────────────────────────────────────────────────────
# 5. SIMULACIÓN
# ──────────────────────────────────────────────────────────────────────────────
def simular(args):
    rnd = random.Random(args.semilla)
    menu = MenuERP(CONFIG_ERP)
    menu.validar()

    # Usuarios: N por rol, repartidos entre los arquetipos del rol según su peso
    usuarios, k = [], 1
    for rol in menu.permisos:
        arqs = [n for n, a in ARQUETIPOS.items() if a["rol"] == rol]
        for _ in range(args.usuarios_por_rol):
            nom = rnd.choices(arqs, weights=[ARQUETIPOS[n]["peso"] for n in arqs])[0]
            usuarios.append(crear_usuario(f"SIM{k:04d}", nom, menu, rnd))
            k += 1

    logs, tareas = [], []
    for u in usuarios:
        ex = EXPERIENCIA[u["experiencia"]]
        n_ses = max(5, int(rnd.gauss(args.sesiones, args.sesiones * 0.2)))
        dias = sorted(rnd.sample(range(args.dias), min(n_ses, args.dias)))
        aprendizaje = 1.0
        for s_idx, dia in enumerate(dias, start=1):
            h0, h1 = u["horas"]
            t = FECHA_INICIO + timedelta(days=dia, hours=rnd.randint(h0, h1 - 1),
                                         minutes=rnd.randint(0, 59), seconds=rnd.randint(0, 59))
            sid = f"{u['user_id']}-S{s_idx:02d}-B1"
            prev = None
            for t_idx, objetivo in enumerate(secuencia_tareas(u, rnd.randint(*args.tareas), rnd)):
                inf = menu.info[objetivo]
                # Tiempo de búsqueda: clics necesarios + ítems escaneados hasta encontrarlo
                escaneo = menu.pos_grupo(u["rol"], objetivo) + inf["pos_item"]
                t_busqueda = (inf["clics"] * gauss_pos(rnd, ex["clic"]) +
                              escaneo * ex["scan"] * rnd.lognormvariate(0, 0.35)) * aprendizaje
                t += timedelta(seconds=t_busqueda)

                # Errores: más probables en ítems profundos y lejanos
                p_err = ex["error"] * (1.3 if inf["clics"] == 3 else 1.0) * (0.8 + escaneo / 20) * aprendizaje
                visitas_mal, clics = 0, inf["clics"]
                while rnd.random() < p_err and visitas_mal < 3:
                    mal = ruta_equivocada(objetivo, u, menu, rnd)
                    logs.append(_evento(t, u, sid, mal, prev))
                    prev = mal
                    visitas_mal += 1
                    clics += menu.info[mal]["clics"]
                    t += timedelta(seconds=rnd.uniform(1.0, 6.0))  # se da cuenta y vuelve al menú
                    p_err *= 0.4

                logs.append(_evento(t, u, sid, objetivo, prev))
                prev = objetivo
                t_conf = gauss_pos(rnd, ex["confirmar"], 0.8)
                duracion = t_busqueda + t_conf + visitas_mal * 3.5
                t += timedelta(seconds=t_conf)
                tareas.append({
                    "pid": u["user_id"], "session_no": s_idx, "block_no": 1, "condition": "static",
                    "task_index": t_idx, "target_route": objetivo, "practice": False,
                    "clicks": clics, "wrong_visits": visitas_mal, "wrong_confirms": 0,
                    "duration_ms": int(duracion * 1000), "completed": True,
                    "created_at": t.isoformat(timespec="milliseconds"),
                })
            # Efecto de aprendizaje entre sesiones (se estabiliza)
            aprendizaje = max(0.6, aprendizaje * 0.985)

    logs.sort(key=lambda x: (x["timestamp"], x["user_id"]))
    _guardar(logs, tareas, usuarios)
    return logs, tareas, usuarios


# ──────────────────────────────────────────────────────────────────────────────
# 6. MODO ESTUDIO — imita exactamente la sesión 1 de la app (menú estático)
#    Calibrado con el participante real P001 (Administrador, experiencia "ninguna"):
#      bloque 1: mediana 3 clics y ~15 s por tarea; bloque 2: 2 clics y ~10 s;
#      ~0.2 visitas erróneas por tarea; ~5 % confirmaciones erróneas;
#      tareas de práctica mucho más lentas (exploración del menú).
# ──────────────────────────────────────────────────────────────────────────────
ESTUDIO_EXP = {
    #             lectura instrucción (s)   prob. explorar grupos (práctica, bloque1, bloque2)   confirmación errónea
    "ninguna":   {"lectura": (4.0, 1.5), "explorar": (0.75, 0.35, 0.10), "conf_mal": 0.05},
    "basica":    {"lectura": (3.0, 1.0), "explorar": (0.55, 0.20, 0.06), "conf_mal": 0.03},
    "frecuente": {"lectura": (2.2, 0.7), "explorar": (0.30, 0.08, 0.03), "conf_mal": 0.01},
}


def perfil_estudio(tareas_rol, rnd):
    """Igual que personalProfile() de la app: permutación propia + Zipf s = 1."""
    rutas = tareas_rol[:]
    rnd.shuffle(rutas)
    w = [1 / (i + 1) for i in range(len(rutas))]
    tot = sum(w)
    return {r: x / tot for r, x in zip(rutas, w)}


def tareas_bloque(perfil, n, rnd):
    """Igual que generateTasks() de la app: sorteo por perfil, sin repetir la anterior."""
    rutas, pesos = list(perfil), list(perfil.values())
    out, prev = [], ""
    for _ in range(n):
        r = rnd.choices(rutas, weights=pesos)[0]
        for _ in range(10):
            if r != prev:
                break
            r = rnd.choices(rutas, weights=pesos)[0]
        out.append(r)
        prev = r
    return out


def simular_estudio(args):
    rnd = random.Random(args.semilla)
    menu = MenuERP(CONFIG_ERP)

    usuarios, k = [], 1
    for rol in menu.permisos:
        for _ in range(args.usuarios_por_rol):
            exp = rnd.choices(list(EXPERIENCIA), weights=[e["p"] for e in EXPERIENCIA.values()])[0]
            usuarios.append({
                "user_id": f"SIM{k:04d}", "rol": rol, "arquetipo": "Estudio", "experiencia": exp,
                "perfil": perfil_estudio(menu.tareas[rol], rnd), "prob_flujo": 0.0, "zipf_s": 1.0,
            })
            k += 1

    logs, tareas = [], []
    for u in usuarios:
        ex, ee = EXPERIENCIA[u["experiencia"]], ESTUDIO_EXP[u["experiencia"]]
        # Cada usuario hace su sesión 1 en un día y hora distintos (como en el estudio)
        t = FECHA_INICIO + timedelta(days=rnd.randrange(args.dias), hours=rnd.randint(8, 21),
                                     minutes=rnd.randint(0, 59))
        prev = None
        for bloque in range(1, args.bloques + 1):
            t += timedelta(seconds=rnd.uniform(5, 30))  # instrucciones del bloque
            sid = f"{u['user_id']}-S1-B{bloque}"
            ritmo = 1.0 if bloque == 1 else 0.7  # aprendizaje del bloque 1 al 2 (P001: 15 s -> 10 s)
            for t_idx, objetivo in enumerate(tareas_bloque(u["perfil"], args.tareas_por_bloque, rnd)):
                practica = bloque == 1 and t_idx < args.practica
                inf = menu.info[objetivo]
                t += timedelta(seconds=gauss_pos(rnd, ee["lectura"], 0.8) * ritmo)

                # Exploración: abre/cierra grupos antes de encontrar la función (no genera "nav")
                p_exp = ee["explorar"][0 if practica else (1 if bloque == 1 else 2)]
                extra = 0
                if rnd.random() < p_exp:
                    extra = min(20, int(rnd.expovariate(1 / 4)) + 2)
                escaneo = menu.pos_grupo(u["rol"], objetivo) + inf["pos_item"]
                t_busqueda = ((inf["clics"] + extra) * gauss_pos(rnd, ex["clic"]) +
                              escaneo * ex["scan"] * rnd.lognormvariate(0, 0.35)) * ritmo
                if practica:
                    t_busqueda *= rnd.uniform(1.5, 4.0)
                t += timedelta(seconds=t_busqueda)

                # Visitas a funciones equivocadas (sí generan "nav", igual que en la app)
                p_err = ex["error"] * (1.3 if inf["clics"] == 3 else 1.0) * (1.0 if bloque == 1 else 0.6)
                visitas_mal, clics = 0, inf["clics"] + extra
                while rnd.random() < p_err and visitas_mal < 8:
                    mal = ruta_equivocada(objetivo, u, menu, rnd)
                    if mal not in u["perfil"] and rnd.random() < 0.5:
                        mal = rnd.choice([r for r in u["perfil"] if r != objetivo])
                    logs.append(_evento(t, u, sid, mal, prev))
                    prev = mal
                    visitas_mal += 1
                    clics += menu.info[mal]["clics"]
                    t += timedelta(seconds=rnd.uniform(1.0, 6.0))
                    p_err *= 0.5

                logs.append(_evento(t, u, sid, objetivo, prev))
                prev = objetivo
                conf_mal = 1 if rnd.random() < ee["conf_mal"] else 0
                t_conf = gauss_pos(rnd, ex["confirmar"], 0.8) + conf_mal * rnd.uniform(2, 6)
                t += timedelta(seconds=t_conf)
                tareas.append({
                    "pid": u["user_id"], "session_no": 1, "block_no": bloque, "condition": "static",
                    "task_index": t_idx, "target_route": objetivo, "practice": practica,
                    "clicks": clics, "wrong_visits": visitas_mal, "wrong_confirms": conf_mal,
                    "duration_ms": int((t_busqueda + t_conf + visitas_mal * 3.5) * 1000), "completed": True,
                    "created_at": t.isoformat(timespec="milliseconds"),
                })

    logs.sort(key=lambda x: (x["timestamp"], x["user_id"]))
    _guardar(logs, tareas, usuarios)
    return logs, tareas, usuarios


def _evento(t, u, sid, ruta, prev):
    return {
        "timestamp": t.isoformat(timespec="milliseconds"),
        "user_id": u["user_id"],
        "role": u["rol"],
        "session_id": sid,
        "route": "/" + ruta,
        "from_route": ("/" + prev) if prev else None,
        "elemento_texto": "menu",
        "tipo_evento": "clic_navegacion",
        "condition": "static",
        "fuente": "sintetico",
    }


def _guardar(logs, tareas, usuarios):
    SALIDA_DIR.mkdir(parents=True, exist_ok=True)
    with open(SALIDA_DIR / "logs_sinteticos_erp.ndjson", "w", encoding="utf-8") as f:
        for e in logs:
            f.write(json.dumps(e, ensure_ascii=False) + "\n")

    with open(SALIDA_DIR / "task_results_sinteticos.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=list(tareas[0].keys()))
        w.writeheader()
        w.writerows(tareas)

    with open(SALIDA_DIR / "participantes_sinteticos.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["pid", "role", "arquetipo", "experience_level", "zipf_s", "prob_flujo", "top3_perfil"])
        for u in usuarios:
            top = sorted(u["perfil"].items(), key=lambda x: -x[1])[:3]
            w.writerow([u["user_id"], u["rol"], u["arquetipo"], u["experiencia"], u["zipf_s"],
                        round(u["prob_flujo"], 3), " | ".join(r for r, _ in top)])


def main():
    ap = argparse.ArgumentParser(description="Generador sintético con el menú real del ERP")
    ap.add_argument("--modo", choices=["estudio", "jornada"], default="estudio",
                    help="estudio = igual que la sesión 1 de la app (por defecto); "
                         "jornada = uso diario del ERP con arquetipos y flujos de trabajo")
    ap.add_argument("--usuarios-por-rol", type=int, default=None,
                    help="por defecto 150 (estudio) o 50 (jornada)")
    ap.add_argument("--semilla", type=int, default=42)
    ap.add_argument("--dias", type=int, default=90, help="días del periodo simulado")
    # modo estudio
    ap.add_argument("--bloques", type=int, default=2)
    ap.add_argument("--tareas-por-bloque", type=int, default=20)
    ap.add_argument("--practica", type=int, default=3, help="tareas de práctica al inicio del bloque 1")
    # modo jornada
    ap.add_argument("--sesiones", type=int, default=30, help="[jornada] sesiones promedio por usuario")
    ap.add_argument("--tareas", type=int, nargs=2, default=(12, 25), metavar=("MIN", "MAX"),
                    help="[jornada] tareas por sesión")
    args = ap.parse_args()
    if args.usuarios_por_rol is None:
        args.usuarios_por_rol = 150 if args.modo == "estudio" else 50

    print(f"Modo {args.modo}: simulando {args.usuarios_por_rol} usuarios por rol con el menú real del ERP...")
    logs, tareas, usuarios = (simular_estudio if args.modo == "estudio" else simular)(args)

    rutas = {e["route"] for e in logs}
    err = sum(t["wrong_visits"] for t in tareas)
    print(f"[OK] {len(usuarios)} usuarios | {len(tareas):,} tareas | {len(logs):,} navegaciones | "
          f"{len(rutas)} funciones distintas | visitas erróneas: {err:,} ({err / len(tareas):.1%} por tarea)")
    print(f"     -> {SALIDA_DIR}")


if __name__ == "__main__":
    main()
