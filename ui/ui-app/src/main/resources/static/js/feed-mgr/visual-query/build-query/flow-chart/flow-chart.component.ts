import {DragResponse, Point} from "./draggable.directive";
import {FlowchartUtils} from "./flowchart-utils";
import {MouseOverState, MouseOverType} from "./model/mouse-over-state";
import {Component, ElementRef, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, KeyValueChangeRecord, KeyValueDiffer, KeyValueDiffers, Renderer, DoCheck} from "@angular/core";

import {DragSelectionRect} from "./model/drag-selection-rect";
import {DragState} from "./model/drag-state";
import {FlowChart} from "./model/flow-chart.model"
import * as _ from "underscore";


@Component({
    selector:'flow-chart',
    styleUrls:['./flow-chart.component.scss'],
    templateUrl:'./flow-chart.component.html'
})
export class FlowChartComponent implements  OnInit{

    // The class for connections and connectors.
    connectionClass = 'flow-connection';
    connectorClass = 'flow-connector';
    nodeClass = 'flow-node';

    /**
     * The bounding box
     */
    dragSelectionRect: DragSelectionRect;

    @Input()
    chart: FlowChart.ChartViewModel;

    //Connector attrs


    dragPoint1:Point;
    dragPoint2:Point;
    dragTangent1:Point;
    dragTangent2:Point;

    /**
     * the size of the radius of the circle connector
     * @type {number}
     */
    connectorSize: number = 10;


    /**
     * The SVG element needed to translate coords
     */
    private svgElement:Element;


    public dragState:DragState = new DragState();
    
    private mouseOverState:MouseOverState = new MouseOverState();

    constructor(public element: ElementRef) {


    }

    /**
     * Mouse down event on the entire SVG canvas
     */
    onMouseDown() {
        this.chart.deselectAll();
    }

    onMouseUp(evt:MouseEvent){
        
        if(this.dragState.isDraggingConnection()){
            this.onConnectorDragEnded();
        }
        else if(this.dragState.isDraggingNode()){
            //no op
        }
        else if(this.dragState.isSelecting()) {
            this.onDragRectEnd();
        }
        this.dragState.endDragging();
        evt.preventDefault();
        evt.stopPropagation();
    }

    onMouseMove(evt: MouseEvent) {

        this.updateMouseOverState(evt);

        if(this.dragState.isSelecting()){
            this.onDraggingRect(evt)
        }
        else if(this.dragState.isDraggingConnection()){
            this.onConnectorDragging(evt);
        }
        else  if(this.dragState.isDraggingNode()){
            this.onNodeDragging(evt);
        }
        evt.preventDefault();
        evt.stopPropagation();


    }

    /**
     * Update the mouseover state object
     */
    private updateMouseOverState(evt:MouseEvent) {
        // Clear out all cached mouse over elements.
        this.mouseOverState.clear();
        let mouseOverElement = FlowchartUtils.hitTest(evt.clientX, evt.clientY);
        if (mouseOverElement == null) {
            // Mouse isn't over anything, just clear all.
                 return;
        }


        let ele: JQuery;
        // Figure out if the mouse is over a connection.
        ele = FlowchartUtils.checkForHit(mouseOverElement, this.connectionClass);
        let connection = null;
        if(ele && ele.length >0){
            connection = this.chart.findConnection(ele.get(0).id);
        }
        if(this.mouseOverState.setMouseOverConnection(connection)){
            // Don't attempt to mouse over anything else.
            return;
        }

        // Figure out if the mouse is over a connector.
        ele = FlowchartUtils.checkForHit(mouseOverElement, this.connectorClass);
        let connector = null;
        if(ele && ele.length >0) {
            connector = this.chart.findConnectorById(ele.get(0).id)
        }
        if(this.mouseOverState.setMouseOverConnector(connector)){
            // Don't attempt to mouse over anything else.
            return;
        }

        // Figure out if the mouse is over a node.
        ele = FlowchartUtils.checkForHit(mouseOverElement, this.nodeClass);
        let node = undefined;
        if(ele && ele.length >0){
            node = this.chart.findNode(ele.get(0).id)
        }
        this.mouseOverState.setMouseOverNode(node);
    }

    /**
     * Called when we are dragging to select nodes
     * @param {DragResponse} dragResponse
     */
    onDragStart(response: DragResponse) {
        if(!this.dragState.isDraggingNodeOrConnection() && !this.mouseOverState.isType(MouseOverType.CONNECTOR)  && !this.mouseOverState.isType(MouseOverType.CONNECTION)) {
            //Set the state to selecting
            this.dragState.dragSelect();
            //get the starting point and create the rect object with the starting 
            let startingPoint = this.svgCoordinatesForEvent(response.dragStartMouseEvent)
            this.dragSelectionRect = new DragSelectionRect(startingPoint);
        }
    }

    /**
     * Called when the user is dragging the rectangle to select nodes
     * Update the rectangle view state
     * @param {MouseEvent} event
     */
    onDraggingRect(event: MouseEvent) {
        let currentPoint = this.svgCoordinatesForEvent(event);
        this.dragSelectionRect.update(currentPoint);
    }

    /**
     * Called when the the user completes the rectangle selectiong 
     *
     */
    onDragRectEnd() {
        //apply the selection to the chart and nodes
        this.chart.applySelectionRect(this.dragSelectionRect);
        //reset the rectangle to nothing
        this.dragSelectionRect = undefined;
    }
    
    
    
    
    ///NODE methods
    
    
    
    

    /**
     * translate the mouse event points to SVG coordinates
     * @param {MouseEvent} event
     * @return {any}
     */
    private svgCoordinatesForEvent(event:MouseEvent){
        this.ensureSvgElement();
        return FlowchartUtils.translateCoordinates(this.svgElement,event.pageX, event.pageY, event);
    }


    /**
     * Translate the mouse 'event' relative to the 'startEvent' as SVG coordinates
     * @param {MouseEvent} event
     * @param {MouseEvent} startEvent
     * @return {any}
     */
    private svgCoordinatesRelativeToStart(event:MouseEvent, startEvent:MouseEvent){
        this.ensureSvgElement();
        return FlowchartUtils.translateCoordinates(this.svgElement,event.pageX, event.pageY, startEvent);
    }

 


   private ensureSvgElement(){
        if(this.svgElement == undefined && this.element && this.element.nativeElement && this.element.nativeElement.children){
            this.svgElement = this.element.nativeElement.children[0]
        }
    }

    ngOnInit(){

    }


    //identifiers

    /**
     * get the identifier for the node
     * @param node
     * @param {number} index
     * @return {string}
     */
    nodeIdentifier(node:FlowChart.NodeViewModel, index:number) {
       return node.data.id;
    }

    /**
     * get the identifier for the connector
     * @param node
     * @param {number} nodeIndex
     * @param connector
     * @param {number} connectorIndex
     * @return {string}
     */
    connectorIdentifier(node:FlowChart.NodeViewModel, nodeIndex:number, connector:FlowChart.ConnectorViewModel, connectorIndex:number) {
        let id = node.data.id+'|'+connectorIndex;
        return id;
    }

    /**
     * Get the identifier for the connection
     * @param connection
     * @param {number} index
     */
    connectionIdentifier(connection:FlowChart.ConnectionViewModel,index:number){
      return connection.data.id
    }








    /**
     * Called when the Node starts dragging
     * @param {DragResponse} dragResponse
     */
    onNodeDragStarted(dragResponse:DragResponse){
        //allow node dragging only if we are not selecting or dragging a connection
        if(!this.dragState.isDraggingConnectionOrSelecting()) {
            //save the state of the starting coords 
            this.dragState.lastNodeMouseCoords = this.svgCoordinatesForEvent(dragResponse.dragStartMouseEvent);
            this.dragState.dragStartEvent = dragResponse.dragStartMouseEvent;
            
            let node = this.chart.findNode(dragResponse.draggableId)
            this.dragState.dragNode(node);
            
            // If nothing is selected when dragging starts,
            // at least select the node we are dragging.
            if (node && !node.selected()) {
                this.chart.deselectAll();
                node.select();
            }
        }
    }


    /**
     * called when the node is dragging
     * @param {MouseEvent} event
     */
    onNodeDragging(event:MouseEvent) {
        // Dragging selected nodes... update their x,y coordinates.
        //translate off the mouse down event
        let curCoords =  this.svgCoordinatesRelativeToStart(event,this.dragState.dragStartEvent);
        let deltaX = curCoords.x - this.dragState.lastNodeMouseCoords.x;
        var deltaY = curCoords.y - this.dragState.lastNodeMouseCoords.y;
        this.chart.updateSelectedNodesLocation(deltaX, deltaY);
        this.dragState.lastNodeMouseCoords = curCoords;
    }


    onNodeClicked(dragResponse:DragResponse) {
        let node = this.chart.findNode(dragResponse.draggableId)
        this.chart.handleNodeClicked(node, dragResponse.event.ctrlKey);        
    }





    // Connectors

    /**
     * get the node Id for a connector
     * @param {string} connectorIdentifier
     */
    private nodeIdentifierFromConnectorIdentifier(connectorIdentifier:string){
        let nodeId = connectorIdentifier.substring(0,connectorIdentifier.indexOf('|'))
        return nodeId;
    }

    /**
     * Get the connector index from the connector id
     * @param {string} connectorIdentifier
     * @return {string}
     */
    private connectorIndexFromConnectorIdentifier(connectorIdentifier:string){
        let index = connectorIdentifier.substring(connectorIdentifier.indexOf('|')+1)
        return index;
    }

    /**
     * Called when a user is dragging out a connector
     * @param {DragResponse} dragResponse
     */
    onConnectorDragStarted(dragResponse:DragResponse){
        let curCoords =  this.svgCoordinatesForEvent(dragResponse.dragStartMouseEvent);
        
        let connectorId = dragResponse.draggableId;

        //get the node
        let nodeId = this.nodeIdentifierFromConnectorIdentifier(connectorId);
        let node = this.chart.findNode(nodeId);
        let connectorIndex = this.connectorIndexFromConnectorIdentifier(connectorId);
        let connector = this.chart.findConnector(nodeId,connectorIndex);

        //update the state
        this.dragState.dragConnector(node,connector);
        this.updateDragPoints(curCoords, node, connector);
    }

    /**
     * Called when the connector is dragging around the canvas.
     * Update the line
     * @param {MouseEvent} event
     */
    onConnectorDragging(event:MouseEvent) {
        let curCoords =  this.svgCoordinatesForEvent(event);
        let connector = this.dragState.activeConnector;
        let node = this.dragState.activeNode;
        this.updateDragPoints(curCoords, node, connector);
    }

    /**
     * Called when the connection is done.
     * Attempt to make the connection to a node if possible
     * @param {DragResponse} dragResponse
     */
    onConnectorDragEnded(){
        if (this.mouseOverState.isType(MouseOverType.CONNECTOR) &&
            !this.mouseOverState.isMouseOverConnector(this.dragState.activeConnector)) {
            //
            // Dragging has ended...
            // The mouse is over a valid connector...
            // Create a new connection.
            //
            this.chart.createNewConnection( this.dragState.activeConnector, this.mouseOverState.mouseOverConnector);
        }
        else if(this.mouseOverState.isType(MouseOverType.NODE) && !this.mouseOverState.isMouseOverNode(this.dragState.activeNode)) {
            //find a connector and add it
            let connector = this.chart.findConnector(this.mouseOverState.mouseOverNode.data.id,0);
            if(connector){
                this.chart.createNewConnection( this.dragState.activeConnector, connector);
            }
        }
        this.clearDragPoints();      
    }

    private clearDragPoints(){
        this.dragPoint1 = null;
        this.dragPoint2 = null;
        this.dragTangent1 = null;
        this.dragTangent2 = null;
    }

    private updateDragPoints(point:Point, node:any, connector:any){
        this.dragPoint1 = FlowChart.FlowchartModelBase.computeConnectorPos(node, connector);
        this.dragPoint2 = {
            x: point.x,
            y: point.y
        };
        this.dragTangent1 = FlowChart.FlowchartModelBase.computeConnectionSourceTangent(this.dragPoint1, this.dragPoint2);
        this.dragTangent2 = FlowChart.FlowchartModelBase.computeConnectionDestTangent(this.dragPoint1, this.dragPoint2);
    }


//Style methods
    getAttrCheckboxCheckedClass(attr:any){
        return attr.selected ? 'accent-color show': 'hide';
    }

    getAttrCheckboxUnCheckedClass(attr:any){
        return attr.selected ? 'hide': ' accent-color show';
    }

    getAllCheckboxCheckedClass(nodeAttrs:any){
        return nodeAttrs.hasAllSelected() ? 'accent-color show': 'hide';
    }

    getAllCheckboxUnCheckedClass(nodeAttrs:any){
        return nodeAttrs.hasAllSelected() ? 'hide': 'accent-color show';
    }

    getNodeClass(node:any){
        return this.getNodeOrConnectionClass(node,this.mouseOverState.isMouseOverNode(node),'node-rect');
    }


    getConnectorClass(connector:any){
        return this.mouseOverState.isMouseOverConnector(connector) ? 'mouseover-connector-circle' : 'connector-circle';
    }


    connectionLineClass(connection:any){
        return this.getNodeOrConnectionClass(connection, this.mouseOverState.isMouseOverConnection(connection),'connection-line');
    }

    connectionNameClass(connection:any){
        return this.getNodeOrConnectionClass(connection,this.mouseOverState.isMouseOverConnection(connection),'connection-name');
    }
    connectionEndpointClass(connection:any){
        return this.getNodeOrConnectionClass(connection,this.mouseOverState.isMouseOverConnection(connection),'connection-endpoint')
    }

    private getNodeOrConnectionClass(item:any,mouseOver:boolean,suffix:string){
        if(item.selected()) {
            return 'selected-'+suffix;
        }
        else if(mouseOver) {
            return 'mouseover-'+suffix;
        }
        else {
            return suffix;
        }
    }

    // Handle mousedown on a connection.
    onConnectionClicked(dragResponse:DragResponse) {
        let connection = this.chart.findConnection(dragResponse.draggableId)
        if(connection) {
            this.chart.handleConnectionMouseDown(connection, dragResponse.event.ctrlKey);
        }
        // Don't let the chart handle the mouse down.
        dragResponse.event.stopPropagation();
        dragResponse.event.preventDefault();
    }


}