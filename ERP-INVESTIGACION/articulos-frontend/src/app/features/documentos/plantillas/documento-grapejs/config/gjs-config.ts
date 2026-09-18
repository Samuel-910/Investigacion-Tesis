import type { EditorConfig } from 'grapesjs';

export const GJS_CONFIG_BASE: Partial<EditorConfig> = {
    height: '100%',
    fromElement: true,
    storageManager: false,
    selectorManager: {
        componentFirst: true,
    },
    dragMode: 'absolute',
    undoManager: { maximumStackLength: 20 },
    i18n: {
        locale: 'es',
        messages: {
            es: {
                assetManager: { addButton: 'Añadir imagen' },
                blockManager: {
                    labels: { 'search': 'Buscar bloques' },
                    categories: { 'General': 'Estructura' }
                },
                styleManager: {
                    sectors: {
                        general: 'General',
                        dimension: 'Dimensión',
                        typography: 'Tipografía',
                        decorations: 'Decoración',
                        extra: 'Extra',
                        flex: 'Flex'
                    },
                    properties: {
                        display: 'Mostrar',
                        float: 'Flotante',
                        position: 'Posición',
                        top: 'Superior',
                        right: 'Derecha',
                        left: 'Izquierda',
                        bottom: 'Inferior',
                        width: 'Ancho',
                        height: 'Alto',
                        'max-width': 'Ancho máx.',
                        auto: 'Automático',
                        margin: 'Margen',
                        padding: 'Relleno',
                        'font-family': 'Fuente',
                        'font-size': 'Tamaño letra',
                        'font-weight': 'Grosor letra',
                        'letter-spacing': 'Espaciado letras',
                        color: 'Color',
                        'line-height': 'Interlineado',
                        'text-align': 'Alineación',
                        'text-shadow': 'Sombra texto',
                        'background-color': 'Color fondo',
                        'border-radius': 'Radio borde',
                        border: 'Borde',
                        'box-shadow': 'Sombra caja',
                        background: 'Fondo'
                    }
                },
                deviceManager: {
                    device: 'Dispositivo'
                }
            }
        }
    }
};

export const CANVAS_STYLES = [
    '.gjs-resizer-h, .gjs-resizer-v, .gjs-resizer-c { background-color: #3b82f6 !important; opacity: 1 !important; border: 1px solid white !important; width: 10px !important; height: 10px !important; border-radius: 999px !important; z-index: 999; }',
    'table { min-width: 50px; min-height: 20px; transition: none !important; table-layout: fixed !important; display: table !important; width: 100vw; border-collapse: collapse; }',
    'table.gjs-selected { outline: 2px solid #3b82f6 !important; }',
    'th, td { border: 1px solid #ddd; min-width: 20px; padding: 4px; position: relative; word-break: break-word; overflow-wrap: anywhere; vertical-align: top; }',
    'th { background-color: #f3f4f6; font-weight: bold; text-align: left; }',
    '.group-block { min-height: 50px; min-width: 50px; border: 1px dashed #9ca3af !important; background-color: rgba(243, 244, 246, 0.3); transition: background-color 0.2s, border-color 0.2s; }',
    '.group-block:hover { border-color: #3b82f6 !important; background-color: rgba(59, 130, 246, 0.05); }',
    '.group-block.gjs-selected { border: 2px solid #3b82f6 !important; border-style: solid !important; }',
    '.group-block-hover { background-color: rgba(59, 130, 246, 0.2) !important; border: 2px dashed #2563eb !important; outline: 3px solid rgba(59, 130, 246, 0.4); }',
    '.group-block-hover::before { content: "Se agrupará aquí"; position: absolute; top: 0px; left: 50%; transform: translate(-50%, -50%); background: #2563eb; color: white; padding: 2px 8px; font-size: 11px; font-family: sans-serif; border-radius: 10px; z-index: 100; pointer-events: none; }'
];
