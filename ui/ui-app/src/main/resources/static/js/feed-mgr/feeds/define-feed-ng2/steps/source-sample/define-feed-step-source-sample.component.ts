import {Component, Input, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {DataSource} from "../../../../catalog/api/models/datasource";
import {FeedModel, Step} from "../../model/feed.model";
import {StateRegistry, StateService} from "@uirouter/angular";
import {FormBuilder,FormGroup} from "@angular/forms";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {SparkDataSet} from "../../../../catalog/datasource/preview-schema/model/preview-data-set";
import {SelectionService} from "../../../../catalog/api/services/selection.service";
import * as angular from 'angular';

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


    constructor(defineFeedService:DefineFeedService,stateService: StateService, private selectionService: SelectionService, ) {
        super(defineFeedService,stateService);
        this.sourceSample = new FormGroup({})

    }

    getStepName(){
        return "Source Sample";
    }

    init(){
        this.stateParams = {feedId:this.feed.id}
        console.log("LOAD DATASET for ",this.feed.sourceDataSets)
    }

    goToDataSet(dataSet:SparkDataSet){
        let params = angular.extend({},this.stateParams);
        params["dataSource"]=dataSet.dataSource;
        params["resetSelectionService"] = false;
        params["datasourceId"]= dataSet.dataSource.id;
        params["path"]= dataSet.resolvePath(false);
        console.log("go with params ",params)
        this.selectionService.reset(dataSet.dataSource.id);
        this.selectionService.setLastPath(dataSet.dataSource.id,{path:dataSet.resolvePath()});
       // this.selectionService.set()
        this.stateService.go('feed-definition.feed-step.datasource',params)
    }



}