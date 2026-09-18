import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { UserService, UserResponse } from '../../../../core/services/user.service';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { AlertService } from '../../../../core/services/alert.service';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { BreadcrumbComponent } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';

@Component({
    selector: 'app-cambio-clave',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        FormInputComponent,
        BreadcrumbComponent,
        PageHeaderComponent
    ],
    templateUrl: './cambio-clave.component.html'
})
export class CambioClaveComponent implements OnInit {
    tituloPagina = 'Cambio de Claves';
    breadcrumbItems = [
        { label: 'Configuraciones', path: '/configuraciones', icon: 'settings' },
        { label: 'Seguridad', path: '/configuraciones/seguridad', icon: 'shield' },
        { label: 'Cambio de Claves', path: '/configuraciones/seguridad/cambios-clave', icon: 'key' }
    ];

    // Vista y Búsqueda
    usuarios: UserResponse[] = [];
    totalUsuarios = 0;
    busqueda = '';
    loading = false;

    // Usuario seleccionado para reset
    usuarioSeleccionado: UserResponse | null = null;

    // Formulario de Reset
    resetForm: FormGroup;
    loadingReset = false;

    constructor(
        private fb: FormBuilder,
        private userService: UserService,
        public sidebarService: SidebarService,
        private alertService: AlertService,
        private cdr: ChangeDetectorRef
    ) {
        this.resetForm = this.fb.group({
            newPassword: ['', [Validators.required, Validators.minLength(6)]],
            confirmPassword: ['', [Validators.required]]
        }, {
            validators: this.passwordMatchValidator
        });
    }

    ngOnInit(): void {
        this.cargarUsuarios();
    }

    cargarUsuarios() {
        this.loading = true;
        this.userService.getActiveUsers(0, 5).subscribe({
            next: (res: any) => {
                this.usuarios = res.content || [];
                this.totalUsuarios = res.totalElements || 0;
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.loading = false;
                this.alertService.error('Error', 'No se pudieron cargar los usuarios');
            }
        });
    }

    buscarUsuarios() {
        if (!this.busqueda.trim()) {
            this.cargarUsuarios();
            return;
        }

        this.loading = true;
        this.userService.searchUsers(this.busqueda).subscribe({
            next: (res: any) => {
                this.usuarios = res.content || [];
                this.totalUsuarios = res.totalElements || 0;
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.loading = false;
            }
        });
    }

    seleccionarUsuario(user: UserResponse) {
        this.usuarioSeleccionado = user;
        this.resetForm.reset();
    }

    cancelarReset() {
        this.usuarioSeleccionado = null;
    }

    passwordMatchValidator(g: FormGroup) {
        return g.get('newPassword')?.value === g.get('confirmPassword')?.value
            ? null : { mismatch: true };
    }

    onResetSubmit() {
        if (this.resetForm.invalid || !this.usuarioSeleccionado) {
            this.resetForm.markAllAsTouched();
            return;
        }

        this.alertService.confirm(
            `¿Resetear contraseña de ${this.usuarioSeleccionado.username}?`,
            'Esta acción cambiará la contraseña del usuario sin requerir la actual.',
            'Sí, resetear',
            'Cancelar'
        ).then(result => {
            if (result.isConfirmed) {
                this.ejecutarReset();
            }
        });
    }

    private ejecutarReset() {
        this.loadingReset = true;
        const { newPassword, confirmPassword } = this.resetForm.value;

        this.userService.adminResetPassword(this.usuarioSeleccionado!.id, {
            newPassword,
            confirmPassword
        }).subscribe({
            next: () => {
                this.loadingReset = false;
                this.alertService.success('¡Éxito!', `Contraseña de ${this.usuarioSeleccionado?.username} actualizada.`).then(() => {
                    this.cancelarReset();
                });
            },
            error: (err) => {
                this.loadingReset = false;
                const msg = err.error?.message || 'Error al resetear contraseña';
                this.alertService.error('Error', msg);
            }
        });
    }
}
