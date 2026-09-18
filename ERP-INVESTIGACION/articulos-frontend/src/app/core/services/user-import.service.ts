import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { UserResponse } from './user.service';
import { Observable, from } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class UserImportService {

    constructor() { }

    /**
     * Lee un archivo Excel y retorna una lista de UserResponse compatibles
     * @param file Archivo Excel subido
     */
    importFromExcel(file: File): Observable<UserResponse[]> {
        return from(this.readFileAsArrayBuffer(file)).pipe(
            map(buffer => {
                const workbook = XLSX.read(buffer, { type: 'array' });
                const sheetName = workbook.SheetNames[0];
                const worksheet = workbook.Sheets[sheetName];

                // Convertir a JSON
                const data: any[] = XLSX.utils.sheet_to_json(worksheet);

                return data.map((row, index) => {
                    // Mapeo flexible según los nombres indicados por el usuario
                    // Soporta variaciones en los encabezados (minusculas/mayusculas)
                    const nombre = row['Nombre'] || row['nombre'] || row['Full Name'] || 'SIN NOMBRE';
                    const dni = row['DNI'] || row['dni'] || row['Documento'] || '';
                    const ruc = row['RUC'] || row['ruc'] || '';
                    const tipoRaw = row['Tipo'] || row['tipo'] || row['Relacion'] || 'BENEFICIARIO';

                    const tipo: 'BENEFICIARIO' | 'FAMILIAR' =
                        tipoRaw.toString().toUpperCase().includes('FAMILIAR') ? 'FAMILIAR' : 'BENEFICIARIO';

                    return {
                        id: -(index + 1), // ID temporal negativo para identificarlos en la UI
                        username: dni || `import_${index}`,
                        email: row['Email'] || row['email'] || '',
                        firstName: nombre.split(' ')[0],
                        lastName: nombre.split(' ').slice(1).join(' '),
                        nombreCompleto: nombre,
                        numdoc: dni.toString(),
                        ruc: ruc.toString(),
                        active: true,
                        roles: ['PACIENTE'],
                        isImported: true,
                        tipoBeneficiario: tipo
                    } as UserResponse;
                });
            })
        );
    }

    private readFileAsArrayBuffer(file: File): Promise<ArrayBuffer> {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = (e) => resolve(e.target?.result as ArrayBuffer);
            reader.onerror = (e) => reject(e);
            reader.readAsArrayBuffer(file);
        });
    }
}
