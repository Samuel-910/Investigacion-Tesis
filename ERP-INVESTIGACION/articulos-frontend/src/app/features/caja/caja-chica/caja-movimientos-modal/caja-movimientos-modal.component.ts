import { Component, EventEmitter, Input, Output, signal, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { CajaChicaService } from '../../services/caja-chica.service';
import { Movimiento } from '../../models/caja-chica.model';
import { AlertService } from '../../../../core/services/alert.service';

@Component({
    selector: 'app-caja-movimientos-modal',
    standalone: true,
    imports: [CommonModule, ModalComponent, TablaGeneralComponent, PaginationComponent],
    templateUrl: './caja-movimientos-modal.component.html'
})
export class CajaMovimientosModalComponent implements OnChanges {
    @Input() isOpen = false;
    @Input() cajaId: number | null = null;
    @Output() modalClose = new EventEmitter<void>();

    movimientos = signal<Movimiento[]>([]);
    loading = signal(false);
    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);

    columns: Columna[] = [
        { field: 'fecha', header: 'Fecha', tipo: 'date', subField: [] },
        { field: 'tipo', header: 'Tipo', tipo: 'caja-status', subField: [] },
        { field: 'monto', header: 'Monto', tipo: 'currency', subField: [] },
        { field: 'descripcion', header: 'Descripción', tipo: 'text', subField: [] },
        { field: 'referencia', header: 'Referencia', tipo: 'text', subField: [] },
        { field: 'usuario', header: 'Cajero', tipo: 'text', subField: [] }
    ];

    constructor(
        private cajaService: CajaChicaService,
        private alertService: AlertService
    ) { }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['isOpen']?.currentValue === true && this.cajaId) {
            this.currentPage.set(0);
            this.cargarMovimientos();
        }
    }

    cargarMovimientos(): void {
        if (!this.cajaId) return;

        this.loading.set(true);
        this.cajaService.listarMovimientos(this.cajaId, this.currentPage(), this.pageSize()).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.movimientos.set(res.data.content);
                    this.totalElements.set(res.data.totalElements);
                    this.totalPages.set(res.data.totalPages);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error al cargar movimientos', 'error');
            }
        });
    }

    cambiarPagina(page: number): void {
        this.currentPage.set(page);
        this.cargarMovimientos();
    }

    onClose(): void {
        this.modalClose.emit();
    }
}
