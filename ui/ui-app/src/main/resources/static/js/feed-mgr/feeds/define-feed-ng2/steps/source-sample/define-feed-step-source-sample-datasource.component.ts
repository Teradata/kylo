import * as angular from "angular";
import {Component, Injector, Input,OnInit,OnDestroy} from "@angular/core";
import {DatasourceComponent} from "../../../../catalog/datasource/datasource.component";
import {DataSource} from "../../../../catalog/api/models/datasource";
import {FeedModel, Step} from "../../model/feed.model";
import {DefineFeedService} from "../../services/define-feed.service";
import {SelectionService} from "../../../../catalog/api/services/selection.service";
import {StateRegistry, StateService} from "@uirouter/angular";
import {ConnectorTab} from "../../../../catalog/api/models/connector-tab";
import {ISubscription} from "rxjs/Subscription";
import {FeedStepValidator} from "../../model/feed-step-validator";

@Component({
    selector: "define-feed-source-sample-catalog-dataset",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/source-sample/define-feed-step-source-sample-datasource.component.html"
})
export class DefineFeedStepSourceSampleDatasourceComponent  extends DatasourceComponent implements OnInit, OnDestroy {


    /**
     * Data set to be configured
     */
    @Input()
    public datasource: DataSource;

    public params:any = {};

    public feed: FeedModel;

    public step :Step;

    selectedTab:ConnectorTab;

    constructor(state: StateService, stateRegistry: StateRegistry, selectionService: SelectionService,  $$angularInjector: Injector,private  defineFeedService:DefineFeedService) {
       super(state,stateRegistry,selectionService,$$angularInjector);
    }

    onTabClicked(tab:ConnectorTab) {
        this.selectedTab = tab;
    }

    ngOnInit(){
        this.selectionService.reset(this.datasource.id);
        if (this.datasource.connector.tabs) {
            this.tabs = angular.copy(this.datasource.connector.tabs);
        }
        // Add system tabs
        this.tabs.push({label: "Preview", sref: ".preview"});
        this.feed =this.defineFeedService.getFeed();
        this.step = this.feed.steps.find(step => step.systemName == "Source Sample");
        this.step.visited = true;


        // Go to the first tab
        this.onTabClicked(this.tabs[0]);
    }
    ngOnDestroy(){
    }

}

