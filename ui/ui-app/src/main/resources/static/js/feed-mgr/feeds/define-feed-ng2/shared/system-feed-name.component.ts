import {Component, Inject, Input, OnInit} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";

import {Feed} from "../../../model/feed/feed.model";
import {FeedService} from "../../../services/FeedService";

@Component({
    selector: "system-feed-name",
    templateUrl: "./system-feed-name.component.html"
})
export class SystemFeedNameComponent implements OnInit {

    @Input()
    formGroup: FormGroup;

    @Input()
    feed?: Feed;

    @Input()
    layout: string = "row";


    constructor(@Inject("FeedService") private feedService: FeedService) {
    }

    private generateSystemName(value: string) {
        this.feedService.getSystemName(value).then((response: any) => {
            this.formGroup.get("systemFeedName").setValue(response.data);
        });
    }


    ngOnInit() {
        let feedName = this.feed != undefined ? this.feed.feedName : '';
        let systemFeedName = this.feed != undefined ? this.feed.systemFeedName : '';

        let feedNameCtrl = new FormControl(feedName, [Validators.required]);
        feedNameCtrl.valueChanges.debounceTime(200).subscribe(value => {
            if (this.formGroup.get("systemFeedName").pristine) {
                this.generateSystemName(value);
            }
            else {
               // console.log('system name will not auto generate.  its been touched')
            }
        });
        this.formGroup.registerControl("feedName", feedNameCtrl);

        //TODO add in pattern validator, and unique systemFeedName validator
        const systemFeedNameControl = new FormControl(systemFeedName, [Validators.required]);
        if (this.feed && this.feed.hasBeenDeployed()) {
            systemFeedNameControl.disable();
        }
        this.formGroup.addControl("systemFeedName", systemFeedNameControl);
    }

    resetForm() {
        this.formGroup.reset({"feedName": this.feed ? this.feed.feedName : "", "systemFeedName": this.feed ? this.feed.systemFeedName : ""})
    }

    checkRequired(formGroup: FormGroup, controlName: string) {
        return formGroup.get(controlName).hasError('required');
    }
}
