import { Component, OnInit, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, distinctUntilChanged, switchMap, finalize } from 'rxjs';
import { UserService, UserResponse } from '../../../../../core/services/user.service';
import { UserImportService } from '../../../../../core/services/user-import.service';

@Component({
    selector: 'app-venta-sidebar',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './venta-sidebar.component.html'
})
export class VentaSidebarComponent implements OnInit {
    searchTerm: string = '';
    results: UserResponse[] = [];
    initialResults: UserResponse[] = []; // Para restaurar cuando se borra la búsqueda
    isLoading: boolean = false;
    selectedUserId: number | null = null;

    @Output() onUserSelected = new EventEmitter<UserResponse>();

    private searchSubject = new Subject<string>();

    constructor(
        private userService: UserService,
        private importService: UserImportService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.loadInitialUsers();
        this.setupSearch();
    }

    selectUser(user: UserResponse): void {
        this.selectedUserId = user.id;
        this.onUserSelected.emit(user);
        this.cdr.detectChanges();
    }

    private loadInitialUsers(): void {
        this.isLoading = true;
        this.userService.getActiveUsers(0, 10).pipe(
            finalize(() => {
                this.isLoading = false;
                this.cdr.detectChanges();
            })
        ).subscribe({
            next: (response) => {
                if (response && response.content) {
                    this.initialResults = response.content;
                    this.results = [...this.initialResults];
                }
            },
            error: (err) => console.error('Error cargando iniciales:', err)
        });
    }

    private setupSearch(): void {
        this.searchSubject.pipe(
            distinctUntilChanged(),
            switchMap(term => {
                if (term.length < 2) {
                    // Si se borra la búsqueda, restauramos los iniciales
                    this.results = [...this.initialResults];
                    this.cdr.detectChanges();
                    return [];
                }
                this.isLoading = true;
                this.cdr.detectChanges();
                return this.userService.searchUsers(term).pipe(
                    finalize(() => {
                        this.isLoading = false;
                        this.cdr.detectChanges();
                    })
                );
            })
        ).subscribe({
            next: (response) => {
                if (response && response.content) {
                    this.results = response.content;
                    this.cdr.detectChanges();
                }
            },
            error: (err) => {
                console.error('Error en búsqueda:', err);
                this.isLoading = false;
                this.cdr.detectChanges();
            }
        });
    }

    onSearch(event: any): void {
        const term = event.target.value;
        this.searchSubject.next(term);
    }

    onFileSelected(event: any): void {
        const file = event.target.files[0];
        if (file) {
            this.importService.importFromExcel(file).subscribe({
                next: (users) => {
                    // Limpiar búsqueda
                    this.searchTerm = '';
                    // Añadir a los resultados (al inicio)
                    this.results = [...users, ...this.initialResults];
                    this.cdr.detectChanges();

                    // Auto-seleccionar el PRIMER usuario importado si existe
                    if (users.length > 0) {
                        this.selectUser(users[0]);
                    }
                },
                error: (err) => console.error('Error importando:', err)
            });
        }
    }
}
