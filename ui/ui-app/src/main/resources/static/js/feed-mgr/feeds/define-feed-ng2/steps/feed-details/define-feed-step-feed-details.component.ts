import {Component, Input, OnInit} from "@angular/core";
import {DefaultFeedModel, FeedModel, Step} from "../../model/feed.model";
import {FormGroup} from "@angular/forms";
import {DefineFeedService} from "../../services/define-feed.service";
import {FormsModule} from '@angular/forms'
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";

@Component({
    selector: "define-feed-step-feed-details",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/feed-details/define-feed-step-feed-details.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/feed-details/define-feed-step-feed-details.component.html"
})
export class DefineFeedStepFeedDetailsComponent extends AbstractFeedStepComponent {


    constructor(  defineFeedService:DefineFeedService,  stateService:StateService) {
        super(defineFeedService,stateService);
    }

    getStepName() {
        return "Feed Details";
    }

    init(){

    }


}