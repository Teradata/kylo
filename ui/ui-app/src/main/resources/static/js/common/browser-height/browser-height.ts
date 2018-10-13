import {Directive, ElementRef, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges,EventEmitter} from "@angular/core";
import * as $ from "jquery";
import "rxjs/add/observable/fromEvent";
import {Observable} from "rxjs/Observable";
import {Subscription} from "rxjs/Subscription";

/**
 * Directive to auto size the container to fill the rest of the screen based upon the height of the browser window
 *
 * attrs:
 *  - browser-height-selector=  some css selector (i.e. #content) this will be used to determine the height of the element instead of the current element
 *  - browser-height-scroll-y=true/false   show the scroll bar on the content
 *  - browser-height-wait-and-calc= true/false  if true it will wait before calculating the height to get the items in the page default=false
 *  - browser-height-scroll-left or browser-height-scroll-x=##
 *  - browser-height-resize-event=binding to on resize of the window
 *  - browser-height-offset=offset to apply to the height of the element after getting the window size
 *
 */
@Directive({
    selector: "[browser-height]"
})
export class BrowserHeight implements OnDestroy, OnInit {

    @Input("browser-height-selector")
    eleSelector: string;

    @Input("browser-height-scroll-y")
    scrollY: boolean = true;

    @Input("browser-height-wait-and-calc")
    browserHeightWaitAndCalc: boolean;

    @Input("browser-height-scroll-left")
    browserHeightScrollLeft: boolean;

    @Input("browser-height-scroll-x")
    browserHeightScrollX: boolean;

    @Input("browser-height-resize-event")
    bindResize: boolean = true;

    @Input("browser-height-offset")
    offsetHeight: number = 0;

    @Output()
    heightChange = new EventEmitter<any>();

    private ele: JQuery;

    private resizeSubscription: Subscription;

    constructor(private element: ElementRef) {
        (element.nativeElement as Element).classList.add("browser-height");
    }

    ngOnInit(): void {
        this.ele = this.eleSelector ? $(this.element.nativeElement).find(this.eleSelector) : $(this.element.nativeElement);

        if (this.browserHeightWaitAndCalc) {
            setTimeout(() => this.calcHeight(), 1300)
        }

        if (this.bindResize) {
            this.resizeSubscription = Observable.fromEvent(window, "resize").subscribe(() => this.calcHeight());
        }

        setTimeout(() => this.calcHeight(), 10);
    }

    ngOnDestroy(): void {
        if (this.resizeSubscription) {
            this.resizeSubscription.unsubscribe();
        }
    }

    private calcHeight() {
        if(this.offsetHeight == undefined || isNaN(this.offsetHeight)){
            this.offsetHeight = 0;
        }
        let height = $(window).height() - this.offsetHeight;

        this.ele.css("height", height+'px');
        this.ele.css("overflow-x", (this.browserHeightScrollLeft || this.browserHeightScrollX) ? "scroll" : "hidden");
        if (this.scrollY || this.scrollY === null) {
            this.ele.css("overflow-y", "scroll");
        }
        else if(this.scrollY == false){
            this.ele.css("overflow-y", "hidden");
        }
        this.heightChange.emit(height);
    }


}
