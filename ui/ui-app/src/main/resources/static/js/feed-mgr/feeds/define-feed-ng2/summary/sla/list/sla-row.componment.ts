import {Component, Input} from "@angular/core";
import {Sla} from '../sla.componment';

@Component({
    selector: "sla-row",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/sla/list/sla-row.component.html"
})
export class SlaRowComponent {

    @Input('sla') sla: Sla;

    constructor(){
        //todo use dynamic-form builder?
    }
}

