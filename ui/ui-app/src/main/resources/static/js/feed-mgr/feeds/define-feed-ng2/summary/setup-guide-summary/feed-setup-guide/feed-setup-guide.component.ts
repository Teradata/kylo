import {Step} from "../../../../../model/feed/feed-step.model";
import {Component, Input, OnDestroy, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {Feed} from "../../../../../model/feed/feed.model";

@Component({
    selector: "feed-setup-guide",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/setup-guide-summary/feed-setup-guide/feed-setup-guide.component.scss"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/setup-guide-summary/feed-setup-guide/feed-setup-guide.component.html"
})
export class FeedSetupGuideComponent implements OnInit, OnDestroy{


    @Input()
    feed:Feed;

    requiredSteps:Step[] = [];

    optionalSteps:Step[] = [];

    constructor(private  stateService:StateService) {

    }


    ngOnInit() {
        this.feed.steps.forEach((step:Step) => {
            if(step.required){
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