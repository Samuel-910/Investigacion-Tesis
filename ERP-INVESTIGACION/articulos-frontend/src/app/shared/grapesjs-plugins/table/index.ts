import { Editor } from 'grapesjs';

export default (editor: Editor, opts = {}) => {
    const domc = editor.Components;
    const blockManager = editor.Blocks;

    const defaultTableProps = {
        type: 'table',
        tagName: 'table',
        droppable: true,
        draggable: true,
        resizable: {
            step: 1,
            minDim: 5,
            handles: 'e, w', // Enfocarse en horizontal para evitar conflictos con filas
        },
        attributes: {
            class: 'text-sm text-left rtl:text-right text-gray-500 border-collapse',
            border: '1',
            cellpadding: '5',
            cellspacing: '0',
            style: 'min-width: 100px; min-height: 50px; width: auto;' // width auto para permitir reducción
        },
    };

    const defaultCellProps = {
        type: 'cell',
        tagName: 'td',
        draggable: false,
        removable: true,
        resizable: true,
        editable: true, // Permitir escribir texto
        badgable: false, // Evitar que la etiqueta oculte el contenido
        attributes: { class: 'px-6 py-4 border' },
    };

    const defaultHeaderProps = {
        type: 'header-cell',
        tagName: 'th',
        draggable: false,
        removable: true,
        resizable: true,
        editable: true,
        badgable: false,
        attributes: { class: 'px-6 py-3 border bg-gray-100', scope: 'col' },
    };

    const defaultRowProps = {
        type: 'row',
        tagName: 'tr',
        draggable: false,
        removable: true,
        resizable: false,
        badgable: false,
        attributes: { class: 'bg-white border-b' },
        traits: [
            {
                type: 'checkbox',
                name: 'data-iterativa',
                label: '¿Es Iterativa?',
                valueTrue: 'true',
                valueFalse: 'false',
            }
        ]
    };

    // Define Table Component
    domc.addType('table', {
        isComponent: (el) => el.tagName === 'TABLE',
        model: {
            defaults: {
                ...defaultTableProps,
                components: [
                    {
                        tagName: 'thead',
                        attributes: { class: 'text-xs text-gray-700 uppercase bg-gray-50' },
                        components: [
                            {
                                type: 'row',
                                components: [
                                    { type: 'header-cell', content: 'Header 1' },
                                    { type: 'header-cell', content: 'Header 2' },
                                    { type: 'header-cell', content: 'Header 3' },
                                ],
                            },
                        ],
                    },
                    {
                        tagName: 'tbody',
                        components: [
                            {
                                type: 'row',
                                components: [
                                    { type: 'cell', content: 'Cell 1' },
                                    { type: 'cell', content: 'Cell 2' },
                                    { type: 'cell', content: 'Cell 3' },
                                ],
                            },
                        ],
                    },
                ],
            },
        },
    });

    // Define Row Component
    domc.addType('row', {
        isComponent: (el) => el.tagName === 'TR',
        model: {
            defaults: {
                ...defaultRowProps,
                traits: defaultRowProps.traits, // Asegurar que hereda los traits
            },
        },
    });

    // Define Cell Component
    domc.addType('cell', {
        isComponent: (el) => el.tagName === 'TD',
        model: {
            defaults: {
                ...defaultCellProps,
            },
        },
    });

    // Define Header Cell Component
    domc.addType('header-cell', {
        isComponent: (el) => el.tagName === 'TH',
        model: {
            defaults: {
                ...defaultHeaderProps,
            },
        },
    });

    // Add Block
    blockManager.add('table', {
        category: 'Básico',
        label: 'Tabla Simple',
        attributes: { class: 'fa fa-table' },
        content: { type: 'table' },
    });

    blockManager.add('table-detalles', {
        category: 'Básico',
        label: 'Tabla de Detalles',
        attributes: { class: 'fa fa-list-alt' },
        content: {
            type: 'table',
            components: [
                {
                    tagName: 'thead',
                    attributes: { class: 'text-xs text-gray-700 uppercase bg-gray-50' },
                    components: [
                        {
                            type: 'row',
                            components: [
                                { type: 'header-cell', content: 'Descripción' },
                                { type: 'header-cell', content: 'Cant.' },
                                { type: 'header-cell', content: 'P. Unit' },
                                { type: 'header-cell', content: 'Total' },
                            ],
                        },
                    ],
                },
                {
                    tagName: 'tbody',
                    components: [
                        {
                            type: 'row',
                            attributes: { 'data-iterativa': 'true' },
                            components: [
                                { type: 'cell', components: [{ type: 'variable-dinamica', attributes: { token_value: '{{item.nombre}}' }, content: '{{item.nombre}}' }] },
                                { type: 'cell', components: [{ type: 'variable-dinamica', attributes: { token_value: '{{item.cantidad}}' }, content: '{{item.cantidad}}' }] },
                                { type: 'cell', components: [{ type: 'variable-dinamica', attributes: { token_value: '{{item.precio_unitario}}' }, content: '{{item.precio_unitario}}' }] },
                                { type: 'cell', components: [{ type: 'variable-dinamica', attributes: { token_value: '{{item.total}}' }, content: '{{item.total}}' }] },
                            ],
                        },
                    ],
                },
            ],
        },
    });
};
