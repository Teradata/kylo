import {Component, ElementRef, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from "@angular/core";
import {Network, DataSet, Node, Edge, IdType} from 'vis';
import * as _ from "underscore";
import {CloneUtil} from "../utils/clone-util";
// declare var vis: any;

@Component({
    selector: "kylo-vis-network",
    templateUrl: "./kylo-vis-network.component.html"
})
export class KyloVisNetworkComponent implements OnInit, OnChanges {

    network: Network = null;

    @Input()
    data: any;
    @Input()
    options: any;
    @Input()
    widthPx: string = '';
    @Input()
    heightPx: string = '550px';

    @Output()
    onSelect = new EventEmitter<any>();

    @Output()
    stabilized = new EventEmitter<any>();

    constructor(private elementRef: ElementRef) {
    }

    ngOnInit() {
        this.drawNetwork();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes && this.network !== undefined) {
            if(changes['data'] && changes['data'].previousValue) {
                this.network.setData(changes['data'].currentValue);
            }
            if(changes['options'] && changes['options'].previousValue) {
                this.network.setOptions(changes['options'].currentValue);
            }
        }
    }

    drawNetwork() {
        let container = this.elementRef.nativeElement;
        if (this.network != null) {
            this.network.destroy();
        }
        this.network = new Network(container, this.data, this.options);
        // create a network
        this.network.setSize(this.widthPx, this.heightPx);
        this.network.redraw();
        this.network.fit();
        this.network.on('click', (param:any) => {
            this.onSelect.emit(param);
        });
        this.network.on('stabilized', (param:any) => {
            this.stabilized.emit(param);
        });
    }

    setHeight(height:number){
        this.network.setSize(this.widthPx, height+"px");
    }

    updateOptions(options:any){
        let copy = CloneUtil.deepCopy(this.options);
        copy = _.extend(copy,options);
        this.network.setOptions(copy);
    }

}

