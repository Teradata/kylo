import {FormGroup} from "@angular/forms";
import {TemplateRef, ViewChild} from "@angular/core";

/**
 * Any additional step in the preview stepper needs to implement this interface
 */
export interface PreviewStepperStep<T>{
    data:T;

    stepControl:FormGroup;

    templateRef: TemplateRef<any>;

    init():void;

    /**
     * return any saved data you want passed to the subscriber
     */
    onSave():any;

}