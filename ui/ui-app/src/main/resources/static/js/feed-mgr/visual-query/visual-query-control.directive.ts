import {Directive, forwardRef, Provider} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

import {Feed} from "../model/feed/feed.model";
import {VisualQueryStepperComponent} from "./visual-query-stepper.component";

export const VISUAL_QUERY_VALUE_ACCESSOR: Provider = {
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => VisualQueryControlDirective),
    multi: true
};

@Directive({
    selector: "visual-query-stepper[formControlName],visual-query-stepper[formControl],visual-query-stepper[ngModel]",
    host: {"(save)": "emitChange()"},
    providers: [VISUAL_QUERY_VALUE_ACCESSOR]
})
export class VisualQueryControlDirective implements ControlValueAccessor {

    onChange: any;
    onTouched: any;

    constructor(private host: VisualQueryStepperComponent) {
    }

    emitChange(): void {
        if (this.onChange) {
            this.onChange(this.host.feed);
        }
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    writeValue(value: Feed): void {
        this.host.setFeed(value);
    }
}
