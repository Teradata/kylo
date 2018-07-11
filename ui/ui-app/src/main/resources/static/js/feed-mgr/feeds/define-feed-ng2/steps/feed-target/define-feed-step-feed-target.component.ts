import {Component, Input, OnInit} from "@angular/core";
import {DefaultFeedModel, FeedModel, Step} from "../../model/feed.model";
import {FormGroup} from "@angular/forms";
import {DefineFeedService} from "../../services/define-feed.service";
import {FormsModule} from '@angular/forms'
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";

@Component({
    selector: "define-feed-step-feed-target",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/feed-target/define-feed-step-feed-target.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/feed-target/define-feed-step-feed-target.component.html"
})
export class DefineFeedStepFeedTargetComponent extends AbstractFeedStepComponent {


    constructor(  defineFeedService:DefineFeedService,  stateService:StateService) {
        super(defineFeedService,stateService);
    }

    getStepName() {
        return "Feed Target";
    }

    init(){
     //custom init logic here
    }






}