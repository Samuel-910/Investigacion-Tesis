import { Injectable, inject } from '@angular/core';
import Swal, { SweetAlertIcon, SweetAlertResult } from 'sweetalert2';
import { ThemeService } from './theme.service';

@Injectable({
    providedIn: 'root'
})
export class AlertService {
    private themeService = inject(ThemeService);

    constructor() { }

    private get baseConfig() {
        const isDark = this.themeService.darkMode();
        return {
            background: isDark ? '#1e293b' : '#ffffff',
            color: isDark ? '#ffffff' : '#1f2937',
            customClass: {
                popup: 'bg-white dark:bg-slate-800 dark:text-white rounded-2xl shadow-xl border border-gray-100 dark:border-slate-700',
                title: 'text-gray-900 dark:text-white',
                htmlContainer: 'text-gray-600 dark:text-gray-300',
                confirmButton: 'bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl px-5 py-2.5',
                cancelButton: 'bg-red-600 hover:bg-red-700 text-white font-medium rounded-xl px-5 py-2.5',
                actions: 'gap-3' // Space between buttons
            },
            buttonsStyling: false // Important to disable default styles so Tailwind works
        };
    }

    success(title: string, text?: string) {
        return Swal.fire({
            ...this.baseConfig,
            title: title,
            text: text,
            icon: 'success',
            confirmButtonText: 'Aceptar'
        });
    }

    error(title: string, text?: string) {
        return Swal.fire({
            ...this.baseConfig,
            title: title,
            text: text,
            icon: 'error',
            confirmButtonText: 'Cerrar'
        });
    }

    warning(title: string, text?: string) {
        return Swal.fire({
            ...this.baseConfig,
            title: title,
            text: text,
            icon: 'warning',
            confirmButtonText: 'Entendido'
        });
    }

    info(title: string, text?: string) {
        return Swal.fire({
            ...this.baseConfig,
            title: title,
            text: text,
            icon: 'info',
            confirmButtonText: 'Aceptar'
        });
    }

    confirm(title: string, text: string, confirmText: string = 'Sí, continuar', cancelText: string = 'Cancelar'): Promise<SweetAlertResult<any>> {
        return Swal.fire({
            ...this.baseConfig,
            title: title,
            text: text,
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: confirmText,
            cancelButtonText: cancelText,
            reverseButtons: true
        });
    }

    custom(options: any) {
        return Swal.fire({
            ...this.baseConfig,
            ...options,
            customClass: {
                ...this.baseConfig.customClass,
                ...(options.customClass || {}) // Merge custom classes if provided
            }
        });
    }

    toast(title: string, icon: SweetAlertIcon = 'success') {
        const isDark = this.themeService.darkMode();
        return Swal.fire({
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: true,
            icon: icon,
            title: title,
            background: isDark ? '#1e293b' : '#ffffff', // Use theme service
            color: isDark ? '#ffffff' : '#1f2937',
            customClass: {
                popup: 'colored-toast' // Optional: Define specific toast styles
            }
        });
    }

    loading(title: string = 'Procesando...', text: string = 'Por favor espere') {
        return Swal.fire({
            ...this.baseConfig,
            title: title,
            text: text,
            allowOutsideClick: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });
    }

    prompt(title: string, text: string, inputType: 'text' | 'number' | 'password' = 'text'): Promise<SweetAlertResult<any>> {
        return Swal.fire({
            ...this.baseConfig,
            title: title,
            text: text,
            input: inputType,
            showCancelButton: true,
            confirmButtonText: 'Aceptar',
            cancelButtonText: 'Cancelar',
            reverseButtons: true,
            inputValidator: (value) => {
                if (!value) {
                    return 'Este campo es obligatorio';
                }
                return null;
            }
        });
    }

    close() {
        Swal.close();
    }
}
