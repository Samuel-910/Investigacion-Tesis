@echo off
:: ===================================================================
:: iniciar_jupyter.bat
:: Activa el entorno y abre Jupyter Notebook
:: Ejecutar este archivo cada vez que quieras trabajar
:: ===================================================================

title Jupyter - PPI_C9_2026

echo.
echo ===================================================
echo   Iniciando Jupyter - PPI_C9_2026
echo ===================================================
echo.

:: Activar entorno
if not exist ppi_env\Scripts\activate.bat (
    echo [X] No se encuentra el entorno virtual ppi_env.
    echo     Ejecuta primero setup_entorno_ppi.bat
    pause
    exit /b 1
)

call ppi_env\Scripts\activate.bat
if errorlevel 1 (
    echo [X] No se pudo activar el entorno virtual.
    pause
    exit /b 1
)

echo Entorno activo: ppi_env
echo Abriendo Jupyter en tu navegador...
echo Para detener: Ctrl + C en esta ventana
echo.

:: Abrir Jupyter usando el Python del entorno
py -m notebook --notebook-dir="%CD%"

pause
