import { Component, EventEmitter, OnInit, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../features/auth/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';
import { ModalComponent } from '../../components/modal/modal';
import { PrimaryButtonComponent } from '../../components/primary-button/primary-button';
import { FormInputComponent } from '../../../shared/components/forms/form-input/form-input.component';
import { SearchableSelectComponent } from '../../components/searchable-select/searchable-select.component';
import { PuntoService } from '../../../features/configuraciones/services/punto.service';

@Component({
  selector: 'app-cambio-punto',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ModalComponent,
    PrimaryButtonComponent,
    FormInputComponent,
    SearchableSelectComponent
  ],
  template: `
    <app-modal [isOpen]="true" 
               [title]="mode() === 'sucursal' ? 'Cambio de Sucursal' : 'Cambio de Punto de la Sucursal ' + sucursalActual()" 
               size="md" 
               (modalClose)="onClose()" 
               [allowOverflow]="true">
      <div class="bg-white dark:bg-slate-800">
        
        <!-- Mode Selector -->
        <div class="flex p-1 bg-slate-100 dark:bg-slate-900 rounded-xl mb-6">
          <button (click)="changeMode('sucursal')" 
                  [class]="mode() === 'sucursal' ? 'bg-white dark:bg-slate-800 shadow-sm text-blue-600' : 'text-slate-500'"
                  class="flex-1 py-2 text-xs font-bold rounded-lg transition-all">
            SUCURSAL
          </button>
          <button (click)="changeMode('punto')" 
                  [class]="mode() === 'punto' ? 'bg-white dark:bg-slate-800 shadow-sm text-blue-600' : 'text-slate-500'"
                  class="flex-1 py-2 text-xs font-bold rounded-lg transition-all">
            PUNTO DE VENTA
          </button>
        </div>

        <!-- Sucursal Selection (Cascade) -->
        <div class="mb-5" *ngIf="mode() === 'sucursal'">
           <app-searchable-select
             label="1. Seleccionar Sucursal"
             [data]="sucursalesLista()"
             bindLabel="nombre"
             bindValue="id"
             [ngModel]="selectedSucursalId()"
             (ngModelChange)="selectedSucursalId.set($event); sucursalError.set(null); onSucursalChange($event)"
             [error]="sucursalError()"
             [required]="true">
           </app-searchable-select>
        </div>

        <!-- Punto de Venta Selection (Always or after Sucursal) -->
        <div class="mb-5" *ngIf="mode() === 'punto' || (mode() === 'sucursal' && selectedSucursalId())">
           <app-searchable-select
             [label]="mode() === 'punto' ? '1. Seleccionar Punto de Venta' : '2. Seleccionar Punto de Venta'"
             [data]="mode() === 'punto' ? puntos() : puntosDeSucursal()"
             bindLabel="nombre"
             bindValue="id"
             [ngModel]="selectedId()"
             (ngModelChange)="selectedId.set($event); puntoError.set(null)"
             [error]="puntoError()"
             [required]="true">
           </app-searchable-select>
        </div>

        <!-- Verification Form (After Punto selection) -->
        <div *ngIf="selectedId() || (puntoError() && mode() === 'punto')" class="animate-in fade-in slide-in-from-top-4 duration-300">
          <div class="mb-1">
            <app-form-input 
              label="Usuario de Verificación" 
              [(ngModel)]="username" 
              placeholder="Ingrese su usuario (ej. ams)"
              [hasIcon]="true"
              [required]="true"
              [error]="usernameError()"
              (input)="usernameError.set(null)">
              <i icon class="fas fa-user text-slate-400"></i>
            </app-form-input>
          </div>

          <div class="mb-1">
            <app-form-input 
              label="Contraseña de Confirmación" 
              type="password"
              [(ngModel)]="password" 
              [hasIcon]="true"
              placeholder="Ingrese su contraseña"
              [required]="true"
              [error]="passwordError()"
              (input)="passwordError.set(null)">
              <i icon class="fas fa-lock text-slate-400"></i>
            </app-form-input>
          </div>
        </div>

        <div class="flex justify-end gap-3 pt-4 border-t border-slate-100 dark:border-slate-800">
          <button (click)="onClose()" 
                  class="px-5 py-2.5 rounded-xl text-sm font-bold text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
            Cancelar
          </button>
          <app-primary-button 
            [label]="mode() === 'sucursal' ? 'Cambiar Sucursal' : 'Cambiar Punto de Venta'"
            (btnClick)="confirmarCambio()" 
            [loading]="loading()">
          </app-primary-button>
        </div>
      </div>
    </app-modal>
  `
})
export class CambioPuntoComponent implements OnInit {
  @Output() cerrar = new EventEmitter<void>();

  mode = signal<'punto' | 'sucursal'>('sucursal');
  puntos = signal<any[]>([]);
  sucursalesLista = signal<any[]>([]);
  puntosDeSucursal = signal<any[]>([]);
  sucursalActual = signal<string>('');

  selectedId = signal<number | null>(null);
  selectedSucursalId = signal<number | null>(null);

  username = '';
  password = '';
  usernameError = signal<string | null>(null);
  passwordError = signal<string | null>(null);
  sucursalError = signal<string | null>(null);
  puntoError = signal<string | null>(null);
  loading = signal(false);

  constructor(
    private authService: AuthService,
    private puntoService: PuntoService,
    private alertService: AlertService
  ) { }

  ngOnInit() {
    this.sucursalActual.set(this.authService.getSucursalFromToken() || '');
    const userId = this.authService.getUserIdFromToken();
    if (userId) {
      // Cargar Puntos (para el shortcut de Punto de Venta)
      this.authService.getPuntosAutorizados(userId).subscribe({
        next: (res) => {
          if (res.success) {
            this.puntos.set(res.data.map((p: any) => ({
              id: p.idPunto || p.punto || p.id,
              nombre: p.nombrePunto || p.nombre
            })));
          }
        }
      });

      // Cargar Sucursales
      this.authService.getSucursalesAutorizadas(userId).subscribe({
        next: (res) => {
          if (res.success) {
            this.sucursalesLista.set(res.data.map((s: any) => ({
              id: s.idSucursal || s.id,
              nombre: s.nombreSucursal || s.nombre
            })));
          }
        }
      });
    }
  }

  changeMode(newMode: 'punto' | 'sucursal') {
    this.mode.set(newMode);
    this.selectedId.set(null);
    this.selectedSucursalId.set(null);
    this.puntosDeSucursal.set([]);
    this.password = '';
    this.usernameError.set(null);
    this.passwordError.set(null);
    this.sucursalError.set(null);
    this.puntoError.set(null);
  }

  onSucursalChange(sucursalId: number) {
    this.selectedId.set(null);
    this.puntosDeSucursal.set([]);
    if (sucursalId) {
      this.puntoService.listarTodosSinPaginacion(sucursalId).subscribe({
        next: (res) => {
          if (res.success) {
            this.puntosDeSucursal.set(res.data.map((p: any) => ({
              id: p.punto || p.id,
              nombre: p.nombre
            })));
          }
        }
      });
    }
  }

  onClose() {
    this.cerrar.emit();
  }

  confirmarCambio() {
    // Limpiar errores previos
    this.sucursalError.set(null);
    this.puntoError.set(null);
    this.usernameError.set(null);
    this.passwordError.set(null);

    let hasError = false;

    // 1. Validar Sucursal (Solo en modo sucursal)
    if (this.mode() === 'sucursal' && !this.selectedSucursalId()) {
      this.sucursalError.set('Debe seleccionar una sucursal');
      hasError = true;
    }

    // 2. Validar Punto de Venta (Siempre)
    if (!this.selectedId()) {
      this.puntoError.set('Debe seleccionar un punto de venta');
      hasError = true;
    }

    // 3. Validar Credenciales
    if (!this.username) {
      this.usernameError.set('Ingrese su usuario');
      hasError = true;
    }
    if (!this.password) {
      this.passwordError.set('Ingrese su contraseña');
      hasError = true;
    }

    if (hasError) {
      this.alertService.toast('Por favor complete todos los campos requeridos', 'warning');
      return;
    }

    const userId = this.authService.getUserIdFromToken();
    const currentUser = this.authService.currentUserValue;
    const puntoId = this.selectedId();

    if (!userId || !puntoId) return;

    // Verificación de usuario manual
    if (this.username.toLowerCase() !== currentUser?.username.toLowerCase()) {
      this.usernameError.set('El usuario de verificación no coincide');
      this.alertService.toast('El usuario de verificación no coincide', 'error');
      return;
    }

    this.loading.set(true);

    // En ambos casos usamos seleccionarPunto porque el usuario SIEMPRE elige un punto ahora
    this.authService.seleccionarPunto(userId, puntoId, this.password).subscribe({
      next: (res) => {
        if (res.success) {
          this.alertService.toast('Cambio realizado correctamente', 'success');
          window.location.reload();
        } else {
          this.passwordError.set(res.message || 'Error al cambiar');
          this.alertService.toast(res.message || 'Error al cambiar', 'error');
        }
        this.loading.set(false);
      },
      error: (err) => {
        this.passwordError.set('Error de autenticación o contraseña incorrecta');
        this.alertService.toast('Error de autenticación o contraseña incorrecta', 'error');
        this.loading.set(false);
      }
    });
  }
}

