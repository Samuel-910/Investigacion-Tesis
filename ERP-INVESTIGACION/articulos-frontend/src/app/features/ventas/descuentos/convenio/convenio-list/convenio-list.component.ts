import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConvenioService } from '../../services/convenio.service';
import { AlertService } from '../../../../../core/services/alert.service';
import { SidebarService } from '../../../../../shared/sidebar/sidebar.service';
import { HeaderComponent } from '../../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../../shared/sidebar/sidebar.component';
import { TablaGeneralComponent, Columna } from '../../../../../shared/components/tabla-general/tabla-general.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../../shared/components/breadcrumb/breadcrumb';
import { PrimaryButtonComponent } from '../../../../../shared/components/primary-button/primary-button';
import { ModalComponent } from '../../../../../shared/components/modal/modal';
import { UserImportService } from '../../../../../core/services/user-import.service';
import { UserService } from '../../../../../core/services/user.service';
import { DescuentoFormComponent } from '../../descuento/descuento-form/descuento-form.component';
import { FormInputComponent } from '../../../../../shared/components/forms/form-input/form-input.component';
import { FormSelectComponent } from '../../../../../shared/components/forms/form-select/form-select.component';
import * as XLSX from 'xlsx';

@Component({
    selector: 'app-convenio-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        TablaGeneralComponent,
        PrimaryButtonComponent,
        BreadcrumbComponent,
        ModalComponent,
        DescuentoFormComponent,
        FormInputComponent,
        FormSelectComponent
    ],
    templateUrl: './convenio-list.component.html'
})
export class ConvenioListComponent implements OnInit {
    afiliacionOptions: any[] = [
        { label: 'TITULAR', value: 'TITULAR' },
        { label: 'DEPENDIENTE', value: 'DEPENDIENTE' },
        { label: 'FAMILIAR', value: 'FAMILIAR' },
        { label: 'CONYUGE', value: 'CONYUGE' }
    ];

    // Lista de compañías para el selector
    companias = signal<any[]>([]);
    companiasFiltradas = signal<any[]>([]);
    companiaSeleccionada = signal<any>(null);
    loading = signal(false);
    searchTerm = signal('');

    // Datos relacionados (Cargados al seleccionar)
    vinculos = signal<any[]>([]);
    descuentos = signal<any[]>([]);
    loadingDetalle = signal(false);

    // Estado UI
    activeTab = signal<'usuarios' | 'descuentos'>('usuarios');
    showDescuentoModal = signal(false);

    // Búsqueda de pacientes
    showPatientModal = signal(false);
    pacientesBusqueda = signal<any[]>([]);
    loadingPacientes = signal(false);
    searchTermPaciente = signal('');
    pacientesSeleccionados = signal<Set<any>>(new Set());

    // Datos del vínculo
    vinculoData = signal({
        nroPoliza: '',
        tipoAfiliacion: 'TITULAR',
        parentesco: 'TITULAR',
        fechaInicio: new Date().toISOString().split('T')[0],
        fechaVencimiento: '',
        activo: true
    });

    // Edición de vínculo
    showEditVinculoModal = signal(false);
    vinculoSeleccionado = signal<any>(null);

    private userService = inject(UserService);
    private importService = inject(UserImportService);

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Ventas', route: '/ventas' },
        { label: 'Descuentos', route: '/descuentos/lista' },
        { label: 'Convenios' }
    ];

    columnsUsuarios: Columna[] = [
        { field: 'persona', header: 'Paciente', tipo: 'object', subField: ['nombreCompleto'] },
        { field: 'persona', header: 'Documento', tipo: 'object', subField: ['numdoc'] },
        { field: 'nroPoliza', header: 'N° Póliza', tipo: 'text', subField: [] },
        { field: 'tipoAfiliacion', header: 'Afiliación', tipo: 'text', subField: [] },
        { field: 'fechaInicio', header: 'Inicio', tipo: 'text', subField: [] },
        { field: 'fechaVencimiento', header: 'Vencimiento', tipo: 'text', subField: [] },
        { field: 'activo', header: 'Estado', tipo: 'status', subField: [] }
    ];

    columnsDescuentos: Columna[] = [
        { field: 'nombre', header: 'Nombre', tipo: 'text', subField: [] },
        { field: 'valorDescuento', header: 'Valor', tipo: 'text', subField: [] },
        { field: 'tipoDescuento', header: 'Tipo', tipo: 'text', subField: [] },
        { field: 'fechaInicio', header: 'Inicio', tipo: 'date', subField: [] },
        { field: 'fechaFin', header: 'Fin', tipo: 'date', subField: [] },
        { field: 'activo', header: 'Estado', tipo: 'status', subField: [] }
    ];

    constructor(
        private convenioService: ConvenioService,
        private alertService: AlertService,
        public sidebarService: SidebarService
    ) { }

    ngOnInit(): void {
        this.cargarCompanias();
    }

    cargarCompanias(): void {
        this.loading.set(true);
        this.convenioService.listarCompanias().subscribe({
            next: (res) => {
                const data = res.data || res || [];
                this.companias.set(Array.isArray(data) ? data : data.content || []);
                this.companiasFiltradas.set(this.companias());
                this.loading.set(false);

                if (this.companias().length > 0) {
                    this.seleccionarCompania(this.companias()[0]);
                }
            },
            error: (err) => {
                this.loading.set(false);
                const errMsg = err.error?.message || err.message || 'Error desconocido';
                this.alertService.toast(`Error al cargar el listado general de compañías: ${errMsg}`, 'error');
            }
        });
    }

    onSearch(event: any): void {
        const term = (event.target as HTMLInputElement).value.toLowerCase();
        this.searchTerm.set(term);
        if (!term) {
            this.companiasFiltradas.set(this.companias());
            return;
        }
        const filtered = this.companias().filter(c =>
            c.nombre.toLowerCase().includes(term) ||
            (c.codigo && c.codigo.toLowerCase().includes(term))
        );
        this.companiasFiltradas.set(filtered);
    }

    seleccionarCompania(compania: any): void {
        this.companiaSeleccionada.set(compania);
        this.cargarDetalles(compania.id);
    }

    cargarDetalles(id: number): void {
        if (!id) return;
        this.loadingDetalle.set(true);

        const nombreCia = this.companiaSeleccionada()?.nombre || 'la compañía';

        this.convenioService.obtenerVinculos(id).subscribe({
            next: (res) => {
                let content = res?.data?.content || res?.data || res || [];
                if (!Array.isArray(content) && content.content) content = content.content;
                
                const data = Array.isArray(content) ? content : [];
                const formatted = data.map((v: any) => ({
                    ...v,
                    persona: {
                        nombreCompleto: v.pacienteNombreCompleto || '',
                        numdoc: v.pacienteDni || ''
                    },
                    activo: v.estado?.name === 'ACTIVO' || v.estado === 'ACTIVO' || v.estado?.valor === 1 || v.activo
                }));
                this.vinculos.set(formatted);
            },
            error: (err) => {
                const errMsg = err.error?.message || err.message || 'Error desconocido';
                this.alertService.toast(`Error al cargar beneficiarios de la compañía "${nombreCia}": ${errMsg}`, 'error');
            }
        });

        this.convenioService.listarDescuentos(id).subscribe({
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
                this.loadingDetalle.set(false);
            },
            error: (err) => {
                this.loadingDetalle.set(false);
                const errMsg = err.error?.message || err.message || 'Error desconocido';
                this.alertService.toast(`Error al cargar descuentos de la compañía "${nombreCia}": ${errMsg}`, 'error');
            }
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
        const id = this.companiaSeleccionada()?.id;
        if (id) this.cargarDetalles(id);
        this.alertService.success('Beneficio creado exitosamente');
    }

    abrirModalPacientes(): void {
        this.showPatientModal.set(true);
        this.buscarPacientes('');
    }

    cerrarModalPacientes(): void {
        this.showPatientModal.set(false);
        this.vinculoData.set({
            nroPoliza: '',
            tipoAfiliacion: 'TITULAR',
            parentesco: 'TITULAR',
            fechaInicio: new Date().toISOString().split('T')[0],
            fechaVencimiento: '',
            activo: true
        });
    }

    buscarPacientes(term: string): void {
        this.loadingPacientes.set(true);
        this.userService.searchUsers(term).subscribe({
            next: (res: any) => {
                const content = res.content || [];
                // Filtrar usuarios que representen una compañía
                const filtered = content.filter((p: any) => !p.isCompania);
                const pacientesConVinculo = filtered.map((p: any) => ({
                    ...p,
                    vinculo: { ...this.vinculoData() }
                }));
                this.pacientesBusqueda.set(pacientesConVinculo);
                this.loadingPacientes.set(false);
            },
            error: () => this.loadingPacientes.set(false)
        });
    }

    aplicarGeneralATodos(): void {
        const general = this.vinculoData();
        this.pacientesBusqueda.update(list => list.map(p => ({
            ...p,
            vinculo: { ...general }
        })));
        this.alertService.success('Datos generales aplicados a los resultados actuales');
    }

    vincularPacientes(pacientes?: any[]): void {
        const selected = (pacientes && pacientes.length > 0) ? pacientes : Array.from(this.pacientesSeleccionados());
        if (selected.length === 0) {
            this.alertService.warning('Seleccione al menos un paciente');
            return;
        }

        this.alertService.loading('Vinculando pacientes...');

        const vinculosDetalle = selected.map(p => ({
            idPaciente: p.id,
            ...p.vinculo
        }));

        this.convenioService.vincularPacientesMasivoDetalle(this.companiaSeleccionada().id, vinculosDetalle).subscribe({
            next: () => {
                this.alertService.success(`${selected.length} pacientes vinculados.`);
                this.cerrarModalPacientes();
                this.pacientesSeleccionados.set(new Set());
                this.cargarDetalles(this.companiaSeleccionada().id);
            },
            error: () => this.alertService.error('Error al vincular')
        });
    }

    toggleSeleccion(paciente: any): void {
        this.pacientesSeleccionados.update(set => {
            const newSet = new Set(set);
            if (newSet.has(paciente)) {
                newSet.delete(paciente);
            } else {
                newSet.add(paciente);
            }
            return newSet;
        });
    }

    isSeleccionado(paciente: any): boolean {
        return this.pacientesSeleccionados().has(paciente);
    }

    onExcelSelected(event: any): void {
        const file = event.target.files[0];
        if (!file) return;

        this.alertService.loading('Procesando archivo...');
        const reader: FileReader = new FileReader();
        reader.onload = (e: any) => {
            try {
                const bstr: string = e.target.result;
                const wb: XLSX.WorkBook = XLSX.read(bstr, { type: 'binary' });
                const wsname: string = wb.SheetNames[0];
                const ws: XLSX.WorkSheet = wb.Sheets[wsname];
                const data = XLSX.utils.sheet_to_json(ws, { header: 1 }) as any[];
                this.vincularDesdeExcelData(data);
            } catch (err) {
                this.alertService.error('Error al leer el archivo Excel');
            }
        };
        reader.onerror = () => {
            this.alertService.error('Error al cargar el archivo');
        };
        reader.readAsBinaryString(file);
    }

    private vincularDesdeExcelData(data: any[]): void {
        if (!data || data.length === 0) {
            this.alertService.error('El archivo Excel está vacío');
            return;
        }

        // Obtener cabeceras en minúsculas, limpias y normalizadas (sin tildes)
        const cabeceras = (data[0] || []).map((c: any) => String(c).toLowerCase().trim().normalize("NFD").replace(/[\u0300-\u036f]/g, ""));
        
        const colIndexDni = cabeceras.findIndex((c: any) => c === 'dni' || c === 'documento' || c === 'ruc');
        if (colIndexDni === -1) {
            this.alertService.error('Formato inválido: El archivo Excel debe contener una columna de cabecera llamada "DNI", "Documento" o "RUC".');
            return;
        }

        const colIndexPoliza = cabeceras.findIndex((c: any) => c === 'poliza' || c === 'nropoliza' || c === 'nro poliza' || c === 'n° poliza');
        const colIndexAfiliacion = cabeceras.findIndex((c: any) => c === 'afiliacion' || c === 'tipoafiliacion' || c === 'tipo afiliacion' || c === 'tipo');
        const colIndexInicio = cabeceras.findIndex((c: any) => c === 'inicio' || c === 'fechainicio' || c === 'fecha inicio');
        const colIndexVencimiento = cabeceras.findIndex((c: any) => c === 'vencimiento' || c === 'fechavencimiento' || c === 'fecha vencimiento' || c === 'fin' || c === 'fechafin' || c === 'fecha fin');

        const vinculosList: any[] = [];
        const rows = data.slice(1);

        const defaultDto = this.vinculoData();

        rows.forEach(row => {
            const dniVal = row[colIndexDni] ? String(row[colIndexDni]).trim() : null;
            if (dniVal) {
                const polizaVal = colIndexPoliza !== -1 && row[colIndexPoliza] ? String(row[colIndexPoliza]).trim() : defaultDto.nroPoliza;
                
                let afiliacionVal = defaultDto.tipoAfiliacion;
                if (colIndexAfiliacion !== -1 && row[colIndexAfiliacion]) {
                    const rawAfiliacion = String(row[colIndexAfiliacion]).trim().toUpperCase();
                    if (['TITULAR', 'DEPENDIENTE', 'FAMILIAR', 'CONYUGE'].includes(rawAfiliacion)) {
                        afiliacionVal = rawAfiliacion;
                    }
                }

                let inicioVal = defaultDto.fechaInicio;
                if (colIndexInicio !== -1 && row[colIndexInicio]) {
                    inicioVal = this.parseExcelDate(row[colIndexInicio]) || defaultDto.fechaInicio;
                }

                let vencimientoVal = defaultDto.fechaVencimiento;
                if (colIndexVencimiento !== -1 && row[colIndexVencimiento]) {
                    vencimientoVal = this.parseExcelDate(row[colIndexVencimiento]) || defaultDto.fechaVencimiento;
                }

                vinculosList.push({
                    dni: dniVal,
                    nroPoliza: polizaVal,
                    tipoAfiliacion: afiliacionVal,
                    parentesco: afiliacionVal,
                    fechaInicio: inicioVal,
                    fechaVencimiento: vencimientoVal,
                    activo: true
                });
            }
        });

        if (vinculosList.length === 0) {
            this.alertService.error('No se encontraron registros de pacientes con DNI válidos');
            return;
        }

        this.alertService.loading(`Vinculando ${vinculosList.length} pacientes...`);
        this.convenioService.vincularPacientesPorDniDetalle(this.companiaSeleccionada().id, vinculosList).subscribe({
            next: (res: any) => {
                const vinculados = res?.length || 0;
                if (vinculados === 0) {
                    this.alertService.warning('Ningún paciente fue vinculado', 'Por favor, asegúrese de que los DNIs ingresados en el Excel correspondan a pacientes ya registrados en el sistema.');
                } else {
                    this.alertService.success(`Proceso completado. ${vinculados} pacientes vinculados.`);
                }
                this.cargarDetalles(this.companiaSeleccionada().id);
            },
            error: (err: any) => {
                console.error(err);
                this.alertService.error('Error durante la vinculación detallada vía Excel');
            }
        });
    }

    private parseExcelDate(val: any): string | null {
        if (!val) return null;
        if (val instanceof Date) {
            return val.toISOString().split('T')[0];
        }
        if (typeof val === 'number') {
            const date = new Date(Math.round((val - 25569) * 86400 * 1000));
            return date.toISOString().split('T')[0];
        }
        const str = String(val).trim();
        if (/^\d{4}-\d{2}-\d{2}$/.test(str)) {
            return str;
        }
        const parts = str.split('/');
        if (parts.length === 3) {
            const day = parts[0].padStart(2, '0');
            const month = parts[1].padStart(2, '0');
            const year = parts[2];
            if (year.length === 4) {
                return `${year}-${month}-${day}`;
            }
        }
        return null;
    }

    updateVinculoData(field: string, event: any): void {
        const val = event.target.value;
        this.vinculoData.update(prev => ({ ...prev, [field]: val }));
    }

    updateVinculoDataSignal(field: string, val: any): void {
        this.vinculoData.update(prev => ({ ...prev, [field]: val }));
    }

    descargarPlantillaExcel(): void {
        const headers = [['DNI', 'Nombre', 'Poliza', 'Afiliacion', 'Inicio', 'Vencimiento']];
        const data = [
            ['77777771', 'Carlos Rodriguez', 'POL-99991', 'TITULAR', '2026-06-12', '2027-06-12'],
            ['77777772', 'Ana Fernandez', 'POL-99992', 'FAMILIAR', '2026-06-12', '2027-06-12']
        ];

        const worksheet = XLSX.utils.aoa_to_sheet([...headers, ...data]);
        const workbook = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(workbook, worksheet, 'Plantilla Beneficiarios');

        XLSX.writeFile(workbook, 'plantilla_convenio_beneficiarios.xlsx');
    }

    updateIndividualVinculo(paciente: any, field: string, event: any): void {
        const val = event.target.value;
        paciente.vinculo[field] = val;
    }

    // --- ACCIONES SOBRE VÍNCULOS EXISTENTES ---

    onEditVinculo(vinculo: any): void {
        this.vinculoSeleccionado.set({ ...vinculo });
        this.showEditVinculoModal.set(true);
    }

    onDeleteVinculo(vinculo: any): void {
        this.alertService.confirm(
            'Desvincular Paciente',
            `¿Está seguro de desvincular a ${vinculo.persona.nombreCompleto}?`,
            'Confirmar',
            'Cancelar'
        ).then(result => {
            if (result.isConfirmed) {
                this.alertService.loading('Desvinculando...');
                this.convenioService.eliminarVinculo(vinculo.id).subscribe({
                    next: () => {
                        this.alertService.success('Paciente desvinculado');
                        this.cargarDetalles(this.companiaSeleccionada().id);
                    },
                    error: () => this.alertService.error('No se pudo desvincular')
                });
            }
        });
    }

    guardarEdicionVinculo(): void {
        const vinculo = this.vinculoSeleccionado();
        if (!vinculo) return;

        this.alertService.loading('Guardando cambios...');
        const dto = {
            nroPoliza: vinculo.nroPoliza,
            tipoAfiliacion: vinculo.tipoAfiliacion,
            parentesco: vinculo.parentesco,
            fechaInicio: vinculo.fechaInicio,
            fechaVencimiento: vinculo.fechaVencimiento,
            activo: vinculo.activo
        };

        this.convenioService.actualizarVinculo(vinculo.id, dto).subscribe({
            next: () => {
                this.alertService.success('Datos actualizados');
                this.showEditVinculoModal.set(false);
                this.cargarDetalles(this.companiaSeleccionada().id);
            },
            error: () => this.alertService.error('Error al actualizar')
        });
    }
}
