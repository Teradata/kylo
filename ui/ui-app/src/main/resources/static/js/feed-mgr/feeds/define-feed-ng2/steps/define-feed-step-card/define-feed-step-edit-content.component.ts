import {Component, Input, OnInit} from "@angular/core";
import {DefaultFeedModel, FeedModel, Step} from "../../model/feed.model";
import {FormGroup} from "@angular/forms";
import {DefineFeedService} from "../../services/define-feed.service";
import {FormsModule} from '@angular/forms'
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";

@Component({
    selector: "feed-step-edit-content",
    template:`<ng-content></ng-content>`
})
export class DefineFeedStepEditContentComponent  {

    @Input()
    public feed: FeedModel;

    @Input()
    public step : Step;


    constructor() {

    }


}