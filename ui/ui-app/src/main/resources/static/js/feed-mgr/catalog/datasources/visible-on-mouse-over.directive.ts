import {Directive, ElementRef, Input} from '@angular/core';

@Directive({
    selector: '[visibleOnMouseOver]',
    host: {
        '(mouseenter)': 'onMouseEnter()',
        '(mouseleave)': 'onMouseLeave()'
    }
})
export class VisibleOnMouseOverDirective {
    private el: ElementRef;

    @Input() visibleOnMouseOver: string;

    constructor(el: ElementRef) { this.el = el; }

    onMouseEnter() {
        const element = this.getElement();
        if (element) {
            element.style.visibility = 'visible';
        }
    }

    onMouseLeave() {
        const element = this.getElement();
        if (element) {
            element.style.visibility = 'hidden';
        }
    }

    private getElement() {
        return this.el.nativeElement.getElementsByClassName(this.visibleOnMouseOver)[0];
    }
}