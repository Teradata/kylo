import { Component, Input, ViewEncapsulation } from "@angular/core";


@Component({
    selector: 'vs-table-header',
    templateUrl: './visual-query-table-header.html',
    styleUrls: ['./visual-query-table-header.scss'],
    encapsulation: ViewEncapsulation.None
})
export class VisualQueryTableHeader {

    @Input("header") header : any;
    @Input("table") table : any;
    @Input("availableCasts") availableCasts : any;
    @Input("availableDomainTypes") availableDomainTypes : any;
    @Input("domainType") domainType : any;
    
}