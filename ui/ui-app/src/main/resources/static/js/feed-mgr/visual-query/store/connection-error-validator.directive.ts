import {Directive, Input} from "@angular/core";
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from "@angular/forms";

@Directive({
    selector: "[connectionError]",
    providers: [{provide: NG_VALIDATORS, useExisting: ConnectionErrorValidatorDirective, multi: true}]
})
export class ConnectionErrorValidatorDirective implements Validator {

    @Input("connectionError")
    connectionError: boolean;

    validate(control: AbstractControl): ValidationErrors {
        return this.connectionError ? {connectionError: true} : null;
    }
}
