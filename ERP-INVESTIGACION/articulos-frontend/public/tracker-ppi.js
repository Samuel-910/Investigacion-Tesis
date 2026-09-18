class AutoMenuTracker {
    constructor(config) {
        // Configuración inicial
        this.endpoint = config.endpoint || 'http://localhost:8000/api/logs';
        this.sessionId = this.getOrCreateSessionId();
        this.lastRoute = window.location.pathname;

        // Iniciar la escucha de eventos
        this.initListeners();
        console.log("✅ AutoMenuTracker inicializado. Sesión:", this.sessionId);
    }

    // Intenta obtener datos del usuario desde localStorage (típico en Angular/React)
    getUserInfo() {
        try {
            // Intenta leer si el ERP guarda algo como 'user' o 'token'
            const userStr = localStorage.getItem('usuario') || localStorage.getItem('user');
            if (userStr) {
                const userObj = JSON.parse(userStr);
                return {
                    id: userObj.id || userObj.username || 'usuario_erp',
                    role: userObj.rol || userObj.role || 'sin_rol'
                };
            }
        } catch (e) {
            // Ignorar errores de parseo
        }
        return { id: 'anonimo', role: 'sin_rol' };
    }

    getOrCreateSessionId() {
        let sessionId = sessionStorage.getItem('ppi_session_id');
        if (!sessionId) {
            sessionId = crypto.randomUUID ? crypto.randomUUID() : 'sess_' + Date.now();
            sessionStorage.setItem('ppi_session_id', sessionId);
        }
        return sessionId;
    }

    initListeners() {
        // Capturar todos los clics
        document.addEventListener('click', (evento) => {
            // Identificar si tocó algo interactivo (enlace, botón, o ítems de menú)
            const elementoClickeado = evento.target.closest('a, button, [role="menuitem"], .nav-link, .menu-item');
            
            if (elementoClickeado) {
                // Pequeño delay para que Angular actualice la URL si es un routerLink
                setTimeout(() => {
                    this.registrarClic(elementoClickeado);
                }, 50);
            }
        });

        // Escuchar cambios en el historial (SPA)
        window.addEventListener('popstate', () => {
            // No registramos clic aquí, pero actualizamos la ruta anterior
            this.lastRoute = window.location.pathname;
        });
    }

    registrarClic(elemento) {
        const currentRoute = window.location.pathname;
        const userInfo = this.getUserInfo();
        
        // Obtener texto limpio del botón/enlace (sin HTML interno)
        let texto = elemento.innerText || elemento.textContent || '';
        texto = texto.replace(/\s+/g, ' ').trim().substring(0, 50);

        const logData = {
            timestamp: new Date().toISOString(),
            user_id: userInfo.id,
            role: userInfo.role,
            session_id: this.sessionId,
            elemento_texto: texto,
            elemento_id: elemento.id || 'sin_id',
            route: currentRoute,
            from_route: this.lastRoute,
            tipo_evento: 'clic_navegacion'
        };

        this.enviarAlServidor(logData);
        this.lastRoute = currentRoute;
    }

    enviarAlServidor(data) {
        // Usamos sendBeacon para no bloquear al ERP
        if (navigator.sendBeacon) {
            const blob = new Blob([JSON.stringify(data)], { type: 'application/json' });
            navigator.sendBeacon(this.endpoint, blob);
            console.log("📡 [Tracker] Log enviado:", data);
        } else {
            // Fallback por si el navegador es muy antiguo
            fetch(this.endpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data),
                keepalive: true
            }).catch(err => console.error("Error enviando telemetría", err));
        }
    }
}

// Auto-inicializar cuando el script cargue
window.addEventListener('DOMContentLoaded', () => {
    window.ppiTracker = new AutoMenuTracker({
        endpoint: 'http://localhost:8000/api/logs' // Cambiaremos esto cuando levantemos el backend Python
    });
});
