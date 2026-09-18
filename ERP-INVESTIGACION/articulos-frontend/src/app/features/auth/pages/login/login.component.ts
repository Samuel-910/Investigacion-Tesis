import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, FormInputComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  usuario: string = '';
  contrasena: string = '';
  mostrarContrasena: boolean = false;
  error: boolean = false;
  mensajeError: string = '';
  cargando: boolean = false;

  // Bloqueo por intentos excesivos
  isBlocked: boolean = false;
  unblockTime: Date | null = null;
  countdownInterval: any = null;
  remainingTimeString: string = '';

  constructor(
    private router: Router, 
    private authService: AuthService,
    private cdRef: ChangeDetectorRef
  ) { }

  ngOnInit() {
    const currentUser = this.authService.currentUserValue;
    if (currentUser && currentUser.token) {
      console.log('✅ [LOGIN] Usuario ya autenticado, redirigiendo al Dashboard Principal...');
      this.router.navigate(['/reportes/personalizado/PRINCIPAL']);
    }
  }

  ngOnDestroy() {
    // Limpiar intervalo al destruir componente
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
  }

  toggleMostrarContrasena() {
    this.mostrarContrasena = !this.mostrarContrasena;
  }

  async iniciarSesion() {
    // No permitir login si está bloqueado
    if (this.isBlocked) {
      return;
    }

    // Resetear el estado de error antes de intentar login
    this.error = false;
    this.mensajeError = '';

    // Validación básica
    if (!this.usuario || !this.contrasena) {
      this.error = true;
      this.mensajeError = 'Por favor complete todos los campos';
      return;
    }



    this.authService.login(this.usuario, this.contrasena).subscribe({
      next: (response) => {
        this.cargando = true;
        this.cdRef.detectChanges();
        // El AuthService ya maneja el almacenamiento en localStorage
        // Solo redirigimos
        this.router.navigate(['/dashboard/Principal']);
      },
      error: (err) => {
        this.error = true;
        this.cdRef.detectChanges(); // Forzar pintado inmediato de rojo

        console.log('err.error:', err.error);
        console.log('err.error.message:', err.error?.message);
        console.log('err.error.remainingAttempts:', err.error?.remainingAttempts);
        console.log('err.error.blocked:', err.error?.blocked);
        console.log('======================');

        let mensajeError = 'Ha ocurrido un error inesperado.';

        // Extraer el mensaje específico del backend
        if (err.error && err.error.message) {
          mensajeError = err.error.message;
        }

        this.mensajeError = mensajeError;
        this.cdRef.detectChanges();

        // Si está bloqueado, capturar unblockTime e iniciar countdown
        if (err.error?.blocked && err.error?.unblockTime) {
          this.isBlocked = true;
          this.unblockTime = new Date(err.error.unblockTime);
          this.startCountdown();
          this.cdRef.detectChanges();
        }

        // Mostrar SweetAlert
        Swal.fire({
          icon: 'error',
          title: 'Error de Autenticación',
          text: mensajeError,
          confirmButtonText: 'Entendido',
          confirmButtonColor: '#dc2626'
        });
      },
      complete: () => {
        this.cargando = false;
        this.cdRef.detectChanges();
      }
    });
  }

  onSubmit(event: Event) {
    event.preventDefault();
    this.iniciarSesion();
  }

  startCountdown() {
    // Limpiar intervalo anterior si existe
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }

    // Actualizar inmediatamente
    this.updateRemainingTime();

    // Actualizar cada segundo
    this.countdownInterval = setInterval(() => {
      this.updateRemainingTime();
    }, 1000);
  }

  updateRemainingTime() {
    if (!this.unblockTime) {
      this.remainingTimeString = '';
      return;
    }

    const now = new Date().getTime();
    const unblockTimestamp = this.unblockTime.getTime();
    const difference = unblockTimestamp - now;

    if (difference <= 0) {
      // Tiempo expirado, desbloquear
      this.isBlocked = false;
      this.unblockTime = null;
      this.remainingTimeString = '';
      if (this.countdownInterval) {
        clearInterval(this.countdownInterval);
        this.countdownInterval = null;
      }
      return;
    }

    // Calcular minutos y segundos restantes
    const minutes = Math.floor((difference / 1000 / 60) % 60);
    const seconds = Math.floor((difference / 1000) % 60);

    this.remainingTimeString = `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }
}