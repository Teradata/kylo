export enum MouseOverType {
    CONNECTOR=1, CONNECTION=2, NODE=3, NONE=4
}

export class MouseOverState {

    /**
     * the connector the mouse is over
     * @type {null}
     */
    mouseOverConnector:any = null;

    /**
     * The connection the mouse is over
     * @type {null}
     */
    mouseOverConnection:any =  null;

    /**
     * The node the mouse is over
     * @type {null}
     */
    mouseOverNode:any = null;

    mouseOverType:MouseOverType;

    constructor(){ }

    clear() {
        this.mouseOverConnection = null;
        this.mouseOverConnector = null;
        this.mouseOverNode = null;
        this.mouseOverType = MouseOverType.NONE;
    }

    isMouseOverNode(node:any){
        return node == this.mouseOverNode;
    }

    isMouseOverConnection(connection:any){
        return  connection == this.mouseOverConnection;
    }
    isMouseOverConnector(connector:any){
        return connector == this.mouseOverConnector;
    }

    setMouseOverConnector(connector:any) : boolean{
        this.mouseOverConnector = connector;
        if(connector != null) {
            this.mouseOverType = MouseOverType.CONNECTOR;
        }
        return connector != null;
    }

    setMouseOverConnection(connection:any) : boolean{
        this.mouseOverConnection = connection;
        if(connection != null) {
            this.mouseOverType = MouseOverType.CONNECTION;
        }
        return connection != null;
    }

    setMouseOverNode(node:any) : boolean{
        this.mouseOverNode = node;
        if(node != null) {
            this.mouseOverType = MouseOverType.NODE;
        }
        return node != null;
    }

    isType(mouseOverType:MouseOverType){
        return this.mouseOverType == mouseOverType;
    }

}