import { Directive, EventEmitter, HostBinding, HostListener, Output } from '@angular/core';

@Directive({
    selector: '[draggableElement]'
})
export class BaseDraggableDirective {
    @HostBinding('class.draggable') draggable = true;

    // to trigger pointer-events polyfill
    @HostBinding('attr.touch-action') touchAction = 'none';

    @Output() dragStart = new EventEmitter<PointerEvent>();
    @Output() dragMove = new EventEmitter<PointerEvent>();
    @Output() dragEnd = new EventEmitter<PointerEvent>();

    @HostBinding('class.dragging') dragging = false;

    @HostListener('pointerdown', ['$event'])
    onPointerDown(event: PointerEvent): void {
        this.dragging = true;
        this.dragStart.emit(event);
    }

    @HostListener('document:pointermove', ['$event'])
    onPointerMove(event: PointerEvent): void {
        if (!this.dragging) {
            return;
        }

        this.dragMove.emit(event);
    }

    @HostListener('document:pointerup', ['$event'])
    onPointerUp(event: PointerEvent): void {
        if (!this.dragging) {
            return;
        }

        this.dragging = false;
        this.dragEnd.emit(event);
    }
}