import { Injectable, OnDestroy, inject, signal } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import { Subject, Observable } from 'rxjs';
import SockJS from 'sockjs-client';
import { AlertService } from './alert.service';

import { environment } from '../../environments/environment';

export interface Notification {
  userId: string;
  modulo: string;
  titulo: string;
  contenido: any;
  tipo: 'INFO' | 'SUCCESS' | 'ERROR';
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService implements OnDestroy {
  private alertService = inject(AlertService);
  private stompClient: Client | null = null;
  public connected = signal<boolean>(false);
  private notificationSubject = new Subject<Notification>();
  public notifications$ = this.notificationSubject.asObservable();


  connect(userId?: string): void {
    if (this.stompClient && this.stompClient.connected) {
      return;
    }

    // Usar la base de la API y reemplazar /api por /ws-notifications
    const wsUrl = environment.apiUrl.replace('/api', '') + '/ws-notifications';
    const socket = new SockJS(wsUrl);
    const token = localStorage.getItem('token');
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: token ? `Bearer ${token}` : ''
      },
      debug: (str) => console.log('STOMP: ' + str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('Conectado a WebSocket');
      this.connected.set(true);

      // Obtener el ID de la sucursal actual del usuario
      const currentUserStr = localStorage.getItem('currentUser');
      let currentSucursalId = null;
      if (currentUserStr) {
        try {
          const user = JSON.parse(currentUserStr);
          if (user && user.sucursalActual) {
            currentSucursalId = user.sucursalActual.idSucursal;
          }
        } catch (e) {}
      }

      // 1. Suscripción a notificaciones del módulo para la sucursal específica
      if (currentSucursalId) {
        this.stompClient?.subscribe(`/topic/notifications/articulos/${currentSucursalId}`, (message) => {
          this.handleNotification(JSON.parse(message.body));
        });
      }

      // Suscripción al canal global del módulo
      this.stompClient?.subscribe('/topic/notifications/articulos/all', (message) => {
        this.handleNotification(JSON.parse(message.body));
      });

      // 2. Suscripción a notificaciones privadas por User Destination
      // El backend envía a /user/{userId}/queue/notifications
      // STOMP automáticamente mapea /user/queue/notifications al usuario autenticado (o userId)
      this.stompClient?.subscribe('/user/queue/notifications', (message) => {
        this.handleNotification(JSON.parse(message.body));
      });
    };

    this.stompClient.onDisconnect = () => {
      console.log('Desconectado de WebSocket');
      this.connected.set(false);
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Error de STOMP', frame.headers['message']);
    };

    this.stompClient.activate();
  }

  private handleNotification(notification: Notification): void {
    console.log('Nueva notificación recibida:', notification);

    // El filtro por módulo y sucursal ahora se maneja desde el backend.


    this.notificationSubject.next(notification);

    // El AlertService se remueve para usar exclusivamente el contenedor HTML del Header
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
