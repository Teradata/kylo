import {Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import * as angular from "angular";
import {Subject} from "rxjs/Subject";
import * as $ from "jquery";

/**
 * Displays an SVG icon using AngularJS's ng-md-icon.
 */
@Component({
    selector: "kylo-icon,ng-md-icon",
    template: ""
})
export class KyloIconComponent implements OnChanges, OnInit {

    /**
     * Name of the icon
     */
    @Input()
    public icon: string;

    /**
     * Size of the icon
     */
    @Input()
    public size: string;

    private iconObserver = new Subject();

    private sizeObserver = new Subject();

    constructor(private element: ElementRef) {
    }

    public ngOnInit(): void {
        const $element = $(this.element.nativeElement);
        $(document.body).injector().get("ngMdIconDirective")[0].link(this, $element, this);
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.icon) {
            this.iconObserver.next(changes.icon.currentValue);
        }
        if (changes.size) {
            this.sizeObserver.next(changes.size.currentValue);
        }
    }

    public $observe<T>(name: string, fn: (value?: T) => any): void {
        if (name === "icon") {
            this.iconObserver.subscribe(fn);
        } else if (name === "size") {
            this.sizeObserver.subscribe(fn);
        }
    }
}
