import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ConvenioService } from '../../services/convenio.service';
import { AlertService } from '../../../../../core/services/alert.service';
import { SidebarService } from '../../../../../shared/sidebar/sidebar.service';
import { HeaderComponent } from '../../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../../../shared/components/tabla-general/tabla-general.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../../shared/components/breadcrumb/breadcrumb';
import { PrimaryButtonComponent } from '../../../../../shared/components/primary-button/primary-button';
import { ModalComponent } from '../../../../../shared/components/modal/modal';
import { DescuentoFormComponent } from '../../descuento/descuento-form/descuento-form.component';

@Component({
    selector: 'app-convenio-detalle',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        TablaGeneralComponent,
        BreadcrumbComponent,
        PrimaryButtonComponent,
        ModalComponent,
        DescuentoFormComponent
    ],
    templateUrl: './convenio-detalle.component.html'
})
export class ConvenioDetalleComponent implements OnInit {
    compania = signal<any>(null);
    vinculos = signal<any[]>([]);
    descuentos = signal<any[]>([]);
    loading = signal(false);
    activeTab = signal<'usuarios' | 'descuentos'>('usuarios');
    showDescuentoModal = signal(false);
    idCompania!: number;

    // Buscar paciente
    busquedaPaciente = '';
    buscando = false;

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Descuentos', route: '/descuentos/mantenimiento' },
        { label: 'Convenios', route: '/descuentos/convenio' },
        { label: 'Detalle' }
    ];

    columnsUsuarios: Columna[] = [
        { field: 'persona.nombre', header: 'Nombre', tipo: 'text', subField: ['persona', 'nombre'] },
        { field: 'persona.apepat', header: 'Apellido', tipo: 'text', subField: ['persona', 'apepat'] },
        { field: 'nroPoliza', header: 'N° Póliza', tipo: 'text', subField: [] },
        { field: 'tipoAfiliacion', header: 'Afiliación', tipo: 'text', subField: [] },
        { field: 'fechaInicio', header: 'Desde', tipo: 'text', subField: [] },
        { field: 'fechaVencimiento', header: 'Hasta', tipo: 'text', subField: [] },
        { field: 'activo', header: 'Activo', tipo: 'status', subField: [] }
    ];

    columnsDescuentos: Columna[] = [
        {
            field: 'nombre', header: 'Descuento', tipo: 'layered-info', subField: [],
            layeredConfig: [
                { field: 'nombre', class: 'font-bold text-gray-900 dark:text-white' },
                { field: 'descripcion', class: 'text-xs text-gray-500 italic', format: 'text' }
            ]
        },
        { field: 'valorDescuento', header: 'Valor', tipo: 'text', subField: [] },
        { field: 'tipoDescuento', header: 'Tipo', tipo: 'text', subField: [] },
        { field: 'fechaInicio', header: 'Inicio', tipo: 'date', subField: [] },
        { field: 'fechaFin', header: 'Fin', tipo: 'date', subField: [] },
        { field: 'activo', header: 'Estado', tipo: 'status', subField: [] }
    ];

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private convenioService: ConvenioService,
        private alertService: AlertService,
        public sidebarService: SidebarService
    ) { }

    ngOnInit(): void {
        this.idCompania = Number(this.route.snapshot.paramMap.get('id'));
        this.cargarDatos();
    }

    cargarDatos(): void {
        this.loading.set(true);
        this.convenioService.obtenerCompania(this.idCompania).subscribe({
            next: (c) => {
                this.compania.set(c);
                this.breadcrumbItems = [
                    { label: 'Descuentos', route: '/descuentos/mantenimiento' },
                    { label: 'Convenios', route: '/descuentos/convenio' },
                    { label: c.nombre || 'Compañía' }
                ];
                this.loading.set(false);
            }
        });
        this.cargarVinculos();
        this.cargarDescuentos();
    }

    cargarVinculos(): void {
        this.convenioService.obtenerVinculos(this.idCompania).subscribe({
            next: (data) => this.vinculos.set(data || []),
            error: () => this.alertService.toast('Error al cargar usuarios vinculados', 'error')
        });
    }

    cargarDescuentos(): void {
        this.convenioService.listarDescuentos(this.idCompania).subscribe({
            next: (res: any) => {
                const content = res?.data?.content || res?.data || res || [];
                const formatted = content.map((d: any) => {
                    const primerDetalle = d.detalles && d.detalles.length > 0 ? d.detalles[0] : null;
                    const tieneMas = d.detalles && d.detalles.length > 1;

                    let valorTxt = '-';
                    let tipoTxt = '-';
                    if (primerDetalle) {
                        valorTxt = primerDetalle.tipoDescuento === 'PORCENTAJE'
                            ? `${primerDetalle.valorDescuento}%`
                            : `S/ ${primerDetalle.valorDescuento}`;
                        if (tieneMas) valorTxt += ' (+)';

                        tipoTxt = primerDetalle.tipoDescuento;
                        if (tieneMas) tipoTxt += ' (+)';
                    }

                    return {
                        ...d,
                        valorDescuento: valorTxt,
                        tipoDescuento: tipoTxt
                    };
                });
                this.descuentos.set(formatted);
            },
            error: () => this.alertService.toast('Error al cargar descuentos', 'error')
        });
    }

    setTab(tab: 'usuarios' | 'descuentos'): void {
        this.activeTab.set(tab);
    }

    abrirModalDescuento(): void {
        this.showDescuentoModal.set(true);
    }

    cerrarModalDescuento(): void {
        this.showDescuentoModal.set(false);
    }

    onDescuentoGuardado(): void {
        this.cerrarModalDescuento();
        this.cargarDescuentos();
        this.alertService.toast('Descuento creado correctamente', 'success');
    }

    volver(): void {
        this.router.navigate(['/descuentos/convenio']);
    }
}
