import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, firstValueFrom } from 'rxjs';
import { Plantilla } from '../models/documento.model';
import { ClinicaService } from '../../configuraciones/services/clinica.service';
import { DocumentoService } from './documento.service';
import { AuthService } from '../../auth/services/auth.service';
import { SucursalService } from '../../../core/services/sucursal.service';

@Injectable({
    providedIn: 'root'
})
export class DocumentoImpresionService {
    private clinicaService = inject(ClinicaService);
    private documentoService = inject(DocumentoService);
    private authService = inject(AuthService);
    private sucursalService = inject(SucursalService);

    /**
     * Procesa una plantilla con datos de venta y clínica, e imprime via iframe.
     */
    async imprimirVenta(idPlantilla: number, datosVenta: any): Promise<void> {
        // ... (se mantiene igual para ventas)
        try {
            const idSucursal = this.authService.getSucursalIdFromToken();
            const [resPlantilla, resClinica, resSucursal] = await Promise.all([
                firstValueFrom(this.documentoService.obtenerPlantilla(idPlantilla)),
                firstValueFrom(this.clinicaService.obtenerPrincipal()),
                idSucursal ? firstValueFrom(this.sucursalService.getById(idSucursal)) : Promise.resolve({ data: null, success: true })
            ]);

            const plantilla = resPlantilla.data;
            const datosClinica = resClinica.data;
            const datosSucursal = resSucursal?.data;

            if (!plantilla) throw new Error('No se encontró la plantilla de impresión.');

            const contexto = this.prepararContexto(datosVenta, datosClinica, datosSucursal);
            let htmlFinal = (plantilla.htmlTraducido && plantilla.htmlTraducido.length > 0) ? plantilla.htmlTraducido : plantilla.htmlContenido;
            if (htmlFinal.includes('{{tabla_items}}')) {
                const htmlTabla = this.generarHtmlTablaItems(datosVenta);
                htmlFinal = htmlFinal.replace('{{tabla_items}}', htmlTabla);
            }
            htmlFinal = this.procesarEtiquetas(htmlFinal, contexto);
            const fullHtml = this.construirDocumentoCompleto(htmlFinal, plantilla);
            this.ejecutarImpresionIframe(fullHtml);
        } catch (error) {
            console.error('❌ Error en el proceso de impresión de venta:', error);
            throw error;
        }
    }

    /**
     * Procesa múltiples documentos e imprime todo en un solo popup
     */
    async imprimirMasa(documentos: { idPlantilla: number, datosVenta: any }[]): Promise<void> {
        try {
            const idSucursal = this.authService.getSucursalIdFromToken();
            const [resClinica, resSucursal] = await Promise.all([
                firstValueFrom(this.clinicaService.obtenerPrincipal()),
                idSucursal ? firstValueFrom(this.sucursalService.getById(idSucursal)) : Promise.resolve({ data: null, success: true })
            ]);

            const datosClinica = resClinica.data;
            const datosSucursal = resSucursal?.data;

            let allHtml = '';

            for (let i = 0; i < documentos.length; i++) {
                const doc = documentos[i];
                const resPlantilla = await firstValueFrom(this.documentoService.obtenerPlantilla(doc.idPlantilla));
                const plantilla = resPlantilla.data;

                if (!plantilla) continue;

                const contexto = this.prepararContexto(doc.datosVenta, datosClinica, datosSucursal);
                let htmlFinal = (plantilla.htmlTraducido && plantilla.htmlTraducido.length > 0) ? plantilla.htmlTraducido : plantilla.htmlContenido;
                if (htmlFinal.includes('{{tabla_items}}')) {
                    const htmlTabla = this.generarHtmlTablaItems(doc.datosVenta);
                    htmlFinal = htmlFinal.replace('{{tabla_items}}', htmlTabla);
                }
                htmlFinal = this.procesarEtiquetas(htmlFinal, contexto);

                allHtml += htmlFinal;
                // Add page break between documents
                if (i < documentos.length - 1) {
                    allHtml += '<div style="page-break-after: always;"></div>';
                }
            }

            if (allHtml.trim() !== '') {
                // Obtenemos la primera plantilla para extraer los estilos generales y el formato (ancho)
                const primeraPlantilla = await firstValueFrom(this.documentoService.obtenerPlantilla(documentos[0].idPlantilla));
                const fullHtml = this.construirDocumentoCompleto(allHtml, primeraPlantilla.data);
                this.ejecutarImpresionIframe(fullHtml);
            }
        } catch (error) {
            console.error('❌ Error en el proceso de impresión masiva:', error);
            throw error;
        }
    }

    /**
     * MÉTODO NUEVO: Procesa la impresión específica para Movimientos Diversos.
     */
    async imprimirMovimiento(idPlantilla: number, mov: any): Promise<void> {
        try {
            console.log('🔍 Iniciando impresión de MOVIMIENTO. ID Plantilla:', idPlantilla);

            const idSucursal = this.authService.getSucursalIdFromToken();
            const [resPlantilla, resClinica, resSucursal] = await Promise.all([
                firstValueFrom(this.documentoService.obtenerPlantilla(idPlantilla)),
                firstValueFrom(this.clinicaService.obtenerPrincipal()),
                idSucursal ? firstValueFrom(this.sucursalService.getById(idSucursal)) : Promise.resolve({ data: null, success: true })
            ]);

            const plantilla = resPlantilla.data;
            const datosClinica = resClinica.data;
            const datosSucursal = resSucursal?.data;

            if (!plantilla) throw new Error('No se encontró la plantilla de impresión.');

            // 1. Usar contexto específico de movimiento
            const contexto = this.prepararContextoMovimiento(mov, datosClinica, datosSucursal);
            console.log('📦 Contexto MOVIMIENTO:', contexto);

            let htmlFinal = (plantilla.htmlTraducido && plantilla.htmlTraducido.length > 0)
                ? plantilla.htmlTraducido
                : plantilla.htmlContenido;

            // 2. Procesar tabla de detalles de movimiento
            if (htmlFinal.includes('{{tabla_items}}')) {
                const htmlTabla = this.generarHtmlTablaItemsMovimiento(mov.detalles || []);
                htmlFinal = htmlFinal.replace('{{tabla_items}}', htmlTabla);
            }

            // 3. Reemplazar etiquetas (usa el mismo motor pero con nuevo contexto)
            htmlFinal = this.procesarEtiquetas(htmlFinal, contexto);

            // 4. Construir y ejecutar
            const fullHtml = this.construirDocumentoCompleto(htmlFinal, plantilla);
            this.ejecutarImpresionIframe(fullHtml);

        } catch (error) {
            console.error('❌ Error en el proceso de impresión de movimiento:', error);
            throw error;
        }
    }

    private prepararContextoMovimiento(mov: any, cli: any, suc: any = null): any {
        return {
            clinica: {
                logoBase64: cli?.logoBase64 || '',
                razonSocial: cli?.razonSocial || '--',
                ruc: cli?.ruc || '--',
                direccion: suc?.direccion || cli?.direccion || '--',
                telefono: suc?.telefono || cli?.telefono || '--',
                email: cli?.email || '--',
                web: cli?.web || '--'
            },
            documento: {
                id: mov.id || '--',
                numdoc: mov.numDocumento || '--',
                fecha: mov.fecha ? new Date(mov.fecha).toLocaleString() : new Date().toLocaleString(),
                motivo: mov.motivo || '--',
                estado: mov.estado || 'ACTIVO',
                tipoDoc: 'MOVIMIENTO DE INVENTARIO'
            },
            movimiento: {
                numDocumento: mov.numDocumento || '--',
                motivo: mov.motivo || '--',
                fecha: mov.fecha || new Date()
            },
            usuario: {
                username: mov.usuario?.username || '--',
                nombre: mov.usuario?.nombreCompleto || mov.usuario?.nombre || '--'
            },
            sucursal: {
                nombre: mov.sucursal?.nombre || '--',
                serie: mov.sucursal?.serie || '--'
            },
            // Mapeo detallado para que las etiquetas {{detalle.xxx}} funcionen
            detalles: (mov.detalles || []).map((det: any) => ({
                ...det,
                nombreProducto: det.catalogo?.nombre || '--',
                tipo: det.tipo || '--',
                cantidad: det.cantidad || 0,
                lote: det.nroLote || '--',
                vencimiento: det.fechaVencimiento || '--',
                observacion: det.observacion || ''
            }))
        };
    }

    private generarHtmlTablaItemsMovimiento(detalles: any[]): string {
        if (!detalles || detalles.length === 0) return '';
        return detalles.map((det: any) => `
            <tr class="border-b border-gray-100">
                <td class="py-1 px-2 border text-center font-bold">${det.cantidad}</td>
                <td class="py-1 px-2 border text-left">
                    <div class="font-bold">${det.catalogo?.nombre || 'Producto'}</div>
                    <div class="text-[10px] text-gray-500">Lote: ${det.nroLote || '-'} | Venc: ${det.fechaVencimiento || '-'}</div>
                </td>
                <td class="py-1 px-2 border text-left text-[10px]">${det.tipo || ''}</td>
                <td class="py-1 px-2 border text-left text-[10px] italic">${det.observacion || ''}</td>
            </tr>
        `).join('');
    }

    private prepararContexto(vta: any, cli: any, suc: any = null): any {
        return {
            clinica: {
                logoBase64: cli?.logoBase64 || '',
                razonSocial: cli?.razonSocial || '--',
                ruc: cli?.ruc || '--',
                direccion: suc?.direccion || cli?.direccion || '--',
                abrev: cli?.abrev || '',
                codigo: cli?.codigo || '',
                telefono: suc?.telefono || cli?.telefono || '--',
                email: cli?.email || '',
                fax: cli?.fax || '',
                web: cli?.web || '--',
                representante: cli?.representante || '',
                auditor: cli?.auditor || '',
                liquidador: cli?.liquidador || '',
                financiero: cli?.financiero || '',
                ctacte: cli?.ctacte || '',
                estado: cli?.estado || '',
                idClinica: cli?.idClinica || '',
                fechaCreacion: cli?.fechaCreacion || '',
                fechaActualizacion: cli?.fechaActualizacion || ''
            },
            documento: {
                idVenta: vta.idVenta || '--',
                serie: vta.serie || '--',
                numdoc: (vta.numero ?? vta.numdoc)?.toString().padStart(7, '0') || '--',
                fecha: vta.fecha || new Date().toLocaleDateString(),
                tipoDoc: this.getTipoDocDesc(vta.tipoDoc) || vta.tipoDoc || '--',
                moneda: vta.moneda || 'PEN',
                tc: vta.tc || '1.00',
                voucher: vta.voucher || '--',
                serieTicketera: vta.serieTicketera || vta.serie || '--',
                concepto: vta.concepto || vta.observacion || vta.obs || '--',
                obs: vta.obs || vta.observacion || '--',

                // Financiero
                valorAfecto: vta.baseImp || vta.impBase || 0,
                valorInaf: vta.valorInaf || 0,
                valorExo: vta.valorExo || 0,
                baseImp: vta.baseImp || 0,
                igv: vta.igv || 0,
                ivap: vta.ivap || 0,
                descuento: vta.descuento || 0,
                descuentoEsp: vta.descuentoEsp || 0,
                total: vta.total || 0,
                copago: vta.copago || 0,
                metodoPago: vta.metodoPago?.descripcion || vta.metodoPago || '--',
                importePago: vta.importePago || 0,
                vuelto: vta.vuelto || 0,
                totalLetras: vta.totalLetras || '--',

                // Datos Cliente incrustados en Venta
                nombrePac: vta.nombrePaciente || vta.nombrePac || vta.paciente?.nombreCompleto || '--',
                ruc: vta.ruc || '--',
                razon: vta.razon || '--',
                direcRuc: vta.direcRuc || vta.direccRuc || '--',
                direccion: vta.direccion || vta.direccResi || '--',
                tipoDni: vta.tipoDni || '--',
                nroDni: vta.nroDni || '--',

                // Auditoría
                idUser: vta.idUser || '--',
                createdAt: vta.createdAt || '',
                updatedAt: vta.updatedAt || '',
                ip: vta.ip || ''
            },
            cliente: {
                nombre: vta.nombrePaciente || vta.nombrePac || vta.razon || vta.paciente?.nombreCompleto || '--',
                documento: vta.ruc || vta.nroDni || vta.paciente?.numdoc || '--',
                direccion: vta.direcRuc || vta.direccion || '--',
                telefono: vta.telefono || vta.paciente?.telefono || '--',
                email: vta.email || vta.paciente?.email || '--'
            },
            usuario: {
                username: vta.idUser || '--',
                nombre: vta.nombreVendedor || vta.usuario?.nombreCompleto || '--',
                email: vta.emailVendedor || vta.usuario?.email || '--',
                rol: vta.rolVendedor || vta.usuario?.rol || '--'
            },
            punto: {
                nombre: vta.punto || vta.puntoEscogido || vta.punto?.nombre || '--',
                serie: vta.serie || vta.seriePunto || vta.punto?.serie || '--'
            },
            detalles: vta.detalles || []
        };
    }

    private getTipoDocDesc(codigo: string): string {
        switch (codigo) {
            case '01': return 'FACTURA';
            case '03': return 'BOLETA';
            case '07': return 'NOTA DE CRÉDITO';
            case '08': return 'NOTA DE DÉBITO';
            case 'NV': return 'NOTA DE VENTA';
            default: return codigo;
        }
    }

    private procesarEtiquetas(html: string, contexto: any): string {
        if (!html) return '';

        // 1. Reemplazo general de variables simples (omite las de los ítems de tablas iterativas)
        let htmlProcesado = html.replace(/\{\{\s*([\s\S]+?)\s*\}\}/g, (match, path) => {
            const cleanPath = path.trim();
            // Si empieza con 'item.' o 'detalle.' lo ignoramos, las tablas lo procesarán
            if (cleanPath.startsWith('detalle.') || cleanPath.startsWith('item.')) {
                return match;
            }
            const valor = this.obtenerValorProfundo(contexto, cleanPath);
            return (valor !== undefined && valor !== null && String(valor).trim() !== '') ? String(valor) : '--';
        });

        // 2. Procesamiento de Tablas Iterativas
        htmlProcesado = this.procesarTablasIterativas(htmlProcesado, contexto);

        return htmlProcesado;
    }

    private procesarTablasIterativas(html: string, contexto: any): string {
        if (!contexto?.detalles || !Array.isArray(contexto.detalles) || contexto.detalles.length === 0) {
            return html;
        }

        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');

        // 1. Buscar filas explícitamente marcadas como iterativas
        const filasIterables = Array.from(doc.querySelectorAll('tr[data-iterativa="true"], tr.js-fila-iterativa'));

        // 2. [SMART UPDATE] Buscar filas que contengan tags de detalles pero no estén marcadas
        const todasLasFilas = Array.from(doc.querySelectorAll('tr'));
        todasLasFilas.forEach(tr => {
            if (tr.innerHTML.includes('{{detalle.') || tr.innerHTML.includes('{{item.')) {
                if (!filasIterables.includes(tr)) {
                    filasIterables.push(tr);
                }
            }
        });

        // 3. Buscar tablas enteras marcadas
        const tablasIterativas = Array.from(doc.querySelectorAll('table[data-iterativa="true"], .tabla-iterativa'));

        // Recopilar todos los tbodys que necesitan procesamiento
        const tbodysAProcesar = new Set<Element>();

        // De las filas encontradas, tomamos sus tbodys
        filasIterables.forEach(tr => {
            if (tr.parentElement && tr.parentElement.tagName === 'TBODY') {
                tbodysAProcesar.add(tr.parentElement);
            }
        });

        // De las tablas encontradas, tomamos sus tbodys
        tablasIterativas.forEach(tabla => {
            const tbody = tabla.querySelector('tbody');
            if (tbody) tbodysAProcesar.add(tbody);
        });

        // Procesar cada contenedor de filas dinámicas
        tbodysAProcesar.forEach(tbody => {
            // Buscamos la fila que sirva de plantilla (prioridad a la que tiene el atributo)
            const templateRow = tbody.querySelector('tr[data-iterativa="true"]') ||
                tbody.querySelector('tr.js-fila-iterativa') ||
                tbody.querySelector('tr');

            if (!templateRow) return;

            const templateHtml = templateRow.outerHTML;
            tbody.innerHTML = '';

            contexto.detalles.forEach((det: any) => {
                let rowHtml = templateHtml;

                // Reemplazo robusto (multilínea, con/sin espacios) para {{detalle.X}} o {{item.X}}
                rowHtml = rowHtml.replace(/\{\{\s*(?:detalle|item)\.([\s\S]+?)\s*\}\}/g, (match, path) => {
                    const cleanPath = path.trim();
                    const valor = this.obtenerValorProfundo(det, cleanPath);
                    return (valor !== undefined && valor !== null && String(valor).trim() !== '') ? String(valor) : '--';
                });

                const tempTable = document.createElement('table');
                const tempTbody = document.createElement('tbody');
                tempTable.appendChild(tempTbody);
                tempTbody.innerHTML = rowHtml;
                const newRow = tempTbody.firstElementChild as HTMLElement;

                if (newRow) {
                    // [IMPORTANT] Limpiar IDs y estilos que causan superposición
                    newRow.removeAttribute('id');
                    newRow.removeAttribute('data-iterativa');
                    newRow.removeAttribute('*ngFor');
                    newRow.classList.remove('js-fila-iterativa');

                    // Resetear estilos de posicionamiento que GrapesJS añade en modo absoluto
                    newRow.style.position = 'static';
                    newRow.style.top = 'auto';
                    newRow.style.left = 'auto';
                    newRow.style.display = 'table-row';

                    // Limpiar IDs y estilos de los hijos (celdas y spans) para que sigan el flujo del TR
                    newRow.querySelectorAll('[id]').forEach(el => {
                        el.removeAttribute('id');
                        if (el instanceof HTMLElement) {
                            el.style.position = 'static';
                            el.style.top = 'auto';
                            el.style.left = 'auto';
                        }
                    });

                    tbody.appendChild(newRow);
                }
            });
        });

        // Retornar solo el contenido necesario (evita añadir html/body si no estaban)
        return doc.body.innerHTML;
    }

    private obtenerValorProfundo(obj: any, path: string): any {
        return path.split('.').reduce((prev, curr) => {
            return prev ? prev[curr] : undefined;
        }, obj);
    }

    private generarHtmlTablaItems(datosVenta: any): string {
        const detalles = datosVenta.detalles || [];
        if (detalles.length === 0) return '';

        return detalles.map((det: any) => {
            const lote = det.nroLote || det.lote || '-';
            const vence = det.fechaVenc || det.vence || '';

            return `
                <tr class="border-b border-gray-100">
                    <td class="py-2 px-2 border text-center">${det.cantidad}</td>
                    <td class="py-2 px-2 border text-left font-medium text-xs">${det.glosa}</td>
                    <td class="py-2 px-2 border text-center text-xs text-gray-600">${lote} ${vence}</td>
                    <td class="py-2 px-2 border text-right">${det.precioUnitario}</td>
                    <td class="py-2 px-2 border text-right font-semibold">${det.total}</td>
                </tr>
            `;
        }).join('');
    }

    private construirDocumentoCompleto(htmlContenido: string, plantilla: any): string {
        const formatoNombre = (plantilla.formato?.nombre || 'A4').toUpperCase();
        const anchoPx = plantilla.formato?.anchoPx || (formatoNombre.includes('TICKET') ? 302 : 794);
        const containerWidth = `${anchoPx}px`;

        return `
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    * { box-sizing: border-box; }
                    
                    body { 
                        font-family: 'Arial', sans-serif;
                        margin: 0; 
                        padding: 0; 
                        background: white;
                        display: flex;
                        justify-content: center;
                    }
                    
                    .print-container {
                        background: white;
                        width: ${containerWidth};
                        position: relative;
                        margin: 0 auto;
                        display: block;
                    }

                    ${plantilla.cssEstilo || ''}

                    /* 
                       Aseguramos que los elementos internos fluyan bien 
                       pero sin forzar un diseño que el usuario no quiere.
                    */
                    .print-container table {
                        width: 100% !important;
                        border-collapse: collapse;
                        position: relative !important;
                        margin-top: 10px;
                    }

                    @page { 
                        size: ${containerWidth} auto; 
                        margin: 0; 
                    }

                    @media print {
                        body { background: white; display: block; margin: 0; padding: 0; }
                        .print-container { 
                            border: none !important; 
                            margin: 0 auto; 
                            width: ${containerWidth};
                            box-shadow: none;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="print-container">
                    ${htmlContenido}
                </div>
            </body>
            </html>
        `;
    }

    private ejecutarImpresionIframe(html: string): void {
        const iframe = document.createElement('iframe');
        iframe.style.position = 'fixed';
        iframe.style.right = '0';
        iframe.style.bottom = '0';
        iframe.style.width = '0';
        iframe.style.height = '0';
        iframe.style.border = '0';
        document.body.appendChild(iframe);

        const doc = iframe.contentWindow?.document;
        if (doc) {
            doc.open();
            doc.write(html);
            doc.close();

            // Esperar a que cargue el contenido y Tailwind procese las clases
            iframe.contentWindow?.focus();
            setTimeout(() => {
                iframe.contentWindow?.print();
                // Remover el iframe después de un tiempo para dar chance al diálogo de impresión
                setTimeout(() => {
                    document.body.removeChild(iframe);
                }, 2000);
            }, 1500); // Aumentado para dar tiempo a Tailwind
        }
    }
}
