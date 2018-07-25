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
        const elementByClass = this.el.nativeElement.getElementsByClassName(this.visibleOnMouseOver);
        elementByClass[0].style.visibility = 'visible';
    }
    onMouseLeave() {
        const elementByClass = this.el.nativeElement.getElementsByClassName(this.visibleOnMouseOver);
        elementByClass[0].style.visibility = 'hidden';
    }
}