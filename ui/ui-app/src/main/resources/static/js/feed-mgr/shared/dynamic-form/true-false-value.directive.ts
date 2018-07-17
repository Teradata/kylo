import {Directive, Input, forwardRef, HostListener, ElementRef, Renderer2, NO_ERRORS_SCHEMA} from '@angular/core';
import {ControlValueAccessor,NG_VALUE_ACCESSOR,NgControl} from '@angular/forms';

@Directive({
    selector: ' input[type=checkbox][trueFalseValue]',
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => TrueFalseValueDirective),
            multi: true
        }
    ]
})
export class TrueFalseValueDirective implements ControlValueAccessor {
    private propagateChange = (_: any) => {};
    @Input() trueValue = true;
    @Input() falseValue = false;

    constructor(private elementRef: ElementRef, private renderer: Renderer2) {}

    @HostListener('change', ['$event'])
    onHostChange(ev :any) {
        this.propagateChange(ev.target.checked ? this.trueValue : this.falseValue);
    }

    writeValue(obj: any): void {
        if (obj === this.trueValue) {
            this.renderer.setProperty(this.elementRef.nativeElement, 'checked', true);
        } else {
            this.renderer.setProperty(this.elementRef.nativeElement, 'checked', false);
        }
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
    }

    registerOnTouched(fn: any): void {}

    setDisabledState?(isDisabled: boolean): void {}
}