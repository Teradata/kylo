import {Component, Input, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {DataSource} from "../../../../catalog/api/models/datasource";
import {FeedModel, Step} from "../../model/feed.model";
import {StateRegistry, StateService} from "@uirouter/angular";
import {FormBuilder,FormGroup} from "@angular/forms";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";

@Component({
    selector: "define-feed-step-source-sample",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/source-sample/define-feed-step-source-sample.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/source-sample/define-feed-step-source-sample.component.html"
})
export class DefineFeedStepSourceSampleComponent extends AbstractFeedStepComponent {

    static LOADER = "DefineFeedStepSourceSampleComponent.LOADER";

    @Input("datasources")
    public datasources: DataSource[];

    sourceSample: FormGroup;

    public stateParams : any;


    constructor(defineFeedService:DefineFeedService,stateService: StateService) {
        super(defineFeedService,stateService);
        this.sourceSample = new FormGroup({})

    }

    getStepName(){
        return "Source Sample";
    }

    init(){
        this.stateParams = {feedId:this.feed.id}
    }





}