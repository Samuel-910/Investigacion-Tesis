import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable, BehaviorSubject, timer, of } from 'rxjs';
import { switchMap, tap, retry, shareReplay, catchError } from 'rxjs/operators';

export interface Notificacion {
  idNotificacion: number;
  tipo: string;
  titulo: string;
  mensaje: string;
  referenciaId: string;
  leido: boolean;
  fechaCreacion: string;
  idSucursal: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class NotificacionService {
  private apiUrl = `${environment.apiUrl}/notificaciones`;
  
  private notificacionesSubject = new BehaviorSubject<Notificacion[]>([]);
  public notificaciones$ = this.notificacionesSubject.asObservable();
  
  private noLeidasCountSubject = new BehaviorSubject<number>(0);
  public noLeidasCount$ = this.noLeidasCountSubject.asObservable();

  constructor(private http: HttpClient) {
    this.iniciarPolling();
  }

  private iniciarPolling() {
    // Consulta cada 15 minutos (900,000 ms)
    // El primer llamado es a los 2 segundos de iniciado
    timer(2000, 900000).pipe(
      switchMap(() => this.obtenerNoLeidas().pipe(
        retry(3),
        catchError(err => {
          console.error('Error silencioso al obtener notificaciones', err);
          return of({ success: false, message: 'Error', data: null });
        })
      ))
    ).subscribe({
      next: (res: any) => {
        if (res && res.success && res.data) {
          this.notificacionesSubject.next(res.data);
          this.noLeidasCountSubject.next(res.data.length);
        }
      },
      error: (err) => console.error('Error al obtener notificaciones', err)
    });
  }

  obtenerNoLeidas(): Observable<ApiResponse<Notificacion[]>> {
    return this.http.get<ApiResponse<Notificacion[]>>(`${this.apiUrl}/no-leidas`);
  }

  marcarComoLeida(id: number): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.apiUrl}/${id}/marcar-leida`, {}).pipe(
      tap(() => {
        // Actualizar el estado localmente sin tener que volver a llamar a la API
        const actuales = this.notificacionesSubject.getValue();
        const actualizadas = actuales.filter(n => n.idNotificacion !== id);
        this.notificacionesSubject.next(actualizadas);
        this.noLeidasCountSubject.next(actualizadas.length);
      })
    );
  }
}
