const fs = require('fs');
const ordenPath = 'C:/Users/HP/Documents/GitHub/articulos/articulos-frontend/src/app/features/compra/orden/orden-form/orden-form.component.html';
const registroPath = 'C:/Users/HP/Documents/GitHub/articulos/articulos-frontend/src/app/features/compra/registro/registro-form/registro-form.component.html';

let ordenHtml = fs.readFileSync(ordenPath, 'utf8');
let registroHtml = fs.readFileSync(registroPath, 'utf8');

const regStartTag = '<!-- Tabla Desktop -->';
const endTag = '<!-- Card 3: Totales y Acciones -->';

const regStart = registroHtml.indexOf(regStartTag);
const regEnd = registroHtml.indexOf(endTag);
if (regStart === -1 || regEnd === -1) {
    console.log('Could not find tags in registro');
    process.exit(1);
}
let tableChunk = registroHtml.substring(regStart, regEnd);

// Remove the last </div> <!-- End of Card 1 & 2 wrapper --> from the chunk to match orden-form structure
tableChunk = tableChunk.replace(/<\/div>\s*<!-- End of Card 1 & 2 wrapper -->\s*$/, '');

// Clean up table chunk
tableChunk = tableChunk.replace(/\[disabled\]="pagoSolicitado\(\) \|\| pagoConfirmado\(\)"/g, '');
// Remove abrirCalculadora button
tableChunk = tableChunk.replace(/<button type="button" \(click\)="abrirCalculadora\(\$index\)"[\s\S]*?<\/button>/g, '');

const ordStartTag = '<!-- Tabla Desktop -->';
const ordStart = ordenHtml.indexOf(ordStartTag);
const ordEnd = ordenHtml.indexOf(endTag);
if (ordStart === -1 || ordEnd === -1) {
    console.log('Could not find tags in orden');
    process.exit(1);
}

const newOrdenHtml = ordenHtml.substring(0, ordStart) + tableChunk + ordenHtml.substring(ordEnd);
fs.writeFileSync(ordenPath, newOrdenHtml, 'utf8');
console.log('Replacement successful!');
