import {Directive, EventEmitter, HostListener, ElementRef, Input, Output, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {FlowchartUtils} from "./flowchart-utils";
import {Observable} from "rxjs/Observable";

export interface Point {
    x: number;
    y: number;
}
export enum DragStatus{
    STARTED=1,DRAGGING=2,STOPPED=3,NOT_DRAGGING=4
}

export interface DragResponse {
    dragStartMouseEvent?:MouseEvent;
    event:MouseEvent;

    startingPoint?:Point;
    currentPoint?:Point
    draggingPointRelativeToStart?:Point;
    endingPoint?:Point;
    draggableId?:string;
    draggableType?:string;
}

@Directive({
    selector: '[draggable]'
})
export class Draggable implements OnInit{

    mousedrag:Observable<MouseEvent>;


    draggableId:string

    draggableType:string

    draggableElement: HTMLElement;


    @Output()
    mouseUp :EventEmitter<MouseEvent>  = new EventEmitter<MouseEvent>();
    @Output()
    mouseDown :EventEmitter<MouseEvent>= new EventEmitter<MouseEvent>();
    @Output()
    mouseMove :EventEmitter<MouseEvent> = new EventEmitter<MouseEvent>();

    @Output()
    dragStarted :EventEmitter<DragResponse>   = new EventEmitter<DragResponse> ();

    @Output() //DragEndResponse
    dragEnded: EventEmitter<DragResponse>   = new EventEmitter<DragResponse> ();

    @Output()
    dragging:EventEmitter<DragResponse>  = new EventEmitter<DragResponse> ()

    @Output()
    clicked:EventEmitter<DragResponse>  = new EventEmitter<DragResponse> ()

    /**
     * Translated starting point for SVG element
     */
    startingPoint:Point = null;

    dragStartMouseEvent:MouseEvent;

    mouseDownEvent:MouseEvent;

    dragStartMouseEventPoint:Point;
    dragEndMouseEventPoint:Point;

    currentMouseEventPoint:Point;


    // When the mouse moves by at least this amount dragging starts.
    dragThreshold: number = 2;

    isDragging:boolean = false;

    @HostListener('mouseup', ['$event'])
    onMouseup(event:MouseEvent) {

        if(this.isDragging){
            this.setIdentifiers();
            //emit the start and end coords?
            this.isDragging = false;
            this.dragEnded.emit(this.newDragResponse(event));
        }
        else {
            this.clicked.emit(this.newDragResponse(event));
        }
        this.mouseDownEvent = null;
        this.mouseUp.next(event);
    }

    @HostListener('mousedown', ['$event'])
    onMousedown(event:MouseEvent) {
        this.setIdentifiers();
        // event.preventDefault();
        this.mouseDownEvent = event;
        this.isDragging = false;
        //clear the ending point
        this.dragEndMouseEventPoint = null;
        this.mouseDown.next(event);
    }

    @HostListener('mousemove', ['$event'])
    onMousemove(event:MouseEvent) {
        //if we have a mouse down check and emit the drag events
        if (this.mouseDownEvent && this.mouseDownEvent != null) {
            this.setIdentifiers();
            let dragStatus = this.mouseMoveDragStatus(event);
            if (DragStatus.STARTED == dragStatus) {
                //translate the coords
                this.dragStartMouseEvent = this.mouseDownEvent;
                this.dragStarted.emit(this.newDragResponse(event));
            }
            else if (DragStatus.DRAGGING == dragStatus) {
                this.dragging.emit(this.newDragResponse(event))
            }
        }

        this.mouseMove.next(event);

    }
    private newDragResponse(event:MouseEvent){
        this.setIdentifiers();
       return {event:event, dragStartMouseEvent:this.dragStartMouseEvent,draggableId:this.draggableId};

    }



    private toPoint(event:MouseEvent){
        return {x:event.pageX,y:event.pageY};
    }

    constructor(public element: ElementRef, public viewContainerRef: ViewContainerRef) {

    }

    ngOnInit(){
        this.setIdentifiers();

    }

    /**
     * Set the id for the element being dragged so it can be easily emitted when the DragResponse events
     */
    private setIdentifiers(){
        if(this.draggableId == undefined || this.draggableId == '') {
            this.draggableId = this.element.nativeElement.id;
        }
        if(this.draggableType == undefined || this.draggableType == '') {
            this.draggableType = this.element.nativeElement.attributes.draggableType ? this.element.nativeElement.attributes.draggableType.value : '';
        }
    }



    private mouseMoveDragStatus(evt:MouseEvent):DragStatus{
        if (!this.isDragging && this.mouseDownEvent != null) {
            //detect if we are dragging
            if (Math.abs(evt.pageX - this.mouseDownEvent.pageX) > this.dragThreshold ||
                Math.abs(evt.pageY - this.mouseDownEvent.pageY) > this.dragThreshold) {
                this.isDragging = true;
                return DragStatus.STARTED;
            }
        }
        return this.isDragging ? DragStatus.DRAGGING : DragStatus.NOT_DRAGGING;
    }


}