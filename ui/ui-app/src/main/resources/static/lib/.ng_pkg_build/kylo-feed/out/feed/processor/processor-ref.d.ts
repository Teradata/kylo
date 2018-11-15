import { AbstractControl, FormArray, FormGroup } from "@angular/forms";
import { Processor } from "../model/processor";
export declare class ProcessorRef {
    readonly processor: Processor;
    readonly feed?: any;
    readonly form: FormArray;
    formGroup: FormGroup;
    constructor(processor: Processor, feed?: any);
    control: AbstractControl;
    readonly id: string;
    readonly name: string;
    readonly type: string;
}
