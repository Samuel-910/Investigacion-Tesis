import { Component, EventEmitter, Input, OnInit, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Clinica } from '../../models/empresa.model';
import { PrimaryButtonComponent } from '../../../../shared/components/primary-button/primary-button';
import { AlertService } from '../../../../core/services/alert.service';
import { FormInputComponent } from '../../../../shared/components/forms/form-input/form-input.component';

@Component({
    selector: 'app-clinica-form',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        PrimaryButtonComponent,
        FormInputComponent
    ],
    templateUrl: './clinica-form.component.html'
})
export class ClinicaFormComponent implements OnInit {
    private _clinica: Clinica | null = null;
    @Input() set clinica(value: Clinica | null) {
        this._clinica = value;
        if (value) {
            this.patchForm(value);
        } else {
            this.clinicaForm?.reset({ estado: 'ACTIVO', logoPrincipal: 'CUADRADO' });
            this.logoCuadradoPreview.set(null);
            this.logoRectangularPreview.set(null);
        }
    }
    get clinica(): Clinica | null { return this._clinica; }

    private patchForm(data: Clinica): void {
        if (!this.clinicaForm) return;

        console.log('Editando clínica con ID:', data.id);

        this.clinicaForm.patchValue({
            ...data,
            estado: this.getNormalizedEstado(data.estado)
        });

        this.logoCuadradoPreview.set(data.logoCuadrado || null);
        this.logoRectangularPreview.set(data.logoRectangular || null);
    }

    @Output() onSave = new EventEmitter<Clinica>();
    @Output() onCancel = new EventEmitter<void>();

    clinicaForm: FormGroup;
    logoCuadradoPreview = signal<string | null>(null);
    logoRectangularPreview = signal<string | null>(null);

    constructor(private fb: FormBuilder, private alertService: AlertService) {
        this.clinicaForm = this.fb.group({
            id: [null],
            razonSocial: ['', [Validators.required]],
            ruc: ['', [Validators.required, Validators.pattern('^[0-9]{11}$')]],
            email: ['', [Validators.email]],
            representante: [''],
            auditor: [''],
            liquidador: [''],
            financiero: [''],
            web: [''],
            ctacte: [''],
            codigo: [''],
            abrev: [''],
            logoCuadrado: [null],
            logoRectangular: [null],
            logoPrincipal: ['CUADRADO'],
            estado: ['ACTIVO']
        });
    }

    ngOnInit(): void {
    }

    onFileSelected(event: any, type: 'CUADRADO' | 'RECTANGULAR'): void {
        const file = event.target.files[0];
        if (file) {
            if (file.size > 2 * 1024 * 1024) {
                this.alertService.warning('La imagen es demasiado grande. Máximo 2MB.');
                return;
            }

            const reader = new FileReader();
            reader.onload = () => {
                const base64String = reader.result as string;
                if (type === 'CUADRADO') {
                    this.logoCuadradoPreview.set(base64String);
                    this.clinicaForm.patchValue({ logoCuadrado: base64String });
                } else {
                    this.logoRectangularPreview.set(base64String);
                    this.clinicaForm.patchValue({ logoRectangular: base64String });
                }
            };
            reader.readAsDataURL(file);
        }
    }

    removeLogo(type: 'CUADRADO' | 'RECTANGULAR'): void {
        if (type === 'CUADRADO') {
            this.logoCuadradoPreview.set(null);
            this.clinicaForm.patchValue({ logoCuadrado: null });
        } else {
            this.logoRectangularPreview.set(null);
            this.clinicaForm.patchValue({ logoRectangular: null });
        }
    }

    seleccionarPrincipal(type: 'CUADRADO' | 'RECTANGULAR'): void {
        this.clinicaForm.patchValue({ logoPrincipal: type });
    }

    submit(): void {
        if (this.clinicaForm.valid) {
            this.onSave.emit(this.clinicaForm.value);
        } else {
            const missingFields = this.getMissingFields();
            this.alertService.warning(`Por favor, complete los siguientes campos obligatorios: ${missingFields.join(', ')}`);
        }
    }

    private getMissingFields(): string[] {
        const missing: string[] = [];
        const controls = this.clinicaForm.controls;

        const fieldNames: { [key: string]: string } = {
            razonSocial: 'Razón Social',
            ruc: 'RUC'
        };

        Object.keys(fieldNames).forEach(key => {
            if (controls[key].errors) {
                missing.push(fieldNames[key]);
            }
        });

        if (controls['ruc'].hasError('pattern')) {
            const index = missing.indexOf('RUC');
            if (index !== -1) {
                missing[index] = 'RUC (debe tener 11 dígitos)';
            } else {
                missing.push('RUC (formato inválido)');
            }
        }

        return missing;
    }

    cancel(): void {
        this.onCancel.emit();
    }

    private getNormalizedEstado(estado: any): string {
        if (!estado) return 'ACTIVO';
        if (typeof estado === 'string') return (estado === 'A' || estado === '1') ? 'ACTIVO' : (estado === 'I' || estado === '0' ? 'INACTIVO' : estado);
        if (typeof estado === 'object') return estado.name || 'ACTIVO';
        return 'ACTIVO';
    }
}
