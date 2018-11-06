import * as _ from "underscore";
import { RegisterTemplateServiceFactory } from '../../../services/RegisterTemplateServiceFactory';
import { Directive, OnInit, Input, OnChanges, SimpleChanges, ElementRef } from '@angular/core';

@Directive({
    selector:'[thinkbig-derived-expression]'
})
export class DerivedExpression implements OnChanges {

    @Input() model: any;
    
    ngOnChanges(changes: SimpleChanges) {
        if(changes.model.currentValue) {
            if (changes.model.currentValue != null) {
                var derivedValue = this.RegisterTemplateService.deriveExpression(changes.model.currentValue, true)
                $(this.elRef.nativeElement).html(derivedValue);
            }
            else {
                $(this.elRef.nativeElement).html('');
            }
        }
    }
    constructor(private RegisterTemplateService : RegisterTemplateServiceFactory,
                private elRef: ElementRef) {}
}