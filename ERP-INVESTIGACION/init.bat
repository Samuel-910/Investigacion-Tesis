@echo off

echo ==========================================================
echo INICIANDO EL ECOSISTEMA COMPLETO DEL PROYECTO
echo ==========================================================
echo.

:: 1. Levantar contenedores de Docker
echo [PASO 1]: Verificando infraestructura Docker...
if exist docker-compose.yml (
    echo [Docker] Levantando contenedores de infraestructura en segundo plano...
    docker compose up -d
) else (
    echo [Docker] No se encontro docker-compose.yml en la raiz.
)
echo ----------------------------------------------------------

:: 2. Inicializar Backend
echo [PASO 2]: Verificando modulo de Backend...
echo [Backend] Iniciando articulos-backend en una nueva ventana...
if exist articulos-backend (
    cd articulos-backend
    start "Articulos Backend" cmd /k "call mvnw spring-boot:run"
    echo Comando de arranque enviado a articulos-backend.
    cd ..
) else (
    echo ERROR: La carpeta 'articulos-backend' no existe.
)
echo ----------------------------------------------------------

:: 3. Inicializar Servicio de Notificaciones
echo [PASO 3]: Verificando servicio de Notificaciones...
echo [Notificaciones] Iniciando notificaciones-service en una nueva ventana...
if exist notificaciones-service (
    cd notificaciones-service
    start "Notificaciones Service" cmd /k "call mvnw spring-boot:run"
    echo Comando de arranque enviado a notificaciones-service.
    cd ..
) else (
    echo ERROR: La carpeta 'notificaciones-service' no existe.
)
echo ----------------------------------------------------------

:: 4. Inicializar Frontend
echo [PASO 4]: Verificando modulo de Frontend...
echo [Frontend] Iniciando articulos-frontend (Angular) en una nueva ventana...
if exist articulos-frontend (
    cd articulos-frontend
    start "Angular Frontend" cmd /k "ng serve"
    echo Comando de arranque enviado a articulos-frontend.
    cd ..
) else (
    echo ERROR: La carpeta 'articulos-frontend' no existe.
)
echo ----------------------------------------------------------

echo ==========================================================
echo PROCESO DE INICIALIZACION COMPLETADO
echo ==========================================================
echo REVISA LAS NUEVAS VENTANAS ABIERTAS PARA LOGS.
echo.

pause