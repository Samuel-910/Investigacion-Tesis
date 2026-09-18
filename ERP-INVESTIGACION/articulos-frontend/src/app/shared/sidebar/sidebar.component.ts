import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, RouterModule } from '@angular/router';
import { trigger, transition, style, animate } from '@angular/animations';
import { filter } from 'rxjs/operators';
import { SidebarService } from './sidebar.service';
import { PermissionService2 } from '../../features/auth/services/permission2.service';
import { AuthService } from '../../features/auth/services/auth.service';
import { MenuDinamicoService, MenuItemDTO } from './menu-dinamico.service';
import { AlertService } from '../../core/services/alert.service';
import { ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css'],
  animations: [
    trigger('slideDown', [
      transition(':enter', [
        style({ height: '0', opacity: 0, overflow: 'hidden' }),
        animate('200ms ease-out', style({ height: '*', opacity: 1 }))
      ]),
      transition(':leave', [
        style({ height: '*', opacity: 1, overflow: 'hidden' }),
        animate('200ms ease-in', style({ height: '0', opacity: 0 }))
      ])
    ])
  ]
})
export class SidebarComponent implements OnInit {
  userData: any = null;

  // Ruta actual
  rutaActual: string = '';
  usuarioActual: string = 'Usuario';
  userGender: string = 'null';
  bien: string = 'null';
  menuDinamico: MenuItemDTO[] = [];
  grupoExpandido: { [key: number]: boolean } = {};
  submenuExpandido: { [key: number]: boolean } = {};

  constructor(
    private router: Router,
    public sidebarService: SidebarService,
    private cdRef: ChangeDetectorRef,
    public permissionService: PermissionService2,
    private menuService: MenuDinamicoService,
    private authService: AuthService,
    private alertService: AlertService,
    public themeService: ThemeService
  ) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.rutaActual = event.url;
      this.expandirMenuSegunRuta(event.url);
    });
  }

  ngOnInit() {
    this.cargarPerfil();
    this.rutaActual = this.router.url;

    const userStorage = localStorage.getItem('currentUser');
    if (userStorage) {
      try {
        this.userData = JSON.parse(userStorage);
        this.permissionService.setUser(this.userData);

        // Buscar dinámicamente el acceso "Finanzas" (evitar ID estático)
        const accesoArticulos = this.userData.accesos?.find((a: any) =>
          a.nombre.toLowerCase() === 'articulos'
        );
        const idParaFiltrar = accesoArticulos ? accesoArticulos.id : 1;
        this.cargarMenuDinamico(idParaFiltrar);

      } catch (e) {
        console.error('Error al parsear usuario:', e);
      }
    }

    this.cdRef.detectChanges();
  }

  cargarMenuDinamico(usuarioId: number): void {
    // 1. Intentar cargar instantáneamente desde storage para mayor velocidad
    this.menuService.cargarMenuDesdeStorage();
    const menuLocal = this.menuService.getMenu();

    if (menuLocal && menuLocal.length > 0) {
      this.menuDinamico = menuLocal;
      this.expandirMenuSegunRuta(this.rutaActual);
      this.cdRef.detectChanges();
    }

    // 2. Siempre intentar actualizar desde el backend en segundo plano
    this.menuService.cargarMenu(usuarioId).subscribe({
      next: (response) => {
        // Si el menú del backend es diferente o es la primera carga
        this.menuDinamico = this.menuService.getMenu();
        this.expandirMenuSegunRuta(this.rutaActual);
        this.cdRef.detectChanges();
      },
      error: (err) => {
        if (!this.menuDinamico || this.menuDinamico.length === 0) {
          this.menuService.cargarMenuDesdeStorage();
          this.menuDinamico = this.menuService.getMenu();
          this.cdRef.detectChanges();
        }
      }
    });
  }

  expandirMenuSegunRuta(url: string): void {
    this.menuDinamico.forEach(grupo => {
      const tieneRutaActiva = this.verificarRutaEnGrupo(grupo, url);
      if (tieneRutaActiva) {
        this.grupoExpandido[grupo.id] = true;
        grupo.children?.forEach(child => {
          if (child.tipo === 'SUBMENU') {
            const tieneRutaEnSubmenu = this.verificarRutaEnGrupo(child, url);
            if (tieneRutaEnSubmenu) {
              this.submenuExpandido[child.id] = true;
            }
          }
        });
      }
    });
  }

  private verificarRutaEnGrupo(item: MenuItemDTO, url: string): boolean {
    if (item.ruta && url.includes(item.ruta)) {
      return true;
    }
    if (item.children && item.children.length > 0) {
      return item.children.some(child => this.verificarRutaEnGrupo(child, url));
    }
    return false;
  }

  toggleGrupo(grupoId: number): void {
    this.grupoExpandido[grupoId] = !this.grupoExpandido[grupoId];
  }

  toggleSubmenu(submenuId: number): void {
    this.submenuExpandido[submenuId] = !this.submenuExpandido[submenuId];
  }

  navegarA(ruta: string): void {
    this.router.navigate([ruta]);
  }

  cargarPerfil(): void {
    const userStorage = localStorage.getItem('currentUser');
    if (userStorage) {
      try {
        const user = JSON.parse(userStorage);
        this.usuarioActual = user.firstName + ' ' + (user.lastName || '');
        this.userGender = user.sexo || 'null';
      } catch (e) {
        this.usuarioActual = 'Usuario';
      }
    }
  }

  getProfileImage(): string {
    if (this.userGender === 'M') {
      this.bien = 'Bienvenido';
      return '/male-avatar.jpg';
    } else if (this.userGender === 'F') {
      this.bien = 'Bienvenida';
      return '/female-avatar.png';
    } else {
      this.bien = 'Bienvenide';
      return '/default-avatar.png';
    }
  }

  cerrarSesion(): void {
    this.alertService.confirm(
      '¿Está seguro de cerrar sesión?',
      'Esta acción finalizará su sesión actual.',
      'Sí, cerrar sesión',
      'Cancelar'
    ).then(result => {
      if (result.isConfirmed) {
        this.permissionService.clearPermisos();
        this.menuService.clearMenu();
        this.authService.logout();
      }
    });
  }
}