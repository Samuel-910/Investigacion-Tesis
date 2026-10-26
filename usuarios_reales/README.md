# Pipeline con el menú real del ERP

Esta carpeta es la versión de `implementacion/` que usa **las 60 funciones reales del menú del ERP**
(las mismas de la app del estudio, `estudio-menus-app/estudio-menus/app/src/data/catalog.json`)
y los 4 roles del estudio: Ventas, Compras, Almacen y Administrador.

`implementacion/` se deja intacta como referencia (su simulador usaba rutas que no existen en el ERP,
como `/ventas/registro` o `/compra/orden/aprobar`).

## Ejecutar todo

```bash
python run_all_pipeline.py              # genera datos y reentrena todo
python run_all_pipeline.py --sin-entrenar   # solo recalcula con los modelos ya entrenados
```

Usa el entorno `implementacion/venv` si esta carpeta no tiene uno propio.

## Pasos

| paso | script | salida |
|---|---|---|
| Config del menú | `00_Generador_Datos/construir_config_erp.py` | `erp_config_universal.json`, `erp_menu_schema.json` |
| Datos sintéticos | `00_Generador_Datos/simulador_erp_real.py` | `datos_sinteticos/logs_sinteticos_erp.ndjson` (+ participantes y task_results) |
| OE1 | `OE1_Baseline/main.py --fuente mixto` | perfiles, vocabulario (60 rutas), línea base M1–M3 |
| OE2 | `OE2_Modelado/main.py --model all` | GRU, LSTM, CNN1D, Transformer + `metricas_test.json` |
| OE2 | `OE2_Modelado/generate_scores.py` | `scores_relevancia.json` (LSTM) |
| OE3 | `OE3_Agente_DQN/main.py`, `evaluate.py` | agentes DQN, `menus_personalizados.json`, `menus_app.json` |
| OE4 / OE5 | `main.py` | auditoría y análisis estadístico |

`--fuente` de OE1: `mixto` (sintéticos + reales, por defecto), `sintetico` o `real`.

## Generador sintético

Dos modos (`--modo`):

- **`estudio` (por defecto)**: imita exactamente la sesión 1 de la app: las 8 tareas del rol,
  perfil personal Zipf (s = 1) como `personalProfile()`, sorteo sin repetir la anterior como
  `generateTasks()`, 2 bloques × 20 tareas, 3 de práctica. Calibrado con P001
  (clics, tiempos, visitas y confirmaciones erróneas, aprendizaje entre bloques).
  150 usuarios por rol = 600 usuarios.
- **`jornada`**: uso diario del ERP con arquetipos y flujos de trabajo (más variedad de funciones).

Comunes a los dos:

- Solo rutas del catálogo; valida que cada ruta exista y esté permitida para el rol.
- 9 arquetipos (2–3 por rol) con funciones principales y flujos de trabajo reales
  (p. ej. Clientes → Registrar venta → Comprobantes electrónicos).
- Perfil personal tipo Zipf por usuario (como `personalProfile()` de la app) + funciones secundarias.
- Experiencia `ninguna / basica / frecuente` (mismos valores que la app): cambia tiempos y errores.
- Errores: visitas a funciones equivocadas (vecinas del mismo grupo u otro grupo permitido).
- Salida en el mismo formato que `tools/export_logs.py`; los usuarios se llaman `SIM0001…`.

```bash
python simulador_erp_real.py                         # modo estudio, 150 usuarios por rol
python simulador_erp_real.py --modo jornada --usuarios-por-rol 50 --sesiones 30
```

## Subir los menús a la app

`OE3_Agente_DQN/output_oe3/menus_app.json` contiene solo a los participantes reales (P001…)
en el formato compacto de la app:

```bash
cd ../estudio-menus-app/estudio-menus/tools
python import_menus.py ../../../usuarios_reales/OE3_Agente_DQN/output_oe3/menus_app.json --modelo lstm-dqn-v1 --dry-run
```

## Cambios respecto a `implementacion/`

- OE1 lee varias fuentes a la vez y el vocabulario incluye todo el menú.
- OE2: el id 0 de las rutas chocaba con el padding (la primera ruta era invisible para los modelos;
  el Transformer la enmascaraba). Ahora la entrada va desplazada +1.
- OE2 reporta loss, acc@1 y acc@3 en el conjunto de prueba.
- OE3 exporta el rol y `menus_app.json`.
- OE3 (DQN) rediseñado; antes acertaba solo 18–35 % en grupos de 6+ ítems:
  - acción = "mover el ítem de la posición p al inicio" (antes: intercambios adyacentes);
  - estado en el orden actual del menú (frecuencia y ranking del ítem en cada posición);
  - recompensa = DCG normalizado de las primeras `TOP_K = 4` posiciones; costo de 0.05 por paso;
  - γ = 0.9, ε decae más lento y el objetivo no suma valor futuro al terminar el episodio;
  - el DQN decide la cabecera de cada grupo y el resto se ordena por el score del LSTM.
  Resultado: 100 % de aciertos en grupos de 2–10 ítems y 96 % en el de 12.
- OE5: los valores POST de M2, M3 y la satisfacción son **proyectados**, no medidos.
  Para la tesis hay que reemplazarlos con los datos reales de la sesión 2.
