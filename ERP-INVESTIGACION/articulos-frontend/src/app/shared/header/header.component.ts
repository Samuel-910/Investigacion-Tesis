import { Component, Input, OnInit, OnDestroy, Output, EventEmitter, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { interval, Subscription, fromEvent, merge } from 'rxjs';
import { throttleTime, filter } from 'rxjs/operators';

import { HttpClientModule } from '@angular/common/http';
import { AuthService, TokenInfo } from '../../features/auth/services/auth.service';
import { SidebarService } from '../sidebar/sidebar.service';
import { ConfiguracionService } from '../../core/services/configuracion.service';
import { ClinicaService } from '../../features/configuraciones/services/clinica.service';
import { ThemeService } from '../../core/services/theme.service';
import { FormsModule } from '@angular/forms';
import { CambioPuntoComponent } from './cambio-punto/cambio-punto.component';
import { CambioClaveModalComponent } from './cambio-clave-modal.component';
import { AlertService } from '../../core/services/alert.service';
import { WebSocketService, Notification as NotificationPayload } from '../../core/services/websocket.service';
import { NotificacionService, Notificacion } from '../../core/services/notificacion.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, HttpClientModule, FormsModule, CambioPuntoComponent, CambioClaveModalComponent],
  // ✅ CRÍTICO: REMOVER AuthService de aquí para usar el singleton global
  // providers: [AuthService],  ❌ ESTO CAUSABA EL PROBLEMA
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Input() breadcrumb: string = 'Inicio';
  @Output() onCerrarSesion = new EventEmitter<void>();

  tiempoRestanteMs: number = 0;
  tiempoFormateado: string = '30:00';

  private timerSubscription: Subscription | undefined;
  private syncSubscription: Subscription | undefined;

  usuarioActual: string = 'Usuario';
  primerNombre: string = 'Usuario';
  userGender: string = 'null';
  bien: string = 'null';
  sucursalNombre: string = '';
  puntoNombre: string = '';
  menuUsuarioAbierto: boolean = false;

  isAdmin: boolean = false;
  showConfigConfig: boolean = false;
  showCambioPunto: boolean = false;
  showCambioClave: boolean = false;
  isLoadingConfig: boolean = false;
  logoClinica: string | null = null;

  private readonly UMBRAL_ADVERTENCIA = 300000;
  private advertenciaMostrada = false;

  notificacionesActivas: NotificationPayload[] = [];
  private notificationSub: Subscription | undefined;
  
  showNotificacionesDropdown = false;

  constructor(
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    public sidebarService: SidebarService,
    private configuracionService: ConfiguracionService,
    public themeService: ThemeService,
    private alertService: AlertService,
    private webSocketService: WebSocketService,
    private clinicaService: ClinicaService,
    public notificacionService: NotificacionService
  ) {
    this.cargarSucursalInicial();
  }

  private activitySubscription: Subscription | undefined;
  private sucursalChangeSubscription: Subscription | undefined;
  private routerSubscription: Subscription | undefined;

  private cargarSucursalInicial(): void {
    const user = this.authService.currentUserValue;

    if (user) {
      this.sucursalNombre = user.sucursalNombre || this.authService.getSucursalFromToken() || '';
      this.puntoNombre = user.puntoNombre || '';
    } else {
      this.sucursalNombre = this.authService.getSucursalFromToken() || '';
      this.puntoNombre = this.authService.getPuntoFromToken() || '';
    }
  }

  ngOnInit() {
    this.iniciarTemporizador();
    this.cargarPerfil();
    this.setupActivityDetection();
    this.setupSucursalChangeListener();
    this.setupRouterListener();
    this.cargarLogoClinica();

    this.cdr.detectChanges();
    document.addEventListener('click', this.cerrarMenuUsuarioFuera.bind(this));

    this.notificationSub = this.webSocketService.notifications$.subscribe(notif => {
      this.notificacionesActivas.push(notif);
      this.cdr.detectChanges();

      // Auto-remove after 5 seconds
      setTimeout(() => {
        this.removerNotificacion(notif);
      }, 5000);
    });
  }

  cargarLogoClinica(): void {
    this.clinicaService.obtenerPrincipal().subscribe({
      next: (response) => {
        if (response.success && response.data && response.data.logoCuadrado) {
          this.logoClinica = response.data.logoCuadrado;
          
          // Actualizar dinámicamente el favicon
          const favicon = document.getElementById('app-favicon') as HTMLLinkElement;
          if (favicon) {
            favicon.href = this.logoClinica;
          }
          
          this.cdr.detectChanges();
        }
      },
      error: (err) => console.error('Error cargando logo de la clínica:', err)
    });
  }

  onNotificationClick(notif: NotificationPayload): void {
    if (notif.modulo === 'COMPRAS') {
      const id = typeof notif.contenido === 'object' && notif.contenido !== null 
          ? notif.contenido.idCompra : null;
      if (id) {
        this.router.navigate(['/admin/compras/registrar'], { queryParams: { openCompraId: id } });
      } else {
        this.router.navigate(['/admin/compras/registrar']);
      }
    }
    this.removerNotificacion(notif);
  }

  removerNotificacion(notif: NotificationPayload): void {
    const index = this.notificacionesActivas.indexOf(notif);
    if (index > -1) {
      this.notificacionesActivas.splice(index, 1);
      this.cdr.detectChanges();
    }
  }

  getContenidoTexto(notif: NotificationPayload): string {
    if (typeof notif.contenido === 'string') {
      return notif.contenido;
    } else if (typeof notif.contenido === 'object' && notif.contenido !== null) {
      if (notif.contenido.mensaje) {
        return notif.contenido.mensaje;
      }
    }
    return 'Nueva notificación';
  }

  getNotifType(notif: any): string {
    return notif && notif.tipo ? notif.tipo.toString().toUpperCase() : 'INFO';
  }

  toggleDarkMode() {
    this.themeService.toggleTheme();
  }

  get isDarkMode(): boolean {
    return this.themeService.isDark();
  }

  setupActivityDetection(): void {
    const activityEvents$ = merge(
      fromEvent(document, 'mousemove'),
      fromEvent(document, 'click'),
      fromEvent(document, 'keydown'),
      fromEvent(document, 'scroll')
    );

    this.activitySubscription = activityEvents$
      .pipe(throttleTime(60000))
      .subscribe(() => {
        this.sincronizarConServidor();
      });
  }

  setupSucursalChangeListener(): void {
    this.sucursalChangeSubscription = this.authService.onSucursalChanged$.subscribe(nuevaSucursal => {
      console.log('🔔 [HEADER] notificación de cambio de sucursal:', nuevaSucursal);

      if (nuevaSucursal && nuevaSucursal !== this.sucursalNombre) {
        this.sucursalNombre = nuevaSucursal;

        // ✅ Intentar actualizar puntoNombre desde el token/currentUser
        const user = this.authService.currentUserValue;
        if (user && user.puntoNombre) {
          this.puntoNombre = user.puntoNombre;
        }

        this.cdr.detectChanges();

        // ✅ Confirmar al AuthService que ya se actualizó la UI
        setTimeout(() => {
          this.authService.confirmSucursalUpdated();
        }, 0);
      }
    });
  }

  setupRouterListener(): void {
    this.routerSubscription = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        const user = this.authService.currentUserValue;
        if (user) {
          this.sucursalNombre = user.sucursalNombre || '';
          this.puntoNombre = user.puntoNombre || '';
          this.cdr.detectChanges();
        }
      });
  }

  cargarPerfil(): void {
    const user = this.authService.currentUserValue;
    if (user) {
      this.usuarioActual = user.firstName + ' ' + (user.lastName || '');
      this.primerNombre = user.firstName || 'Usuario';
      this.userGender = user.sexo || 'null';

      if (this.userGender === 'M') {
        this.bien = 'Bienvenido';
      } else if (this.userGender === 'F') {
        this.bien = 'Bienvenida';
      } else {
        this.bien = 'Bienvenide';
      }
      this.isAdmin = user.isAdmin || false;
      this.sucursalNombre = user.sucursalNombre || this.authService.getSucursalFromToken() || '';
      this.puntoNombre = user.puntoNombre || '';
    } else {
      this.usuarioActual = 'Usuario';
      this.bien = 'Bienvenide';
      this.isAdmin = false;
    }
  }

  getProfileImage(): string {
    if (this.userGender === 'M') {
      return '/male-avatar.jpg';
    } else if (this.userGender === 'F') {
      return '/female-avatar.png';
    } else {
      return '/default-avatar.png';
    }
  }

  iniciarTemporizador(): void {
    const savedTimeout = localStorage.getItem('sessionTimeout');
    if (savedTimeout) {
      this.tiempoRestanteMs = parseInt(savedTimeout);
      this.tiempoFormateado = this.formatearTiempo(this.tiempoRestanteMs);
      console.log('Timeout inicial desde localStorage:', this.tiempoRestanteMs, 'ms');
    } else {
      this.sincronizarConServidor();
    }

    this.timerSubscription = interval(1000).subscribe(() => {
      if (this.tiempoRestanteMs > 0) {
        this.tiempoRestanteMs -= 1000;
        this.tiempoFormateado = this.formatearTiempo(this.tiempoRestanteMs);
        this.cdr.detectChanges();

        if (this.tiempoRestanteMs <= 0) {
          this.manejarSesionExpirada();
        } else {
          if (this.tiempoRestanteMs <= this.UMBRAL_ADVERTENCIA && !this.advertenciaMostrada) {
            this.mostrarAdvertencia();
            this.advertenciaMostrada = true;
          }
        }
      }
    });

    this.syncSubscription = interval(30000).subscribe(() => {
      this.sincronizarConServidor();
    });
  }

  sincronizarConServidor(): void {
    this.authService.refreshToken().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          const tokenInfo: TokenInfo = response.data;

          if (tokenInfo.isExpired) {
            this.manejarSesionExpirada();
          } else {
            this.tiempoRestanteMs = tokenInfo.timeRemainingMs;
            this.tiempoFormateado = this.formatearTiempo(this.tiempoRestanteMs);
            this.cdr.detectChanges();
          }
        }
      },
      error: (error) => {
        console.error('Error al sincronizar tiempo del token:', error);
        if (error.status === 401) {
          this.manejarSesionExpirada();
        }
      }
    });
  }

  mostrarAdvertencia(): void {
    const minutos = Math.floor(this.tiempoRestanteMs / 60000);
    alert(`⚠️ Su sesión expirará en ${minutos} minutos. Por favor, guarde su trabajo.`);
  }

  manejarSesionExpirada(): void {
    this.detenerTemporizador();
    this.tiempoFormateado = '00:00';
    alert('Su sesión ha expirado. Por favor, inicie sesión nuevamente.');
    this.cerrarSesion();
  }

  detenerTemporizador(): void {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
    if (this.syncSubscription) {
      this.syncSubscription.unsubscribe();
    }
  }

  formatearTiempo(ms: number): string {
    const totalSegundos = Math.floor(ms / 1000);
    const minutos = Math.floor(totalSegundos / 60);
    const segundos = totalSegundos % 60;

    const minutosStr = String(minutos).padStart(2, '0');
    const segundosStr = String(segundos).padStart(2, '0');

    return `${minutosStr}:${segundosStr} `;
  }

  ngOnDestroy(): void {
    this.detenerTemporizador();
    if (this.activitySubscription) {
      this.activitySubscription.unsubscribe();
    }
    if (this.sucursalChangeSubscription) {
      this.sucursalChangeSubscription.unsubscribe();
    }
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
    if (this.notificationSub) {
      this.notificationSub.unsubscribe();
    }
    document.removeEventListener('click', this.cerrarMenuUsuarioFuera.bind(this));
  }

  toggleMenuUsuario() {
    this.menuUsuarioAbierto = !this.menuUsuarioAbierto;
  }

  cerrarMenuUsuarioFuera(event: MouseEvent) {
    const target = event.target as HTMLElement;
    const profileContainer = document.getElementById('profile-menu-container');

    if (profileContainer && !profileContainer.contains(target)) {
      this.menuUsuarioAbierto = false;
    }
    
    const notifContainer = document.getElementById('notif-menu-container');
    if (notifContainer && !notifContainer.contains(target)) {
      this.showNotificacionesDropdown = false;
    }
  }

  toggleNotificacionesDropdown() {
    this.showNotificacionesDropdown = !this.showNotificacionesDropdown;
    if (this.showNotificacionesDropdown) {
      this.menuUsuarioAbierto = false;
    }
  }

  marcarNotificacionLeida(id: number, event: Event) {
    event.stopPropagation();
    this.notificacionService.marcarComoLeida(id).subscribe({
      next: () => {
        // el servicio ya actualiza el estado local
      },
      error: err => console.error(err)
    });
  }

  verPerfil() {
    this.menuUsuarioAbierto = false;
    this.router.navigate(['/perfil']);
  }

  cambiarContrasena() {
    this.menuUsuarioAbierto = false;
    this.showCambioClave = true;
  }

  abrirCambioPunto() {
    this.menuUsuarioAbierto = false;
    this.showCambioPunto = true;
  }

  cerrarSesion() {
    this.menuUsuarioAbierto = false;
    this.alertService.confirm(
      '¿Está seguro de cerrar sesión?',
      'Esta acción finalizará su sesión actual.',
      'Sí, cerrar sesión',
      'Cancelar'
    ).then(result => {
      if (result.isConfirmed) {
        this.authService.logout();
        this.onCerrarSesion.emit();
      }
    });
  }

  abrirConfiguracion() {
    this.showConfigConfig = true;
    if (this.isAdmin) {
      this.loadGlobalConfig();
    }
  }

  cancelarConfiguracion() {
    this.showConfigConfig = false;
  }

  globalTimeValue: number = 30;

  loadGlobalConfig() {
    this.configuracionService.getGlobalSessionTimeout().subscribe({
      next: (data) => {
        if (data && data.minutes) {
          this.globalTimeValue = data.minutes;
        }
      },
      error: (err) => {
        console.error('Error al cargar configuración global', err);
      }
    });
  }

  guardarConfiguracion() {
    if (this.globalTimeValue < 15) {
      alert('⚠️ El tiempo mínimo de sesión es de 15 minutos.');
      this.globalTimeValue = 15;
      return;
    }

    this.isLoadingConfig = true;

    if (this.isAdmin) {
      this.configuracionService.updateGlobalSessionTimeout(this.globalTimeValue).subscribe({
        next: () => {
          alert('✅ Configuración actualizada. Cierra sesión y vuelve a iniciar para aplicar los cambios.');
          this.isLoadingConfig = false;
          this.showConfigConfig = false;
        },
        error: (err) => {
          console.error(err);
          alert('❌ Error al actualizar configuración global.');
          this.isLoadingConfig = false;
        }
      });
    } else {
      this.isLoadingConfig = false;
      this.showConfigConfig = false;
    }
  }
}