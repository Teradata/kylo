import {Component, EventEmitter, Input, Output, OnInit} from "@angular/core";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";
import {DefineFeedService} from "../../services/define-feed.service";
import {FormsModule, FormGroup} from '@angular/forms'
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";
import {FeedSideNavService} from "../../services/feed-side-nav.service";

@Component({
    selector: "define-feed-step-card",
    styleUrls: ["./define-feed-step-card.component.scss"],
    templateUrl: "./define-feed-step-card.component.html"
})
export class DefineFeedStepCardComponent implements OnInit {

    @Input()
    public feed: Feed;

    @Input()
    public step : Step;

    @Input()
    public displayToolbar?:boolean = true;

    @Input()
    public displayEditActions?:boolean = true;

    @Input()
    public singleCardView:boolean;

    public disabledDependsUponStep:Step = null;


    @Input()
    public mode ?:string;

    @Output()
    saved:EventEmitter<any> = new EventEmitter<any>();

    @Output()
    cancelEdit:EventEmitter<any> = new EventEmitter<any>();

    constructor(private defineFeedService:DefineFeedService, private feedSideNavService:FeedSideNavService) {

    }

    ngOnInit() {
        if(this.mode == undefined){
            this.mode = 'normal'
        }
        //only make it dependent upon another step if it hasnt been deployed yet
        if(this.feed && !this.feed.isDeployed()) {
            this.disabledDependsUponStep = this.step.findFirstIncompleteDependentStep();
        }
    }

    onSave(){
        this.step.dirty=false;
        this.saved.emit();
    }

    onCancel(){
        this.cancelEdit.emit();
    }

    onEdit(){
        this.feed.readonly = false;
        this.defineFeedService.markFeedAsEditable();

    }

    goToDependsUponStep(){
        if(this.disabledDependsUponStep != null){
            this.feedSideNavService.gotoStep(this.disabledDependsUponStep,this.feed.id)
        }
    }


}