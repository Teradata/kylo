import {Step} from "../../../../../model/feed/feed-step.model";
import {Component, Input, OnDestroy, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {Feed} from "../../../../../model/feed/feed.model";

@Component({
    selector: "feed-setup-guide",
    styleUrls: ["./feed-setup-guide.component.scss"],
    templateUrl: "./feed-setup-guide.component.html"
})
export class FeedSetupGuideComponent implements OnInit, OnDestroy{


    @Input()
    feed:Feed;

    requiredSteps:Step[] = [];

    optionalSteps:Step[] = [];

    constructor(private  stateService:StateService) {

    }


    ngOnInit() {
        this.feed.steps.filter(step => !step.hidden).forEach((step:Step) => {
            if(step.isRequired(this.feed)){
                this.requiredSteps.push(step);
            }
            else {
                this.optionalSteps.push(step);
            }
        })
    }

    ngOnDestroy() {

    }

    onStepSelected(step: Step) {
        let params = {"feedId": this.feed.id};
        this.stateService.go(step.sref, params, {location: "replace"})
    }

}