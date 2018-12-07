import { Component, Input, ViewEncapsulation, AfterViewInit, ViewChild, ElementRef } from "@angular/core";
import { MatMenuTrigger } from "@angular/material/menu";
import {BroadcastService} from "../../../../services/broadcast-service";

@Component({
    selector: 'vs-cell-menu',
    templateUrl: './cell-menu.template.html',
    styleUrls: ['./cell-menu.template.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class CellMenuComponent implements AfterViewInit {

    @Input("selection") selection : any;
    @Input("value") value : any;
    @Input("header") header : any;

    @Input("DataCategory") DataCategory : any;
    @Input("selectionDisplay") selectionDisplay : any;
    @Input("range") range : any;

    @Input("table") table : any;
    @Input("displayValue") displayValue : any;

    @Input() menuTop: any = 0;
    @Input() menuLeft: any = 0;
    
    @ViewChild(MatMenuTrigger) tableMenu: MatMenuTrigger;
    @ViewChild('cellButton') cellButton: ElementRef;

    ngAfterViewInit() {
        this.cellButton.nativeElement.click();
        this.tableMenu.onMenuClose.subscribe(()=>{
            this.broadcastService.notify("CLOSE_CELL_MENU");
        });
    }

    constructor(private broadcastService: BroadcastService) {

    }

}