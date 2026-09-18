import type { Editor } from 'grapesjs';

export class CanvasManager {
    constructor(private editor: Editor) { }

    actualizarAreaTrabajo(anchoPx: number, altoPx: number, orientacion: string) {
        let w = anchoPx;
        let h = altoPx;

        if (orientacion === 'Horizontal') {
            [w, h] = [h, w];
        }

        // 1. EL FRAME: Debe tener el tamaño exacto de la hoja para limitar el arrastre
        // GrapesJS no permite sacar elementos fuera del Frame en modo absoluto
        this.editor.Canvas.getFrames().at(0)?.set({
            width: `${w}px`,
            height: `${h}px`
        });

        // 2. EL WRAPPER: Ocupa todo el frame
        const wrapper = this.editor.getWrapper();
        const wrapperEl = wrapper?.getEl();
        if (wrapperEl) {
            wrapperEl.setAttribute('style', `
                width: 100% !important;
                height: 100% !important;
                background-color: white !important;
                position: relative !important;
                margin: 0 !important;
                overflow: hidden !important;
            `);
        }

        // 3. EL BODY DEL CANVAS (Interno al Frame)
        const body = this.editor.Canvas.getBody();
        if (body) {
            body.setAttribute('style', `
                background-color: white !important;
                margin: 0 !important;
                padding: 0 !important;
            `);
        }

        this.editor.refresh();
    }
}
