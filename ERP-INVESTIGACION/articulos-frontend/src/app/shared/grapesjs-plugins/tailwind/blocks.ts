import type { Editor } from "grapesjs";
import type { PluginOptions } from ".";

export const blocks = (editor: Editor, opts: PluginOptions = {}) => {
    const bm = editor.BlockManager;

    bm.add('group-block', {
        label: 'Bloque Div',
        category: 'Estructura',
        attributes: { class: 'gjs-fonts gjs-f-b1' },
        content: `
        <div class="group-block" data-gjs-droppable="true" data-gjs-draggable="true" data-gjs-resizable="true" data-gjs-type="default" style="box-sizing: border-box; display: block; position: relative;">
        </div>
        `,
        media: '<svg viewBox="0 0 24 24"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z"/><path d="M7 7h10v2H7v-2zm0 4h10v2H7v-2zm0 4h7v2H7v-2z"/></svg>'
    });

    bm.add('stack-block', {
        label: 'Contenedor Apilable',
        category: 'Estructura',
        attributes: { class: 'gjs-fonts gjs-f-b2' },
        content: `
        <div class="stack-block group-block" data-gjs-droppable="true" data-gjs-draggable="true" data-gjs-resizable="true" data-gjs-type="default" style="box-sizing: border-box; display: flex; flex-direction: column; position: absolute;">
        </div>
        `,
        media: '<svg viewBox="0 0 24 24"><path d="M4 5h16v3H4zm0 6h16v3H4zm0 6h16v3H4z"/></svg>'
    });
    bm.add('flex-row', {
        label: 'Fila Horizontal',
        category: 'Estructura',
        attributes: { class: 'gjs-fonts gjs-f-b3' },
        content: `
        <div class="flex-row group-block" data-gjs-droppable="true" data-gjs-draggable="true" style="display: flex; flex-direction: row; gap: 10px; align-items: center; min-height: 50px; padding: 10px; border: 2px dashed #3b82f6; background-color: rgba(59, 130, 246, 0.05); box-sizing: border-box;">
        </div>
        `,
        media: '<svg viewBox="0 0 24 24"><path d="M21 11H3V9h18v2zm0 4H3v-2h18v2z"/></svg>'
    });

    bm.add('text-block', {
        label: 'Bloque Texto',
        category: 'Estructura',
        attributes: { class: 'gjs-fonts gjs-f-text' },
        content: `
        <div class="text-block" data-gjs-type="default" data-gjs-droppable="true" data-gjs-draggable="true" data-gjs-resizable="true" contenteditable="true" style="padding: 5px; min-height: 20px;">
            Inserte su texto aquí
        </div>
        `,
        media: '<svg viewBox="0 0 24 24"><path d="M5 4v3h5.5v12h3V7H19V4z"/></svg>'
    });

    bm.add('boleta-cabecera', {
        label: 'Cabecera Boleta',
        category: 'Documentos',
        content: `
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 1rem; border: 2px solid #1f2937; background-color: #ffffff; box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);">
            <div style="width: 66.666667%;">
                <h2 style="font-size: 1.25rem; font-weight: 700; text-transform: uppercase; margin: 0;">Clínica Particular</h2>
                <p style="font-size: 0.875rem; margin: 0;">Juliaca, Puno - Perú</p>
                <p style="font-size: 0.75rem; color: #4b5563; margin: 0;">Dirección: Av. Nueva Esperanza s/n</p>
            </div>
            <div style="width: 33.333333%; border: 2px solid #dc2626; padding: 0.5rem; text-align: center; border-radius: 0.25rem;">
                <p style="color: #dc2626; font-weight: 700; margin: 0;">R.U.C. 20123456789</p>
                <h3 style="background-color: #dc2626; color: #ffffff; font-weight: 700; margin: 0.25rem 0; padding: 0.25rem;">BOLETA DE VENTA</h3>
                <p style="color: #dc2626; font-weight: 700; margin: 0;">001 - N° 000001</p>
            </div>
        </div>
    `,
        media: '<svg viewBox="0 0 24 24"><path d="M7 2v20h10V2H7zm8 18H9V4h6v16z"></path></svg>'
    });

    bm.add('boleta-detalle', {
        label: 'Tabla Detalle',
        category: 'Documentos',
        content: `
        <table style="width: 100%; margin-top: 1rem; border-collapse: collapse; border: 1px solid #9ca3af; font-size: 0.875rem;">
            <thead>
                <tr style="background-color: #f3f4f6; text-transform: uppercase; font-weight: 700; text-align: center;">
                    <th style="border: 1px solid #9ca3af; padding: 0.5rem; width: 4rem;">Cant.</th>
                    <th style="border: 1px solid #9ca3af; padding: 0.5rem;">Descripción</th>
                    <th style="border: 1px solid #9ca3af; padding: 0.5rem; width: 6rem;">P. Unit.</th>
                    <th style="border: 1px solid #9ca3af; padding: 0.5rem; width: 6rem;">Importe</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td style="border: 1px solid #9ca3af; padding: 0.5rem; text-align: center; height: 2.5rem;"></td>
                    <td style="border: 1px solid #9ca3af; padding: 0.5rem;"></td>
                    <td style="border: 1px solid #9ca3af; padding: 0.5rem; text-align: right;"></td>
                    <td style="border: 1px solid #9ca3af; padding: 0.5rem; text-align: right;"></td>
                </tr>
            </tbody>
        </table>
    `,
        media: '<svg viewBox="0 0 24 24"><path d="M3 3h18v18H3V3zm16 16V5H5v14h14zM7 7h10v2H7V7zm0 4h10v2H7v-2zm0 4h7v2H7v-2z"></path></svg>'
    });

    if (opts.bloques && Array.isArray(opts.bloques)) {
        opts.bloques.forEach(bloque => {
            bm.add(`dynamic-block-${bloque.id || Math.random()}`, {
                label: bloque.nombre,
                category: bloque.categoria || 'Personalizados',
                content: bloque.htmlContenido + (bloque.cssEstilo ? `<style>${bloque.cssEstilo}</style>` : ''),
                media: bloque.imagenUrl
                    ? `<img src="${bloque.imagenUrl}" style="width:100%; height:auto;" />`
                    : '<svg viewBox="0 0 24 24"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z"/><path d="M7 7h10v2H7zm0 4h10v2H7zm0 4h7v2H7z"/></svg>'
            });
        });
    }

    console.log('Bloques predefinidos cargados con CSS puro');
};
