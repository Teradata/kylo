import {Component, Input, OnInit} from "@angular/core";
import {DefaultFeedModel, FeedModel, Step} from "../../model/feed.model";
import {FormGroup} from "@angular/forms";
import {DefineFeedService} from "../../services/define-feed.service";
import {FormsModule} from '@angular/forms'
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";

@Component({
    selector: "feed-step-readonly-content",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/define-feed-step-card/define-feed-step-readonly-content.component.css"],
    template:`<ng-content></ng-content>`
})
export class DefineFeedStepReadonlyContentComponent  {

    @Input()
    public feed: FeedModel;

    @Input()
    public step : Step;

    constructor() {

    }



}