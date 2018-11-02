import {Component, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
export class Sla {
    id: string;
    name: string;
    description: string;
    feedNames: string;
    rules: Array<any> = [];
    canEdit: boolean = false;
    actionConfigurations: Array<any> = [];
    actionErrors: Array<any> = [];
    editable: boolean = false;
}


@Component({
    selector: "sla",
    styleUrls: ["./sla.component.scss"],
    templateUrl: "./sla.component.html"
})
export class SlaComponent implements OnInit {

    feed: any = undefined;

    ngOnInit(): void {
    }


    constructor() {

    }

    init() {

    }
}

