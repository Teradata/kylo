import { Component, Input, ViewEncapsulation } from "@angular/core";


@Component({
    selector: 'vs-cell-menu',
    templateUrl: 'js/feed-mgr/visual-query/transform-data/visual-query-table/cell-menu.template.html',
    styleUrls: ['js/feed-mgr/visual-query/transform-data/visual-query-table/cell-menu.template.css'],
    encapsulation: ViewEncapsulation.None
})
export class CellMenuComponent {

    @Input("selection") selection : any;
    @Input("value") value : any;
    @Input("header") header : any;
    
}