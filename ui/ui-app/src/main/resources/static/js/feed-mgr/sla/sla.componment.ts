import {Component, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";

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

