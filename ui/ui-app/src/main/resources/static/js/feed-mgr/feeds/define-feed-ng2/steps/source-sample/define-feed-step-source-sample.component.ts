import {Component, Input, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {DataSource} from "../../../../catalog/api/models/datasource";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";
import {StateRegistry, StateService} from "@uirouter/angular";
import {FormBuilder,FormGroup} from "@angular/forms";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {SelectionService, SingleSelectionPolicy} from "../../../../catalog/api/services/selection.service";
import * as angular from 'angular';
import {SparkDataSet} from "../../../../model/spark-data-set.model";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {PreviewDataSet} from "../../../../catalog/datasource/preview-schema/model/preview-data-set";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FEED_DEFINITION_SECTION_STATE_NAME} from "../../../../model/feed/feed-constants";

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

    @Input()
    public stateParams : any;

    public paths:string[] = [];


    feedDefintionDatasourceState:string = FEED_DEFINITION_SECTION_STATE_NAME+".datasource"

    /**
     * Flag that is toggled when a user is looking at a feed with a source already defined and they choose to browse the catalog to change the source
     * this will render the catalog selection/browse dialog
     */
    public showCatalog:boolean = false;


    constructor(defineFeedService:DefineFeedService,stateService: StateService, private selectionService: SelectionService,
                dialogService: TdDialogService,
                feedLoadingService:FeedLoadingService,) {
        super(defineFeedService,stateService, feedLoadingService,dialogService);
        this.sourceSample = new FormGroup({})
       this.defineFeedService.ensureSparkShell();

    }

    getStepName(){
        return FeedStepConstants.STEP_SOURCE_SAMPLE;
    }

    init(){
        this.paths = this.feed.getSourcePaths();
        //always show the catalog if no paths are available to preview
        if(this.paths == undefined || this.paths.length ==0) {
            this.showCatalog = true;
        }


    }

    browseCatalog(){
        if(this.feed.sourceDataSets && this.feed.sourceDataSets.length >0){
            this.dialogService.openConfirm({
                message: 'You already have a dataset defined for this feed. Switching the source will result in a new target schema. Are you sure you want to browse for a new dataset? ',
                disableClose: true,
                title: 'Source dataset already defined', //OPTIONAL, hides if not provided
                cancelButton: 'Cancel', //OPTIONAL, defaults to 'CANCEL'
                acceptButton: 'Accept', //OPTIONAL, defaults to 'ACCEPT'
                width: '500px', //OPTIONAL, defaults to 400px
            }).afterClosed().subscribe((accept: boolean) => {
                if (accept) {
                    this.showCatalog = true;
                } else {
                    // no op
                }
            });
        }
    }

    goToDataSet(dataSet:SparkDataSet){
        let params = angular.extend({},this.stateParams);
        params["dataSource"]=dataSet.dataSource;
        params["resetSelectionService"] = false;
        params["datasourceId"]= dataSet.dataSource.id;
        params["path"]= dataSet.resolvePath(false);
        this.selectionService.reset(dataSet.dataSource.id);
        this.selectionService.setLastPath(dataSet.dataSource.id,{path:dataSet.resolvePath()});
       // this.selectionService.set()
        this.stateService.go(FEED_DEFINITION_SECTION_STATE_NAME+".datasource",params)
    }



}