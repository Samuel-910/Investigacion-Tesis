@echo off
title Setup Entorno Implementacion PPI

echo ===================================================
echo   Entorno IA - Implementacion (PPI)
echo ===================================================

echo.
echo [ 1/3 ] Creando entorno virtual "venv"...
if exist venv\ (
    echo    [!] El entorno virtual ya existe.
) else (
    py -m venv venv
    echo    [OK] Entorno creado exitosamente.
)

echo.
echo [ 2/3 ] Activando entorno...
call venv\Scripts\activate.bat

echo.
echo [ 3/3 ] Instalando dependencias de requirements.txt...
py -m pip install --upgrade pip
pip install -r requirements.txt

echo.
echo ===================================================
echo   [OK] El entorno es totalmente replicable y esta listo.
echo ===================================================
echo.
echo Para activarlo en el futuro, solo ejecuta:
echo venv\Scripts\activate
echo.
pause
