import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Producto } from '../../models/producto.model';
import { KardexService } from '../../service/kardex.service';

@Component({
    selector: 'app-producto-servicio-detail',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './producto-detail.component.html'
})
export class ProductoServicioDetailComponent {
    @Input() producto: Producto | null = null;
    @Output() cerrar = new EventEmitter<void>();

    activeTab = signal<'general' | 'stock' | 'precios' | 'sanitario' | 'kardex'>('general');
    movimientos = signal<any[]>([]);
    loadingKardex = signal(false);

    constructor(private kardexService: KardexService) { }

    close(): void {
        this.cerrar.emit();
    }

    setActiveTab(tab: 'general' | 'stock' | 'precios' | 'sanitario' | 'kardex'): void {
        this.activeTab.set(tab);
        if (tab === 'kardex' && this.movimientos().length === 0) {
            this.cargarKardex();
        }
    }

    cargarKardex(): void {
        const idSucursal = this.producto?.idSucursal;
        if (this.producto?.idCatalogo && idSucursal) {
            this.loadingKardex.set(true);
            this.kardexService.getMovimientos(this.producto.idCatalogo, idSucursal, 0, 50).subscribe({
                next: (res) => {
                    if (res.success) {
                        this.movimientos.set(res.data.content);
                    }
                    this.loadingKardex.set(false);
                },
                error: () => this.loadingKardex.set(false)
            });
        }
    }
}
