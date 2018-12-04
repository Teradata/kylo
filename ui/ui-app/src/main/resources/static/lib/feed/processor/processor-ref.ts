import {AbstractControl, FormArray, FormGroup} from "@angular/forms";

import {Processor} from "../model/processor";

export class ProcessorRef {

    readonly form = new FormArray([]);

    formGroup: FormGroup;

    constructor(readonly processor: Processor, readonly feed?: any) {
    }

    get control(): AbstractControl {
        return this.form.at(0);
    }

    set control(control: AbstractControl) {
        if (control != null) {
            this.form.setControl(0, control);
        } else {
            this.form.removeAt(0);
        }
    }

    get id(): string {
        return this.processor.id;
    }

    get name(): string {
        return this.processor.name;
    }

    get type(): string {
        return this.processor.type;
    }
}
