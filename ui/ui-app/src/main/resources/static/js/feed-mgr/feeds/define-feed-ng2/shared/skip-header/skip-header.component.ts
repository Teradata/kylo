import {Component, Input, OnInit} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {Feed} from "../../../../model/feed/feed.model";

@Component({
    selector: "skip-header",
    styleUrls: ["./skip-header.component.css"],
    templateUrl: "./skip-header.component.html"
})

export class SkipHeaderComponent implements OnInit {

    @Input()
    formGroup: FormGroup;

    @Input()
    feed: Feed;

    @Input()
    readonly: boolean;

    @Input()
    title: string;

    @Input()
    subtitle?: string;

    ngOnInit(): void {
        if (this.formGroup == undefined) {
            this.formGroup = new FormGroup({});
        }
    }

    /**
     * Any additional actions on checking/unchecking this option can go here.
     * @param event
     */
    skipHeaderOptionChanged(event: any) {
        //console.log("Changed skip header option to: " + event.checked);
    }
}