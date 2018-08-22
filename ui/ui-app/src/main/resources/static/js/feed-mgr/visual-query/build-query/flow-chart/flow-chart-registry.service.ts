import {MouseOverState} from "./model/mouse-over-state";
import {Injectable} from "@angular/core";

export class FlowChartRegistryService {

    nodes: {[key: string]: any} = {}

    connectors: {[key: string]: any}  = {}

    connections: {[key: string]: any} = {}

    constructor(){}


    registerNode(id:string,node:any){
        this.nodes[id] = node;
    }

    getNode(id:string){
        return this.nodes[id];
    }

    registerConnector(id:string,connector:any){
        this.connectors[id] = connector;
    }

    getConnector(id:string){
        return this.connectors[id];
    }
    registerConnection(id:string,connection:any){
        this.connections[id] = connection;
    }

    getConnection(id:string){
        return this.connections[id];
    }

    deleteNode(id:string){
        delete this.nodes[id];
    }

    deleteConnection(id:string){
        delete this.connections[id];
    }

    deleteConnector(id:string){
        delete this.connectors[id];
    }
}