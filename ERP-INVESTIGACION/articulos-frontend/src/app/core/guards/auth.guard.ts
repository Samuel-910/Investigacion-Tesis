import { Injectable } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../features/auth/services/auth.service';

@Injectable({
    providedIn: 'root'
})
export class AuthGuard {
    constructor(
        private authService: AuthService,
        private router: Router
    ) { }

    canActivate():
        | Observable<boolean | UrlTree>
        | Promise<boolean | UrlTree>
        | boolean
        | UrlTree {

        const currentUser = this.authService.currentUserValue;

        if (currentUser && currentUser.token) {
            // Usuario autenticado, permitir acceso
            return true;
        }

        // No autenticado, redirigir al login
        this.router.navigate(['/login']);
        return false;
    }

    canActivateChild():
        | Observable<boolean | UrlTree>
        | Promise<boolean | UrlTree>
        | boolean
        | UrlTree {
        return this.canActivate();
    }
}
