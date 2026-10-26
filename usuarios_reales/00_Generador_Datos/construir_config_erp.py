"""Construye la configuración del pipeline a partir del menú REAL del ERP.

Fuente única: estudio-menus-app/estudio-menus/app/src/data/catalog.json
(el mismo menú que ven los participantes en la app del estudio).

Genera en usuarios_reales/:
  erp_menu_schema.json       {ruta: clics desde la raíz}  -> lo lee OE1 (métrica M1)
  erp_config_universal.json  MENU_ERP, GRUPOS_RUTAS, PERMISOS_ROL, TAREAS_ROL -> OE3 y el simulador

Profundidad (clics para abrir una función en el menú estático):
  grupo -> ítem              = 2 clics
  grupo -> submenú -> ítem   = 3 clics

Uso:
  python construir_config_erp.py [ruta/a/catalog.json]
"""
import json
import sys
from pathlib import Path

AQUI = Path(__file__).resolve().parent
RAIZ = AQUI.parent  # usuarios_reales/
CATALOGO_DEF = RAIZ.parent / "estudio-menus-app" / "estudio-menus" / "app" / "src" / "data" / "catalog.json"


def cargar_catalogo(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def hojas(nodo, nivel, submenu=None):
    """Devuelve las funciones (hojas) de un nodo con su profundidad en clics."""
    if "route" in nodo:
        return [{"id": nodo["id"], "titulo": nodo["title"], "ruta": nodo["route"],
                 "profundidad": nivel, "submenu": submenu}]
    out = []
    for c in nodo["children"]:
        out += hojas(c, nivel + 1, nodo["title"])
    return out


def construir(catalogo: dict) -> tuple[dict, dict]:
    gnum = {g["id"]: i + 1 for i, g in enumerate(catalogo["groups"])}  # g1 -> 1

    menu, grupos_rutas, profundidad = [], {}, {}
    for g in catalogo["groups"]:
        items = []
        for c in g["children"]:
            items += hojas(c, 2)  # grupo (1 clic) + ítem (1 clic) = 2
        children = []
        for j, it in enumerate(items):
            children.append({
                "id": int(it["id"].lstrip("i")),
                "titulo": it["titulo"],
                "tipo": "ITEM",
                "ruta": it["ruta"],
                "orden_default": j + 1,
                **({"submenu": it["submenu"]} if it["submenu"] else {}),
            })
            profundidad[it["ruta"]] = it["profundidad"]
        menu.append({"id": gnum[g["id"]], "clave": g["id"], "titulo": g["title"], "tipo": "GRUPO",
                     "orden_default": gnum[g["id"]], "children": children})
        grupos_rutas[gnum[g["id"]]] = [c["ruta"] for c in children]

    roles = catalogo["roles"]
    config = {
        "fuente": "estudio-menus-app/estudio-menus/app/src/data/catalog.json",
        "version_catalogo": catalogo.get("version"),
        "MENU_ERP": menu,
        "GRUPOS_RUTAS": grupos_rutas,
        "PERMISOS_ROL": {r: [gnum[x] for x in d["groups"]] for r, d in roles.items()},
        "TAREAS_ROL": {r: list(d["tasks"]) for r, d in roles.items()},
    }
    return config, profundidad


def main():
    cat_path = Path(sys.argv[1]) if len(sys.argv) > 1 else CATALOGO_DEF
    config, profundidad = construir(cargar_catalogo(cat_path))

    (RAIZ / "erp_config_universal.json").write_text(
        json.dumps(config, ensure_ascii=False, indent=2), encoding="utf-8")
    (RAIZ / "erp_menu_schema.json").write_text(
        json.dumps(profundidad, ensure_ascii=False, indent=2), encoding="utf-8")

    n = sum(len(v) for v in config["GRUPOS_RUTAS"].values())
    print(f"[OK] Menú real del ERP: {len(config['MENU_ERP'])} grupos, {n} funciones, "
          f"roles = {list(config['PERMISOS_ROL'])}")
    print(f"     -> {RAIZ / 'erp_config_universal.json'}")
    print(f"     -> {RAIZ / 'erp_menu_schema.json'}")


if __name__ == "__main__":
    main()
