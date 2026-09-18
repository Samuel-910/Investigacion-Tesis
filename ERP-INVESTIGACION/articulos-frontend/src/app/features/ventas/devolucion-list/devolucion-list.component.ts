import { Component, OnInit, signal, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VentaRegistroService } from '../services/venta-registro.service';
import { HeaderComponent } from '../../../shared/header/header.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { AlertService } from '../../../core/services/alert.service';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { DrawerComponent } from '../../../shared/components/drawer/drawer.component';
import { FormInputComponent } from '../../../shared/components/forms/form-input/form-input.component';
import { SearchableSelectComponent } from '../../../shared/components/searchable-select/searchable-select.component';
import { AuthService } from '../../auth/services/auth.service';
import { PuntoDocumentoService } from '../../documentos/services/punto-documento.service';

@Component({
    selector: 'app-devolucion-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        TablaGeneralComponent,
        PaginationComponent,
        BreadcrumbComponent,
        DrawerComponent,
        PageHeaderComponent,
        FormInputComponent
    ],
    templateUrl: './devolucion-list.component.html'
})
export class DevolucionListComponent implements OnInit {
    @ViewChild('accionesTemplate', { static: true }) accionesTemplate!: TemplateRef<any>;

    ventas = signal<any[]>([]);
    loading = signal(false);

    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);

    // Filtros
    serie = signal('');
    numero = signal('');
    fechaDesde = signal(new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60000).toISOString().split('T')[0]);
    fechaHasta = signal(new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60000).toISOString().split('T')[0]);
    estado = signal('V');

    selectedVenta = signal<any>(null);
    isDevolucionModalOpen = signal(false);
    motivoDevolucion = signal('');
    motivoSunat = signal('07'); // Default: Devolucion por item
    isDrawerOpen = signal(false);

    // Modal Exito
    showModalExito = signal(false);
    ncReciente = signal<any>(null);
    plantillaSeleccionadaId = signal<number | null>(null);
    todasLasPlantillasVenta = signal<any[]>([]);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Procesos', route: '#' },
        { label: 'Devoluciones' }
    ];

    columns: Columna[] = [
        { field: 'tipoDocNombre', header: 'TIPO DOC', tipo: 'text', subField: [] },
        { field: 'numeroDocumento', header: 'DOCUMENTO', tipo: 'text', subField: [] },
        { field: 'nombrePaciente', header: 'PACIENTE', tipo: 'text', subField: [] },
        { field: 'fecha', header: 'FECHA', tipo: 'date', subField: [] },
        { field: 'itemsRestantes', header: 'CANT. DISPONIBLE', tipo: 'text', subField: [] },
        { field: 'total', header: 'TOTAL', tipo: 'currency', subField: [] },
        { field: 'estadoDescripcion', header: 'ESTADO', tipo: 'text', subField: [] }
    ];

    constructor(
        private ventaService: VentaRegistroService,
        private alertService: AlertService,
        public sidebarService: SidebarService,
        private puntoDocumentoService: PuntoDocumentoService,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        this.cargarVentas();
        this.cargarPlantillas();
    }

    cargarPlantillas(): void {
        const puntoId = this.authService.getPuntoIdFromToken();
        if (!puntoId) return;

        this.puntoDocumentoService.obtenerPorPunto(puntoId).subscribe({
            next: (res: any) => {
                if (res.success && res.data) {
                    // Filtrar documentos que son '07' (Nota de Crédito)
                    const docsVenta = res.data.filter((pd: any) => pd.tipoDoc === '07' && pd.idPlantilla);
                    this.todasLasPlantillasVenta.set(docsVenta.map((pd: any) => ({
                        id: pd.idPlantilla,
                        nombre: pd.plantillaNombre || `Plantilla ${pd.idPlantilla}`
                    })));

                    if (this.todasLasPlantillasVenta().length > 0) {
                        this.plantillaSeleccionadaId.set(this.todasLasPlantillasVenta()[0].id);
                    }
                }
            }
        });
    }

    cargarVentas(): void {
        this.loading.set(true);
        this.ventaService.search(
            this.currentPage(),
            this.pageSize(),
            this.serie(),
            this.numero(),
            '', // numeroDesde
            '', // numeroHasta
            this.fechaDesde(),
            this.fechaHasta(),
            '', // idVendedor
            '', // condicionPago
            this.estado(), // estado
            'FACTURA_BOLETA' // tipoDoc
        ).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const content = res.data.content || [];
                    const mappedContent = content.map((item: any) => {
                        let tipoNombre = item.tipoDoc;
                        if (item.tipoDoc === '01') tipoNombre = 'Factura';
                        else if (item.tipoDoc === '03') tipoNombre = 'Boleta';
                        else if (item.tipoDoc === '07') tipoNombre = 'Nota de Crédito';
                        else if (item.tipoDoc === '12') tipoNombre = 'Ticket';
                        
                        const itemsRestantes = (item.detalles || []).reduce((acc: number, d: any) => acc + (d.cantidad || 0), 0);
                        
                        return {
                            ...item,
                            tipoDocNombre: tipoNombre,
                            itemsRestantes: itemsRestantes
                        };
                    });
                    this.ventas.set(mappedContent);
                    this.totalElements.set(res.data.totalElements || 0);
                    this.totalPages.set(res.data.totalPages || 0);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error al cargar ventas', 'error');
            }
        });
    }

    abrirDevolucion(venta: any): void {
        const id = venta.idVenta;
        if (!id) {
            this.alertService.toast('ID de venta no válido', 'error');
            return;
        }

        this.loading.set(true);
        this.ventaService.obtenerPorId(id).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const ventaData = res.data;
                    ventaData.detalles = ventaData.detalles.map((d: any) => {
                        const disponible = d.cantidad || 0;
                        return {
                            ...d,
                            cantidadADevolver: disponible,
                            maxDevolver: disponible
                        };
                    });
                    this.selectedVenta.set(ventaData);
                    this.motivoDevolucion.set('');
                    this.isDevolucionModalOpen.set(true);
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error al obtener detalles de la venta', 'error');
            }
        });
    }

    procesarDevolucion(): void {
        const venta = this.selectedVenta();
        const itemsParaDevolver = venta.detalles
            .filter((d: any) => d.cantidadADevolver > 0)
            .map((d: any) => ({
                idDetalle: d.idDetalle || d.id,
                cantidad: d.cantidadADevolver
            }));

        if (itemsParaDevolver.length === 0) {
            this.alertService.toast('Debe ingresar al menos una cantidad a devolver', 'warning');
            return;
        }

        // Validar cantidades
        for (const item of venta.detalles) {
            if (item.cantidadADevolver > item.maxDevolver) {
                this.alertService.toast(`La cantidad a devolver de ${item.descripcion} excede lo disponible`, 'error');
                return;
            }
        }

        this.alertService.confirm('¿Confirmar Devolución?', 'Esta acción afectará el stock y la caja chica.').then(result => {
            if (result.isConfirmed) {
                this.loading.set(true);
                this.ventaService.realizarDevolucion(venta.idVenta, {
                    motivo: this.motivoDevolucion(),
                    motivoSunat: this.motivoSunat(),
                    detalles: itemsParaDevolver
                }).subscribe({
                    next: (res) => {
                        if (res.success) {
                            this.isDevolucionModalOpen.set(false);
                            this.cargarVentas();
                            this.ncReciente.set(res.data);
                            this.showModalExito.set(true);
                        } else {
                            this.alertService.toast(res.message || 'Error al procesar devolución', 'error');
                        }
                        this.loading.set(false);
                    },
                    error: (err) => {
                        this.loading.set(false);
                        console.error('Error al procesar devolución:', err);
                        const msg = err.error?.message || 'Error de conexión';
                        this.alertService.toast(msg, 'error');
                    }
                });
            }
        });
    }

    onFiltrar(): void {
        this.isDrawerOpen.set(false);
        this.currentPage.set(0);
        this.cargarVentas();
    }

    cambiarPagina(page: number): void {
        this.currentPage.set(page);
        this.cargarVentas();
    }

    imprimirNC(): void {
        if (!this.ncReciente()) return;

        if (!this.plantillaSeleccionadaId()) {
            this.alertService.toast('No tienes ninguna plantilla asignada para "Notas de Crédito" en este Punto de Emisión. Asígnala en Configuración.', 'warning');
            return;
        }

        this.loading.set(true);
        this.ventaService.descargarPdf(this.ncReciente().idVenta, this.plantillaSeleccionadaId()!).subscribe({
            next: (res) => {
                if (res.body) {
                    const fileURL = URL.createObjectURL(res.body);
                    window.open(fileURL, '_blank');
                } else {
                    this.alertService.toast('Error al generar PDF', 'error');
                }
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.alertService.toast('Error al imprimir', 'error');
            }
        });
    }
}
