import {Component} from "@angular/core";
import {ProcessorRef} from "@kylo/feed";

@Component({
    template: `<thinkbig-get-table-data-properties [processor]="processor" renderLoadStrategyOptions="true"></thinkbig-get-table-data-properties>`
})
export class GetTableDataComponent {

    constructor(readonly processor: ProcessorRef) {
    }
}
