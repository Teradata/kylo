import { Component, ViewContainerRef, SkipSelf } from "@angular/core";


@Component({
    selector: 'vs-table-header',
    templateUrl: 'js/feed-mgr/visual-query/transform-data/visual-query-table/visual-query-table-header.html'
})
export class VisualQueryTableHeader {

    // @Input("header") header : WranglerColumn;
    constructor(@SkipSelf() viewContainerRef: ViewContainerRef) {
        
    }
}