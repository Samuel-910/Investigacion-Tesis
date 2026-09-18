import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class TraductorPlantillaService {

    constructor() { }

    traducir(html: string): string {
        if (!html) return '';

        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');

        const elementosIterativos = doc.querySelectorAll('[data-iterativa="true"]');
        elementosIterativos.forEach(el => {
            el.classList.add('js-fila-iterativa');
            // Mantenemos el data-iterativa="true" para el nuevo motor JS
        });

        const elementosConToken = doc.querySelectorAll('[token_value]');
        elementosConToken.forEach(el => {
            const tokenRaw = el.getAttribute('token_value');
            if (tokenRaw) {
                let tokenClean = tokenRaw.replace(/[{}]/g, '').trim();

                const isInsideIterative = el.closest('[data-iterativa="true"], .js-fila-iterativa');
                if (isInsideIterative && !tokenClean.startsWith('detalle.')) {
                    if (tokenClean.includes('.')) {
                        tokenClean = 'detalle.' + tokenClean.split('.').slice(1).join('.');
                    } else {
                        tokenClean = 'detalle.' + tokenClean;
                    }
                }

                if (el.tagName === 'IMG') {
                    el.setAttribute('src', `{{ ${tokenClean} }}`);
                } else {
                    el.textContent = `{{ ${tokenClean} }}`;
                }

                el.removeAttribute('token_value');
                el.classList.remove('variable-token');
                el.classList.remove('variable-token-img');
            }
        });

        const todosLosElementos = doc.querySelectorAll('*');
        todosLosElementos.forEach(el => {
            el.removeAttribute('data-gjs-type');
            el.removeAttribute('data-gjs-draggable');
            el.removeAttribute('data-gjs-droppable');
            el.removeAttribute('data-gjs-custom-name');
            el.removeAttribute('draggable');

            if (el.classList.length === 0) {
                el.removeAttribute('class');
            }
        });
        let finalHtml = doc.body.innerHTML;
        finalHtml = finalHtml.replace(/\*ngfor=/g, '*ngFor=');

        return finalHtml;
    }
}
