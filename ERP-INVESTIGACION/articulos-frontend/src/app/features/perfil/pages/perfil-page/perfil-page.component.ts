import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../../../features/configuraciones/services/user.service';
import { AuthService } from '../../../../features/auth/services/auth.service';
import { UserResponse } from '../../../../features/configuraciones/models/user.model';
import { AlertService } from '../../../../core/services/alert.service';
import { CambioClaveModalComponent } from '../../../../shared/header/cambio-clave-modal.component';

@Component({
  selector: 'app-perfil-page',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, CambioClaveModalComponent],
  templateUrl: './perfil-page.component.html',
  styleUrls: ['./perfil-page.component.css']
})
export class PerfilPageComponent implements OnInit {
  activeTab: 'datos' | 'permisos' = 'datos';
  userProfile: UserResponse | null = null;
  profileForm: FormGroup;
  
  isLoading = true;
  isSaving = false;
  showCambioClave = false;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private fb: FormBuilder,
    private alertService: AlertService
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      apemat: [''],
      email: ['', [Validators.required, Validators.email]],
      phone: ['']
    });
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.isLoading = true;
    this.userService.getMe().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.userProfile = res.data;
          this.profileForm.patchValue({
            firstName: this.userProfile.firstName,
            lastName: this.userProfile.lastName,
            email: this.userProfile.email,
            phone: this.userProfile.phone
          });
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading profile', err);
        this.alertService.error('Error', 'No se pudo cargar la información del perfil');
        this.isLoading = false;
      }
    });
  }

  saveProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.userService.updateMe(this.profileForm.value).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.userProfile = res.data;
          this.alertService.success('Éxito', 'Perfil actualizado correctamente');
          // Actualizar el estado en AuthService si es necesario
          const currentUser = this.authService.currentUserValue;
          if (currentUser) {
            currentUser.firstName = this.userProfile.firstName;
            currentUser.lastName = this.userProfile.lastName;
            // update local storage and subject implicitly if needed, or wait for token refresh
          }
        }
        this.isSaving = false;
      },
      error: (err) => {
        console.error('Error updating profile', err);
        this.alertService.error('Error', 'Ocurrió un error al actualizar el perfil');
        this.isSaving = false;
      }
    });
  }

  openChangePassword(): void {
    this.showCambioClave = true;
  }

  closeChangePassword(): void {
    this.showCambioClave = false;
  }
}
