import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef, inject, ViewEncapsulation, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';

// GrapesJS Core y Plugins
import grapesjs from 'grapesjs';
import gjsBasicBlocks from 'grapesjs-blocks-basic';
import gjsForms from 'grapesjs-plugin-forms';
import gjsImageEditor from 'grapesjs-tui-image-editor';
import gjsGradient from 'grapesjs-style-gradient';
import gjsFilters from 'grapesjs-style-filter';
import gjsTouch from 'grapesjs-touch';
import myTailwindPlugin from '../../../../shared/grapesjs-plugins/tailwind';
// Configuración y Módulos Propios
import { GJS_CONFIG_BASE, CANVAS_STYLES } from './config/gjs-config';
import { registerTableCommands } from './commands/table-commands';
import { CanvasManager } from './utils/canvas-manager';
import { HeaderComponent } from '../../../../shared/header/header.component';
import { SidebarComponent } from '../../../../shared/sidebar/sidebar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { AlertService } from '../../../../core/services/alert.service';
import { FormatoService } from '../../services/formato.service';
import { DocumentoService } from '../../services/documento.service';
import { TraductorPlantillaService } from '../../services/traductor-plantilla.service';
import { PuntoDocumentoService } from '../../services/punto-documento.service';
import { DocumentoFormato, Modulo, Plantilla } from '../../models/documento.model';


@Component({
    selector: 'app-documento-grapejs',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        HeaderComponent,
        SidebarComponent,
        BreadcrumbComponent,
        PrimaryButtonComponent,
        ModalComponent
    ],
    templateUrl: './documento-grapejs.component.html',
    styleUrls: ['./documento-grapejs.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class DocumentoGrapejsComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('gjs') gjsElement!: ElementRef;

    public editor: any;
    private canvasManager!: CanvasManager;

    public sidebarService = inject(SidebarService);
    private alertService = inject(AlertService);
    private formatoService = inject(FormatoService);
    private documentoService = inject(DocumentoService);
    private traductorService = inject(TraductorPlantillaService);
    private puntoDocService = inject(PuntoDocumentoService);
    private route = inject(ActivatedRoute);
    private router = inject(Router);

    // Signals para estado Reactivo
    nombre = signal<string>('Nuevo Documento');
    formatoId = signal<number | null>(null);
    orientacion = signal<string>('Vertical');
    loading = signal<boolean>(false);
    plantillaId = signal<number | null>(null);
    modulo = signal<Modulo | null>(null);
    tipoDocumento = signal<any>(null);
    isDefault = signal<boolean>(false);
    showModalGuardar = signal<boolean>(false);
    showHelp = signal<boolean>(false);

    listadoFormatos = signal<DocumentoFormato[]>([]);
    listadoTipos = signal<any[]>([]);
    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Documentos', route: '/documentos' },
        { label: 'Editor de Plantillas' }
    ];

    constructor() {
        effect(() => {
            if (this.editor && (this.formatoId() || this.orientacion() || this.listadoFormatos().length > 0)) {
                this.actualizarAreaTrabajo();
            }
        });
    }

    copyId = signal<number | null>(null);

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        this.route.queryParams.subscribe(params => {
            if (id) {
                this.plantillaId.set(+id);
                this.loading.set(true);
            } else if (params['nuevo']) {
                if (params['nombre']) this.nombre.set(params['nombre']);
                if (params['modulo']) this.modulo.set(params['modulo']);
                if (params['tipo']) {
                    const rawTipo = params['tipo'];
                    // Sanitización inmediata ante cualquier entrada
                    const sanitizado = (typeof rawTipo === 'object' && rawTipo !== null) ? (rawTipo.tipoDoc || rawTipo.id) : rawTipo;
                    this.tipoDocumento.set(sanitizado);
                }
                if (params['formatoId']) this.formatoId.set(+params['formatoId']);
                if (params['orientacion']) this.orientacion.set(params['orientacion']);
                if (params['copyId']) this.copyId.set(+params['copyId']);
                if (params['copy']) this.copyId.set(+params['copy']); // Added 'copy' param as an alternative to 'copyId'
                if (this.copyId()) this.loading.set(true);
            }
        });
    }

    ngAfterViewInit(): void {
        setTimeout(() => this.cargarDatosIniciales(), 0);
    }

    private cargarDatosIniciales(): void {
        this.loading.set(true);

        // 1. Cargar Formatos y Tipos Base
        forkJoin({
            formatos: this.formatoService.listar(),
            tipos: this.puntoDocService.obtenerTiposDocumentos()
        }).subscribe({
            next: (res: any) => {
                this.listadoFormatos.set(res.formatos.success ? res.formatos.data : []);
                this.listadoTipos.set(res.tipos.success ? res.tipos.data : []);
                // 2. Cargar Plantilla o Copia si existe
                if (this.plantillaId() || this.copyId()) {
                    const targetId = this.plantillaId() || this.copyId();
                    this.documentoService.obtenerPlantilla(targetId!).subscribe({
                        next: (tpRes: any) => {
                            const p = tpRes.data;
                            if (this.plantillaId()) {
                                this.nombre.set(p.nombre);
                                this.isDefault.set(p.isDefault || false);
                            }
                            this.formatoId.set(p.formato?.id || null);
                            this.orientacion.set(p.orientacion || 'Vertical');

                            // IMPORTANTE: Primero setear el módulo para que el listado de bloques sea el correcto
                            this.modulo.set(p.modulo);

                            // Sanitización profunda para evitar [OBJECT OBJECT] en la UI
                            const tDoc = p.tipoDocumento;
                            let tDocId = (typeof tDoc === 'object' && tDoc !== null) ? (tDoc.tipoDoc || tDoc.id || tDoc.codigo) : tDoc;

                            // Si sigue siendo objeto o nulo, intentar con el ID directo si existe
                            if (typeof tDocId === 'object') {
                                tDocId = tDocId.id || tDocId.tipoDoc;
                            }

                            this.tipoDocumento.set(tDocId);

                            this.cargarBloquesYFinalizar(p);
                        },
                        error: () => this.finalizarSinDatos()
                    });
                } else {
                    // Es nuevo, el módulo ya debería venir de queryParams
                    this.cargarBloquesYFinalizar();
                }
            },
            error: () => this.finalizarSinDatos()
        });
    }

    private cargarBloquesYFinalizar(sourceData?: any): void {
        const mod = this.modulo();
        console.log('mod', mod);
        if (!mod) {
            this.iniciarEditor([], []);
            this.loading.set(false);
            return;
        }

        this.documentoService.listarBloques(mod).subscribe({
            next: (res: any) => {
                const bloques = res.data || [];
                const dispositivos = this.listadoFormatos().length > 0
                    ? this.listadoFormatos().map(f => ({ id: f.id.toString(), name: f.nombre, width: '', height: '' }))
                    : [{ name: 'Escritorio', width: '' }];

                this.iniciarEditor(dispositivos, bloques);

                if (sourceData && this.editor) {
                    console.log('Cargando contenido previo en editor...');
                    this.editor.setComponents(sourceData.htmlContenido);
                    this.editor.setStyle(sourceData.cssEstilo);
                    if (this.formatoId()) {
                        console.log('Estableciendo dispositivo:', this.formatoId());
                        this.editor.setDevice(this.formatoId()!.toString());
                    }
                }

                // Forzar ajuste del área de trabajo SIEMPRE después de cargar bloques e iniciar editor
                console.log('Programando actualización de área de trabajo...');
                setTimeout(() => {
                    console.log('Ejecutando actualizarAreaTrabajo diferido');
                    this.actualizarAreaTrabajo();
                }, 300);

                this.loading.set(false);
            },
            error: () => {
                this.iniciarEditor([], []);
                this.loading.set(false);
            }
        });
    }

    private finalizarSinDatos(): void {
        this.alertService.toast('Error cargando datos iniciales', 'error');
        this.iniciarEditor([], []);
        this.loading.set(false);
    }

    private iniciarEditor(dispositivos: any[], bloques: any[]): void {


        this.editor = grapesjs.init({
            ...GJS_CONFIG_BASE,
            container: '#gjs',
            deviceManager: { devices: dispositivos },
            pluginsOpts: {
                [gjsImageEditor as any]: { config: { includeUI: { initMenu: 'filter' } } },
                [myTailwindPlugin as any]: { loadBlocks: true }
            },
            plugins: [
                gjsBasicBlocks, gjsForms, gjsImageEditor,
                gjsGradient, gjsFilters, gjsTouch, myTailwindPlugin
            ],
        });

        // Hacer que las imágenes y bloques sean redimensionables por defecto (estilo Word)
        // 1. Configurar componentes al ser añadidos
        this.editor.on('component:add', (model: any) => this.configurarComportamientoComponente(model));

        // 2. Configurar componentes cuando cambian de padre (drag & drop interno)
        this.editor.on('component:update:parent', (model: any) => this.configurarComportamientoComponente(model));

        registerTableCommands(this.editor);
        this.canvasManager = new CanvasManager(this.editor);

        // 3. Forzar cursor y estilos interactivos durante el arrastre sobre celdas o agrupadores
        this.editor.on('canvas:dragover', (ev: any) => {
            const doc = this.editor.Canvas.getDocument();
            if (doc) {
                const prevHovered = doc.querySelectorAll('.group-block-hover');
                prevHovered.forEach((el: any) => el.classList.remove('group-block-hover'));
            }

            const target = ev.target;
            if (!target || !target.classList) return;

            const tagName = (target.tagName || '').toLowerCase();
            const isCell = tagName === 'td' || tagName === 'th' ||
                target.classList.contains('cell-pro') ||
                target.classList.contains('cell-content');

            const isGroup = target.classList.contains('group-block');

            if (isGroup) {
                target.classList.add('group-block-hover');
                this.editor.Canvas.getBody().style.cursor = 'copy';
            } else if (isCell) {
                this.editor.Canvas.getBody().style.cursor = 'copy';
            } else {
                this.editor.Canvas.getBody().style.cursor = 'default';
            }
        });

        // Limpiar el estado de "hover" visual al soltar o cancelar el arrastre
        const clearHoverState = () => {
            const doc = this.editor.Canvas.getDocument();
            if (doc) {
                doc.querySelectorAll('.group-block-hover').forEach((el: any) => el.classList.remove('group-block-hover'));
            }
            if (this.editor.Canvas.getBody()) {
                this.editor.Canvas.getBody().style.cursor = 'default';
            }
        };

        // Forzar anidación matemática al soltar el modelo
        const forceDropNesting = (modelToDrop: any) => {
            if (!modelToDrop) return;
            const doc = this.editor.Canvas.getDocument();
            if (!doc) return;

            const hoveredEls = doc.querySelectorAll('.group-block-hover');
            if (hoveredEls && hoveredEls.length > 0) {
                const targetEl = hoveredEls[0];
                const targetId = targetEl.id;

                if (targetId) {
                    const wrappers = this.editor.DomComponents.getWrapper().find(`#${targetId}`);
                    if (wrappers && wrappers.length > 0) {
                        const targetModel = wrappers[0];

                        // Evitar bucles espaciales: si está soltando sobre sí mismo o si ya está anidado correctamente
                        if (modelToDrop === targetModel || modelToDrop.parent() === targetModel) return;

                        const modelEl = modelToDrop.getEl();
                        if (!modelEl) return;

                        // Calcular posiciones exactas en el canvas
                        const targetRect = targetEl.getBoundingClientRect();
                        const modelRect = modelEl.getBoundingClientRect();

                        // Traspasar el modelo de su padre actual al contenedor
                        targetModel.append(modelToDrop);

                        // Ajustar la posición para que quede exactamente donde el mouse lo soltó (si es libre)
                        // Si es un contenedor de flujo (stack, text, row), se quita la posición absoluta
                        const isFlowContainer = targetEl.classList.contains('stack-block') ||
                            targetEl.classList.contains('text-block') ||
                            targetEl.classList.contains('flex-row');

                        if (isFlowContainer) {
                            modelToDrop.addStyle({
                                position: 'relative',
                                top: 'auto',
                                left: 'auto',
                                margin: '0'
                            });
                        } else {
                            modelToDrop.addStyle({
                                top: `${Math.max(0, modelRect.top - targetRect.top)}px`,
                                left: `${Math.max(0, modelRect.left - targetRect.left)}px`,
                                position: 'absolute'
                            });
                        }

                        modelToDrop.set('draggable', true);
                    }
                }
            }
        };

        // Función inteligente para Límites y Auto-ajuste de contenedores
        const restrictToBoundsAndFit = (model: any) => {
            if (!model) return;
            const el = model.getEl();
            const parent = model.parent();
            if (!el || !parent) return;

            const parentEl = parent.getEl();
            if (!parentEl) return;

            const parentTagName = (parent.get('tagName') || '').toLowerCase();
            const isParentRoot = parentTagName === 'body' || parentTagName === 'wrapper';
            const isParentGroup = parent.get('classes')?.models.some((c: any) => c.get('name') === 'group-block');

            const style = model.getStyle();

            // Si el elemento pertenece al flujo natural (ej: dentro de un stack-block), no alterar sus coordenadas matemáticas absolutas.
            if (style.position === 'relative' || style.position === 'static') return;

            let top = parseFloat(style.top || '0');
            let left = parseFloat(style.left || '0');

            let width = el.offsetWidth;
            let height = el.offsetHeight;
            const pWidth = parentEl.clientWidth;
            const pHeight = parentEl.clientHeight;

            let changedChildPos = false;

            // 1. Bloquear SIEMPRE el arrastre a coordenadas negativas (Salirse por izquierda o arriba)
            if (left < 0) { left = 0; changedChildPos = true; }
            if (top < 0) { top = 0; changedChildPos = true; }

            // 2. Lógica dependiendo de dónde esté el modelo
            if (isParentRoot) {
                // Está suelto en la hoja blanca. LÍMITE RÍGIDO. No permitir desbordamiento derecho/abajo.
                if (width > pWidth && pWidth > 0) { width = pWidth; changedChildPos = true; }
                if (height > pHeight && pHeight > 0) { height = pHeight; changedChildPos = true; }

                if (left + width > pWidth && pWidth > 0) {
                    left = Math.max(0, pWidth - width); changedChildPos = true;
                }
                if (top + height > pHeight && pHeight > 0) {
                    top = Math.max(0, pHeight - height); changedChildPos = true;
                }
            } else if (isParentGroup) {
                // Está dentro de un 'Bloque Div'. Si sobresale hacia la derecha o abajo, EL PADRE CRECE.
                let parentNeedsUpdate = false;
                let newParentWidth = parseFloat(parent.getStyle().width || `${pWidth}`);
                let newParentHeight = parseFloat(parent.getStyle().height || `${pHeight}`);

                if (left + width > newParentWidth) {
                    newParentWidth = left + width;
                    parentNeedsUpdate = true;
                }
                if (top + height > newParentHeight) {
                    newParentHeight = top + height;
                    parentNeedsUpdate = true;
                }

                if (parentNeedsUpdate) {
                    parent.addStyle({
                        width: `${newParentWidth}px`,
                        height: `${newParentHeight}px`
                    });
                    // Forzar verificación de límites del padre para evitar que al crecer rompa la hoja blanca
                    setTimeout(() => restrictToBoundsAndFit(parent), 20);
                }
            }

            // 3. Aplicar las correcciones matemáticas al hijo
            if (changedChildPos) {
                setTimeout(() => {
                    model.addStyle({
                        left: `${left}px`,
                        top: `${top}px`,
                        width: `${width}px`,
                        height: `${height}px`
                    });
                }, 10);
            }
        };

        const container = this.editor.getContainer();
        if (container) {
            container.addEventListener('mouseup', () => {
                setTimeout(() => {
                    const model = this.editor.getSelected();
                    if (model) restrictToBoundsAndFit(model);
                }, 50);
            });
        }

        this.editor.on('canvas:drop', (dataTransfer: any, model: any) => {
            setTimeout(() => {
                const actualModel = model || this.editor.getSelected();
                forceDropNesting(actualModel);
                setTimeout(() => restrictToBoundsAndFit(actualModel), 10);
                clearHoverState();
            }, 50);
        });

        this.editor.on('component:drag:end', (model: any) => {
            setTimeout(() => {
                forceDropNesting(model);
                setTimeout(() => restrictToBoundsAndFit(model), 10);
                clearHoverState();
            }, 50);
        });


        // Cargar Bloques
        bloques.forEach((b: any) => {
            let htmlProcessado = b.htmlContenido;

            // Inyectar data-gjs-draggable si es variable sin envolverlo en un div adicional
            if (b.categoria && (b.categoria.toLowerCase().includes('variable') || b.categoria.toLowerCase().includes('variables'))) {
                // Si ya tiene una etiqueta principal, intentamos inyectar el atributo, sino lo dejamos como está
                if (htmlProcessado.startsWith('<') && htmlProcessado.includes('>')) {
                    htmlProcessado = htmlProcessado.replace('>', ' data-gjs-draggable="*">');
                }
            }

            this.editor.BlockManager.add(b.nombre, {
                label: `<b>${b.nombre}</b><br><small>${b.categoria}</small>`,
                content: `<style>${b.cssEstilo || ''}</style>${htmlProcessado}`,
                category: b.categoria
            });
        });

        this.editor.on('load', () => {
            // Inyectar estilos globales del canvas correctamente sin lanzar peticiones GET accidentales
            const styleString = CANVAS_STYLES.join('\n');
            const styleDocument = this.editor.Canvas.getDocument();
            if (styleDocument && styleDocument.head) {
                styleDocument.head.insertAdjacentHTML('beforeend', `<style>${styleString}</style>`);
            }

            this.configurarCategorias();
            this.actualizarAreaTrabajo();
            // Reforzar configuración en todos los componentes existentes
            if (this.editor) {
                this.editor.getComponents().forEach((model: any) => this.configurarComportamientoComponente(model));
            }
        });
    }

    /**
     * Configura dinámicamente si un componente es arrastrable, redimensionable o receptor de contenido
     * basado en su tipo y ubicación.
     */
    private configurarComportamientoComponente(model: any): void {
        if (!model) return;

        const tagName = (model.get('tagName') || '').toLowerCase();
        const type = model.get('type');
        const classes = model.get('classes') || { models: [] };
        const isGroupBlock = classes.models.some((c: any) => c.get('name') === 'group-block');
        const isCellContent = classes.models.some((c: any) => c.get('name') === 'cell-content');
        const isVariable = classes.models.some((c: any) => c.get('name') === 'variable-token');

        const parent = model.parent();
        const parentTagName = parent ? (parent.get('tagName') || '').toLowerCase() : '';
        const parentClasses = parent ? (parent.get('classes') || { models: [] }) : { models: [] };
        const isInsideCell = parentTagName === 'td' || parentTagName === 'th' ||
            parentClasses.models.some((c: any) => c.get('name') === 'cell-content');

        const tableTags = ['td', 'th', 'tr', 'thead', 'tbody', 'table'];

        // A. Configuración para Elementos de Estructura (Tablas, Imágenes, Celdas, Boxes, Agrupadores)
        if (type === 'image' || tagName === 'img' || tableTags.includes(tagName) || isCellContent || isGroupBlock) {
            model.set({
                resizable: {
                    minDim: 50, // Previene que los selectores se desincronicen cuando alcanza el tamaño mínimo
                    handlers: ['n', 'e', 's', 'w', 'ne', 'se', 'sw', 'nw'],
                },
                draggable: (tagName === 'table' || type === 'image' || tagName === 'img' || isGroupBlock) ? true : false,
                droppable: true,
                removable: true,
                copyable: true,
            });

            // Si es una fila, añadir opción de Fila Iterativa
            if (tagName === 'tr') {
                const traits = model.get('traits');
                // Evitar duplicados
                if (!traits.where({ name: 'data-iterativa' }).length) {
                    traits.add({
                        type: 'checkbox',
                        name: 'data-iterativa',
                        label: 'Fila Iterativa',
                        valueTrue: 'true',
                        valueFalse: '',
                    });
                }
            }
        }

        // B. Bloquear el arrastre de DENTRO de las celdas para que no se "escape"
        if (isInsideCell) {
            model.set('draggable', false);
        } else if (isVariable) {
            // Permitir que las variables se suelten en cualquier parte (especialmente tablas)
            model.set('draggable', '*');
        }

        // Recursividad para hijos
        model.components().forEach((child: any) => this.configurarComportamientoComponente(child));
    }

    private configurarCategorias(): void {
        const categories = this.editor.BlockManager.getCategories();
        if (!categories || !categories.each) return;

        let firstVar = true;
        categories.each((cat: any) => {
            cat.set('open', false);
            const label = cat.get('label') || '';
            if (label.includes('Variables > ')) {
                cat.set('label', label.replace('Variables > ', ''));
                cat.set('attributes', { 'data-is-variable': 'true', 'data-first-variable': firstVar ? 'true' : 'false' });
                firstVar = false;
            }
        });
    }

    actualizarAreaTrabajo(): void {
        if (!this.canvasManager || !this.editor) {
            console.warn('Falta canvasManager o editor para actualizar');
            return;
        }

        setTimeout(() => {
            const fId = this.formatoId();
            const formatos = this.listadoFormatos();
            const formato = formatos.find(f => f.id === fId);

            console.log('Actualizando lienzo:', { fId, totalFormatos: formatos.length, encontrado: !!formato });

            // Valores por defecto (A4 aprox en px)
            let ancho = 794;
            let alto = 1123;

            if (formato) {
                ancho = formato.anchoPx || 794;
                alto = formato.altoPx || 1123;
            }

            this.canvasManager.actualizarAreaTrabajo(ancho, alto, this.orientacion());
            if (fId) this.editor.setDevice(fId.toString());
        }, 50);
    }

    cambiarOrientacion(): void {
        this.orientacion.set(this.orientacion() === 'Vertical' ? 'Horizontal' : 'Vertical');
    }

    setFormato(id: number): void {
        this.formatoId.set(id);
    }

    guardar(): void {
        if (!this.nombre().trim()) {
            this.alertService.toast('Ingrese un nombre para el documento', 'warning');
            return;
        }

        this.loading.set(true);
        const htmlRaw = this.editor.getHtml();
        const css = this.editor.getCss();
        const htmlTraducido = this.traductorService.traducir(htmlRaw);

        const tp = this.tipoDocumento();
        const tipoDocCode = (typeof tp === 'object' && tp !== null) ? (tp as any).tipoDoc || (tp as any).id : tp;

        const plantilla: Plantilla = {
            id: this.plantillaId() || undefined,
            nombre: this.nombre(),
            htmlContenido: htmlRaw,
            htmlTraducido: htmlTraducido,
            cssEstilo: css,
            formatoId: this.formatoId() || undefined,
            orientacion: this.orientacion(),
            modulo: this.modulo()!,
            tipoDocumento: tipoDocCode,
            isDefault: this.isDefault()
        };

        const request$ = this.plantillaId()
            ? this.documentoService.actualizarPlantilla(this.plantillaId()!, plantilla)
            : this.documentoService.guardarPlantilla(plantilla);

        request$.subscribe({
            next: () => {
                this.alertService.toast('Guardado correctamente', 'success');
                this.loading.set(false);
                this.router.navigate(['/documentos']);
            },
            error: () => {
                this.alertService.toast('Error al guardar', 'error');
                this.loading.set(false);
            }
        });
    }

    ngOnDestroy(): void {
        if (this.editor) this.editor.destroy();
    }

    // Getters para UI
    get tipoDocumentoLabel(): string {
        const val = this.tipoDocumento();
        if (!val) return 'PERSONALIZADO';

        // Extraer el ID o código Real si es un objeto (evita [OBJECT OBJECT])
        const tipoId = (typeof val === 'object' && val !== null) ? (val.tipoDoc || val.id || val.codigo) : val;

        const tipos = this.listadoTipos();
        // Buscar el nombre real en el listado de tipos cargado
        const tipoReal = tipos.find((t: any) => t.tipoDoc === tipoId || t.id === tipoId || t.codigo === tipoId);

        if (tipoReal) return tipoReal.nombre.toUpperCase();

        // Fallback si no se encontró en la lista: limpiar el ID crudo
        const fallback = (tipoId?.toString() || '').replace(/_/g, ' ').toUpperCase();
        return fallback.includes('OBJECT OBJECT') ? 'DOCUMENTO' : fallback;
    }

    get nombreFormatoActual(): string {
        const formato = this.listadoFormatos().find(f => f.id === this.formatoId());
        return formato ? formato.nombre : 'Personalizado';
    }
}