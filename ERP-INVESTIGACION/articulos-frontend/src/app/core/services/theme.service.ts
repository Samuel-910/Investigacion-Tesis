import { Injectable, signal, effect } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class ThemeService {
    darkMode = signal<boolean>(false);

    constructor() {
        // 1. Cargar preferencia inicial
        const savedTheme = localStorage.getItem('theme');

        if (savedTheme) {
            this.darkMode.set(savedTheme === 'dark');
        } else {
            // Si no hay preferencia, usar la del sistema
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            this.darkMode.set(prefersDark);
        }

        // 2. Aplicar el tema cada vez que cambie la señal
        effect(() => {
            const isDark = this.darkMode();
            if (isDark) {
                document.documentElement.classList.add('dark');
                localStorage.setItem('theme', 'dark');
            } else {
                document.documentElement.classList.remove('dark');
                localStorage.setItem('theme', 'light');
            }
        });
    }

    toggleTheme() {
        this.darkMode.update(val => !val);
    }

    isDark() {
        return this.darkMode();
    }
}
