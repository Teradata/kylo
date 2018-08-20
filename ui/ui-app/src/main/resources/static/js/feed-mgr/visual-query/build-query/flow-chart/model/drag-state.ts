import {Injectable} from "@angular/core";

export enum DragType {
    CONNECTION=1, NODE=2, SELECTING=3, NONE=4
}

export class DragState {

    dragType:DragType =DragType.NONE;

    /**
     * The node being dragged or the node associated with the connection being dragged.  null if not dragging
     */
    activeNode:any;

    /**
     * The connection being dragged or null if not dragging
     */
    activeConnector:any;

    /**
     * The starting event associated with the drag
     */
    dragStartEvent:MouseEvent


    /**
     * The last known coords for the node that is actively being dragged
     */
    lastNodeMouseCoords:any;


    public isDragging(){
        return this.dragType != DragType.NONE;
    }

    public isDraggingNodeOrConnection(){
        return this.dragType == DragType.NODE || this.dragType == DragType.CONNECTION;
    }

    public isDraggingConnectionOrSelecting(){
        return this.dragType == DragType.SELECTING || this.dragType == DragType.CONNECTION;
    }

    public isOtherType(dragType:DragType){
        return this.dragType != dragType;
    }

    public isDraggingConnection(){
        return this.dragType == DragType.CONNECTION;
    }

    public isDraggingNode(){
        return this.dragType == DragType.NODE;
    }

    public isSelecting() {
        return this.dragType == DragType.SELECTING;
    }

    dragNode(node:any){
        this.dragType = DragType.NODE;
        this.activeNode = node;
    }


    dragConnector(node:any, connector:any){
        this.dragType = DragType.CONNECTION;
        this.activeNode = node;
        this.activeConnector = connector;
    }

    dragSelect(){
        this.dragType = DragType.SELECTING;
        this.activeConnector = null;
        this.activeNode = null;
    }

    endDragging(){
        this.dragType = DragType.NONE;
        this.activeNode= null;
        this.activeConnector = null;
    }





}