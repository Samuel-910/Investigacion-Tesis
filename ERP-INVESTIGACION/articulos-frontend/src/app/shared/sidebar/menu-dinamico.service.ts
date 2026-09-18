import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface MenuItemDTO {
  id: number;
  titulo: string;
  ruta?: string;
  icono?: string;
  orden: number;
  tipo: 'GRUPO' | 'SUBMENU' | 'ITEM';
  children?: MenuItemDTO[];
}

@Injectable({
  providedIn: 'root',
})
export class MenuDinamicoService {
  private readonly API_URL = `${environment.apiUrl}/menu`;

  private menuSubject = new BehaviorSubject<MenuItemDTO[]>([]);
  public menu$ = this.menuSubject.asObservable();

  constructor(private http: HttpClient) { }

  /**
   * Carga el menú dinámico desde el backend según los permisos del usuario
   * Retorna el menú completo tal como viene de la BD (con estructura jerárquica)
   */
  cargarMenu(usuarioId: number): Observable<MenuItemDTO[]> {
    const url = `${this.API_URL}/${usuarioId}`;
    return this.http.get<any>(url).pipe(
      map((response) => {
        // Extraer el array de datos de la respuesta del backend
        const menu = response.data || response || [];
        return menu;
      }),
      tap((menu) => {
        this.menuSubject.next(menu);

        // ✅ Actualizar en el objeto centralizado
        const userData = localStorage.getItem('currentUser');
        if (userData) {
          const user = JSON.parse(userData);
          user.menu = menu;
          localStorage.setItem('currentUser', JSON.stringify(user));
        }

        // Limpiar clave antigua
        localStorage.removeItem('userMenu');
      })
    );
  }

  /**
   * Obtiene el menú actual
   */
  getMenu(): MenuItemDTO[] {
    return this.menuSubject.value;
  }

  /**
   * Carga el menú desde localStorage
   */
  cargarMenuDesdeStorage(): void {
    try {
      const userData = localStorage.getItem('currentUser');
      if (userData) {
        const user = JSON.parse(userData);
        if (user.menu) {
          this.menuSubject.next(user.menu);
        }
      }
    } catch (e) {
      console.error('Error cargando menú desde storage:', e);
    }
  }

  /**
   * Limpia el menú (para logout)
   */
  clearMenu(): void {
    this.menuSubject.next([]);
    console.log('🧹 Menú limpiado');
  }

  /**
   * Busca un item específico en el menú por ruta
   */
  findItemByRuta(ruta: string): MenuItemDTO | null {
    const buscarEnMenu = (items: MenuItemDTO[]): MenuItemDTO | null => {
      for (const item of items) {
        if (item.ruta === ruta) {
          return item;
        }
        if (item.children && item.children.length > 0) {
          const found = buscarEnMenu(item.children);
          if (found) return found;
        }
      }
      return null;
    };

    return buscarEnMenu(this.menuSubject.value);
  }

  /**
   * Obtiene todos los items de tipo ITEM (hojas del árbol)
   */
  getAllItems(): MenuItemDTO[] {
    const items: MenuItemDTO[] = [];

    const extraerItems = (menu: MenuItemDTO[]) => {
      menu.forEach((item) => {
        if (item.tipo === 'ITEM') {
          items.push(item);
        }
        if (item.children && item.children.length > 0) {
          extraerItems(item.children);
        }
      });
    };

    extraerItems(this.menuSubject.value);
    return items;
  }

  /**
   * Obtiene un grupo específico por título
   */
  getGrupoByTitulo(titulo: string): MenuItemDTO | undefined {
    return this.menuSubject.value.find(
      (grupo) => grupo.titulo.toUpperCase() === titulo.toUpperCase()
    );
  }
}
