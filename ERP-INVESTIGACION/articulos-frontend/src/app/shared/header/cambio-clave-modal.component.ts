import { Component, EventEmitter, OnInit, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { AlertService } from '../../core/services/alert.service';
import { ModalComponent } from '../components/modal/modal';
import { PrimaryButtonComponent } from '../components/primary-button/primary-button';
import { FormInputComponent } from '../components/forms/form-input/form-input.component';

@Component({
  selector: 'app-cambio-clave-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    ModalComponent,
    PrimaryButtonComponent,
    FormInputComponent
  ],
  template: `
    <app-modal [isOpen]="true" 
               title="Cambiar Contraseña" 
               size="md" 
               (modalClose)="onClose()">
      <div class="bg-white dark:bg-slate-800">
        <p class="text-sm text-slate-500 dark:text-slate-400 mb-6 font-medium">
          Desea cambiar su contraseña? Por favor complete los siguientes campos. 
          Su nueva contraseña debe tener al menos 6 caracteres y ser diferente a la actual.
        </p>

        <form [formGroup]="passwordForm" (ngSubmit)="$event.preventDefault(); onSubmit()">
          <!-- Usuario -->
          <div class="mb-1">
            <app-form-input 
              label="Usuario" 
              formControlName="username"
              [hasIcon]="true"
              placeholder="Ingrese su usuario">
              <i icon class="fas fa-user text-slate-400"></i>
            </app-form-input>
          </div>

          <!-- Contraseña Actual --><!-- YA TIENE ICONO POR EDICIÓN DEL USUARIO PERO ASEGURO EL BLOQUE -->
          <div class="mb-1">
            <app-form-input 
              label="Contraseña Actual" 
              type="password"
              formControlName="oldPassword" 
              [hasIcon]="true"
              placeholder="Ingrese su contraseña actual"
              [required]="true"
              [error]="getErrorMessage('oldPassword')">
              <i icon class="fas fa-lock text-slate-400"></i>
            </app-form-input>
          </div>

          <!-- Nueva Contraseña -->
          <div class="mb-1">
            <app-form-input 
              label="Nueva Contraseña" 
              type="password"
              formControlName="newPassword" 
              [hasIcon]="true"
              placeholder="Ingrese su nueva contraseña"
              [required]="true"
              [error]="getErrorMessage('newPassword')">
              <i icon class="fas fa-key text-slate-400"></i>
            </app-form-input>
          </div>

          <!-- Confirmar Nueva Contraseña -->
          <div class="mb-1">
            <app-form-input 
              label="Confirmar Nueva Contraseña" 
              type="password"
              formControlName="confirmPassword" 
              [hasIcon]="true"
              placeholder="Repita su nueva contraseña"
              [required]="true"
              [error]="getErrorMessage('confirmPassword')">
              <i icon class="fas fa-check-circle text-slate-400"></i>
            </app-form-input>
          </div>

          <div class="flex justify-end gap-3 pt-4 border-t border-slate-100 dark:border-slate-800">
            <button type="button" (click)="onClose()" 
                    class="px-5 py-2.5 rounded-xl text-sm font-bold text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
              Cancelar
            </button>
            <app-primary-button 
              label="Cambiar Contraseña"
              (btnClick)="onSubmit()" 
              [loading]="loading()">
            </app-primary-button>
          </div>
        </form>
      </div>
    </app-modal>
  `
})
export class CambioClaveModalComponent implements OnInit {
  @Output() cerrar = new EventEmitter<void>();

  passwordForm: FormGroup;
  loading = signal(false);
  userId: number | null = null;
  username = signal<string>('');

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private alertService: AlertService
  ) {
    this.passwordForm = this.fb.group({
      username: ['', [Validators.required]],
      oldPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  ngOnInit() {
    const userJson = localStorage.getItem('currentUser');
    if (userJson) {
      const user = JSON.parse(userJson);
      this.userId = user.id;
      if (user.username) {
        this.passwordForm.patchValue({ username: user.username });
      }
    }
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  getErrorMessage(controlName: string): string | null {
    const control = this.passwordForm.get(controlName);
    if (control?.touched && control?.errors) {
      if (control.errors['required']) return 'Este campo es obligatorio';
      if (control.errors['minlength']) return 'Debe tener al menos 6 caracteres';
    }

    if (controlName === 'confirmPassword' && (this.passwordForm.get('confirmPassword')?.touched || this.passwordForm.touched) && this.passwordForm.errors?.['mismatch']) {
      return 'Las contraseñas no coinciden';
    }

    return null;
  }

  onClose() {
    this.cerrar.emit();
  }

  onSubmit() {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    if (!this.userId) {
      this.alertService.error('Error', 'No se pudo identificar al usuario');
      return;
    }

    this.loading.set(true);
    const { oldPassword, newPassword, confirmPassword } = this.passwordForm.value;

    this.userService.changePassword(this.userId, {
      oldPassword,
      newPassword,
      confirmPassword
    }).subscribe({
      next: () => {
        this.loading.set(false);
        this.alertService.success('¡Éxito!', 'Su contraseña ha sido cambiada correctamente.').then(() => {
          this.onClose();
        });
      },
      error: (err) => {
        this.loading.set(false);
        let msg = 'Ocurrió un error al cambiar la contraseña';
        if (err.error && err.error.message) {
          msg = err.error.message;
        }
        this.alertService.error('Error', msg);
      }
    });
  }
}
