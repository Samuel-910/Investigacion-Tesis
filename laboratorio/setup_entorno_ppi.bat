@echo off

title Setup PPI_C9_2026

echo.
echo ===================================================
echo   Entorno PPI_C9_2026 - Setup Windows
echo ===================================================

:: -- 1. Verificar Python ----------------------------------------
echo.
echo [ 1/5 ] Verificando Python...
py --version >nul 2>&1
if errorlevel 1 (
    echo [X] Python no encontrado.
    echo    Descargalo en: https://www.python.org/downloads/
    echo    Asegurate de marcar "Add Python to PATH"
    pause
    exit /b 1
)
py --version
echo [OK] Python OK

:: -- 2. Crear entorno virtual -----------------------------------
echo.
echo [ 2/5 ] Creando entorno virtual "ppi_env"...
if exist ppi_env\ (
    echo    [!] ppi_env ya existe, omitiendo creacion
) else (
    py -m venv ppi_env
    echo [OK] Entorno virtual creado
)

:: -- 3. Activar entorno -----------------------------------------
echo.
echo [ 3/5 ] Activando entorno...
call ppi_env\Scripts\activate.bat
echo [OK] Entorno activo
echo.
echo [ 4/5 ] Instalando dependencias...
py -m pip install --upgrade pip
py -m pip install pandas numpy scipy scikit-learn matplotlib notebook torch
echo [OK] Dependencias instaladas

echo.
echo ===================================================
echo   Versiones instaladas:
echo ===================================================
py -c "import pandas,numpy,scipy,sklearn,matplotlib,notebook,torch; print(f'  pandas       {pandas.__version__}\n  numpy        {numpy.__version__}\n  scipy        {scipy.__version__}\n  scikit-learn {sklearn.__version__}\n  matplotlib   {matplotlib.__version__}\n  notebook     {notebook.__version__}\n  torch        {torch.__version__}')"

echo.
echo ===================================================
echo   [OK] Entorno listo
echo.
echo   Para usar el entorno, ejecuta:
echo     iniciar_jupyter.bat
echo ===================================================
echo.
pause
