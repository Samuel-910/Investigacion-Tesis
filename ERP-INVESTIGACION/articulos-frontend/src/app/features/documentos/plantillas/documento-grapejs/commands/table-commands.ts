import type { Editor, Component } from 'grapesjs';

export function registerTableCommands(editor: Editor) {
    // Comandos de Tabla
    editor.Commands.add('custom-add-row', {
        run: (editor: Editor) => {
            const selected = editor.getSelected();
            if (!selected) return;
            let rowToClone = selected;
            while (rowToClone && rowToClone.get('tagName') !== 'TR' && rowToClone.parent()) {
                rowToClone = rowToClone.parent()!;
            }
            if (rowToClone && rowToClone.get('tagName') === 'TR') {
                const newRow = rowToClone.clone();
                newRow.components().forEach((cell: any) => {
                    cell.set('content', '');
                    cell.components().reset();
                });
                const parent = rowToClone.parent();
                if (parent) {
                    parent.components().add(newRow, { at: rowToClone.index() + 1 });
                    editor.select(newRow);
                }
            }
        }
    });

    editor.Commands.add('custom-del-row', {
        run: (editor: Editor) => {
            const selected = editor.getSelected();
            if (!selected) return;
            let rowToDelete = selected;
            while (rowToDelete && rowToDelete.get('type') !== 'wrapper' && !rowToDelete.is('tr')) {
                rowToDelete = rowToDelete.parent()!;
            }
            if (rowToDelete && rowToDelete.is('tr')) {
                rowToDelete.remove();
            }
        }
    });

    editor.Commands.add('custom-add-col-left', {
        run: (editor: Editor) => {
            const selected = editor.getSelected();
            if (!selected) return;
            let cell = selected;
            while (cell && !['TD', 'TH'].includes((cell.get('tagName') || '').toUpperCase()) && cell.parent()) {
                cell = cell.parent()!;
            }
            if (cell && ['TD', 'TH'].includes((cell.get('tagName') || '').toUpperCase())) {
                const columnIndex = cell.index();
                const table: any = cell.closest('table');
                if (table && table.find) {
                    table.find('tr').forEach((row: any) => {
                        const cells = row.components();
                        const cellToClone = cells.at(columnIndex) || cells.at(0);
                        if (cellToClone) {
                            const newCell = cellToClone.clone();
                            newCell.set('content', '');
                            newCell.components().reset();
                            cells.add(newCell, { at: columnIndex });
                        }
                    });
                    editor.select(table);
                }
            }
        }
    });

    editor.Commands.add('custom-add-col-right', {
        run: (editor: Editor) => {
            const selected = editor.getSelected();
            if (!selected) return;
            let cell = selected;
            while (cell && !['TD', 'TH'].includes((cell.get('tagName') || '').toUpperCase()) && cell.parent()) {
                cell = cell.parent()!;
            }
            if (cell && ['TD', 'TH'].includes((cell.get('tagName') || '').toUpperCase())) {
                const columnIndex = cell.index();
                const table: any = cell.closest('table');
                if (table && table.find) {
                    table.find('tr').forEach((row: any) => {
                        const cells = row.components();
                        const cellToClone = cells.at(columnIndex) || cells.at(0);
                        if (cellToClone) {
                            const newCell = cellToClone.clone();
                            newCell.set('content', '');
                            newCell.components().reset();
                            cells.add(newCell, { at: columnIndex + 1 });
                        }
                    });
                    editor.select(table);
                }
            }
        }
    });

    editor.Commands.add('custom-del-col', {
        run: (editor: Editor) => {
            const selected = editor.getSelected();
            if (!selected) return;
            let cell = selected;
            while (cell && cell.get('type') !== 'wrapper' && !cell.is('td') && !cell.is('th')) {
                cell = cell.parent()!;
            }
            if (cell && (cell.is('td') || cell.is('th'))) {
                const columnIndex = cell.index();
                const table: any = cell.closest('table');
                if (table && table.find) {
                    table.find('tr').forEach((row: any) => {
                        const cellToRemove = row.components().at(columnIndex);
                        if (cellToRemove) cellToRemove.remove();
                    });
                }
            }
        }
    });

    // Listener para actualizar el toolbar
    editor.on('component:selected', (component: any) => {
        const tagName = component.get('tagName');
        const isTablePart = ['table', 'thead', 'tbody', 'tr', 'td', 'th'].includes(tagName);

        if (isTablePart) {
            const toolbar = component.get('toolbar') || [];
            const cleanToolbar = toolbar.filter((t: any) =>
                !['custom-add-row', 'custom-del-row', 'custom-add-col', 'custom-del-col', 'custom-add-col-left', 'custom-add-col-right'].includes(t.command)
            );

            if (['td', 'th'].includes(tagName)) {
                cleanToolbar.push({
                    attributes: { title: 'Añadir Columna Izquierda' },
                    command: 'custom-add-col-left',
                    label: '<span style="color:#ffffff; font-weight:bold; font-size:16px; margin: 0 2px;">←+</span>'
                });
                cleanToolbar.push({
                    attributes: { title: 'Eliminar Columna' },
                    command: 'custom-del-col',
                    label: '<span style="color:#ffffff; font-weight:bold; font-size:14px; margin: 0 6px;">C-</span>'
                });
                cleanToolbar.push({
                    attributes: { title: 'Añadir Columna Derecha' },
                    command: 'custom-add-col-right',
                    label: '<span style="color:#ffffff; font-weight:bold; font-size:16px; margin: 0 2px;">+→</span>'
                });
            }

            const moveBtn = cleanToolbar.find((t: any) => t.command === 'tlb-move');
            if (moveBtn) {
                moveBtn.attributes = { ...moveBtn.attributes, title: 'Mover Tabla/Elemento' };
            }

            component.set('toolbar', cleanToolbar);
        }
    });
}
