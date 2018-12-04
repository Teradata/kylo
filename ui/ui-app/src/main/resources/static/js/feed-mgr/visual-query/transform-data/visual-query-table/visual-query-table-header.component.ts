import { Component, Input } from "@angular/core";


@Component({
    selector: 'vs-table-header',
    templateUrl: 'js/feed-mgr/visual-query/transform-data/visual-query-table/visual-query-table-header.html'
})
export class VisualQueryTableHeader {

    @Input("header") header : any;
    @Input("table") table : any;
    @Input("availableCasts") availableCasts : any;
    @Input("availableDomainTypes") availableDomainTypes : any;
    @Input("domainType") domainType : any;
    constructor() {
        
    }
}