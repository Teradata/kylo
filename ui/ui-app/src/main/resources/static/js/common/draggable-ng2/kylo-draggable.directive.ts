import {Directive, ElementRef, HostBinding, HostListener, Input} from "@angular/core";
import {DomSanitizer, SafeStyle} from "@angular/platform-browser";
import {BaseDraggableDirective} from "./base-draggable.directive";

interface Position {
    x: number;
    y: number;
}

@Directive({
    selector: '[kyloDraggable]'
})
export class KyloDraggableDirective extends BaseDraggableDirective {
    @HostBinding('style.transform') get transform(): SafeStyle {
        return this.sanitizer.bypassSecurityTrustStyle(
            `translateX(${this.position.x}px) translateY(${this.position.y}px)`
        );
    }

    @HostBinding('class.movable') movable = true;

    position: Position = {x: 0, y: 0};

    private startPosition: Position;

    @Input('appMovableReset') reset = false;

    constructor(private sanitizer: DomSanitizer, public element: ElementRef) {
        super();
    }

    @HostListener('dragStart', ['$event'])
    onDragStart(event: PointerEvent) {
        this.startPosition = {
            x: event.clientX - this.position.x,
            y: event.clientY - this.position.y
        }
    }

    @HostListener('dragMove', ['$event'])
    onDragMove(event: PointerEvent) {
        this.position.x = event.clientX - this.startPosition.x;
        this.position.y = event.clientY - this.startPosition.y;
    }

    @HostListener('dragEnd', ['$event'])
    onDragEnd(event: PointerEvent) {
        if (this.reset) {
            this.position = {x: 0, y: 0};
        }
    }
}