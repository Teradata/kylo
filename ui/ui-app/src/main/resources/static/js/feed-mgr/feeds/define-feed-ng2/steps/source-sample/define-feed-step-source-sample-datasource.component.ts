import * as angular from "angular";
import {Component, Injector, Input,OnInit,OnDestroy} from "@angular/core";
import {DatasourceComponent} from "../../../../catalog/datasource/datasource.component";
import {ConnectorPlugin} from "../../../../catalog/api/models/connector-plugin";
import {DataSource} from "../../../../catalog/api/models/datasource";
import {Feed} from "../../../../model/feed/feed.model";
import {Step} from "../../../../model/feed/feed-step.model";
import {DefineFeedService} from "../../services/define-feed.service";
import {SelectionService, SelectionStrategy, SingleSelectionPolicy} from "../../../../catalog/api/services/selection.service";
import {StateRegistry, StateService} from "@uirouter/angular";
import {ConnectorTab} from "../../../../catalog/api/models/connector-tab";
import {ISubscription} from "rxjs/Subscription";
import {FeedStepValidator} from "../../../../model/feed/feed-step-validator";
import {PreviewDataSet} from "../../../../catalog/datasource/preview-schema/model/preview-data-set";
import {ObjectUtils} from "../../../../../common/utils/object-utils";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {TdDialogService} from "@covalent/core/dialogs";
import {PreviewDatasetCollectionService} from "../../../../catalog/api/services/preview-dataset-collection.service";
import {Node} from "../../../../catalog/api/models/node";
import {FileMetadataTransformService} from "../../../../catalog/datasource/preview-schema/service/file-metadata-transform.service";
import {StepperSelectionEvent} from "@angular/cdk/stepper";
import {FEED_DEFINITION_SECTION_STATE_NAME} from "../../../../model/feed/feed-constants";

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

    @Input("connectorPlugin")
    public plugin: ConnectorPlugin;

    @Input()
    public params:any = {};

    /**
     * an array of paths used for the preview tab
     */
    public paths:string[];

    public feed: Feed;

    public step :Step;

    selectedTab:ConnectorTab;


    private warnIfSourceChanges:boolean = false;


    constructor(state: StateService, stateRegistry: StateRegistry, selectionService: SelectionService,  $$angularInjector: Injector,private  defineFeedService:DefineFeedService,
        private _dialogService: TdDialogService,
                private _fileMetadataTransformService: FileMetadataTransformService) {
       super(state,stateRegistry,selectionService,$$angularInjector);
    }

    onStepSelectionChanged(event:StepperSelectionEvent) {
        let index = event.selectedIndex
        let tab = this.tabs[index];
        this.onTabClicked(tab)
    }

    onTabClicked(tab:ConnectorTab) {
        if(tab.sref == ".preview"){
            this.setPreviewPaths();
        }
        this.selectedTab = tab;
    }

    ngOnInit(){
        if (this.plugin && this.plugin.tabs) {
            this.tabs = angular.copy(this.plugin.tabs);
        }
        // Add system tabs
        this.tabs.push({label: "Preview", sref: ".preview"});
        this.feed =this.defineFeedService.getFeed();
        if(this.feed.isDataTransformation()){
            this.selectionService.multiSelectionStrategy();
        }
        else {
            this.selectionService.singleSelectionStrategy();
        }
        this.step = this.feed.steps.find(step => step.systemName == FeedStepConstants.STEP_SOURCE_SAMPLE);
        this.step.visited = true;

        let feedTargetStep = this.feed.getStepBySystemName(FeedStepConstants.STEP_FEED_TARGET);
        this.warnIfSourceChanges = feedTargetStep.visited && this.feed.table.tableSchema.fields.length >0;
        // Go to the first tab
        this.onTabClicked(this.tabs[0]);
    }
    ngOnDestroy(){
        super.ngOnDestroy();
    }

    setPreviewPaths(){
        let paths :string[] = [];
        let feedPaths = this.feed.getSourcePaths().concat(paths);
        //add in the node selection
        //attempt to get the paths from the selectionService and selected node
        let node: Node = <Node> this.selectionService.get(this.datasource.id);
            if(node) {
                paths = this._fileMetadataTransformService.getSelectedItems(node, this.datasource);

            }

        this.paths = paths.concat(feedPaths);
        }

        isSingleSelectionPolicy(){
        return this.selectionService.hasPolicy(SingleSelectionPolicy);
        }

    onDatasetAdd(dataset:PreviewDataSet){
        if(this.warnIfSourceChanges && this.previewDatasetCollectionService.datasetCount() >0 && !this.previewDatasetCollectionService.exists(dataset)){
            this._dialogService.openConfirm({
                message: 'You already have a dataset selected.  The new dataset you are trying to add is different.  Switching the source will result in a new target schema.  Are you sure you want to do this?',
                disableClose: true,
                title: 'Confirm source dataset change', //OPTIONAL, hides if not provided
                cancelButton: 'Cancel', //OPTIONAL, defaults to 'CANCEL'
                acceptButton: 'Accept', //OPTIONAL, defaults to 'ACCEPT'
                width: '500px', //OPTIONAL, defaults to 400px
            }).afterClosed().subscribe((accept: boolean) => {
                if (accept) {
                    //remove all existing datasets if we are using the singleselection
                    if(this.isSingleSelectionPolicy()){
                        this.previewDatasetCollectionService.reset();
                    }
                    this.previewDatasetCollectionService.addDataSet(dataset);
                } else {
                    // no op
                }
            });
        }
        else {
            if(this.isSingleSelectionPolicy()){
                this.previewDatasetCollectionService.reset();
            }
            this.previewDatasetCollectionService.addDataSet(dataset);
        }
    }


    onDatasetRemoval(dataset:PreviewDataSet){
        //warn if its the only dataset
        let count = this.previewDatasetCollectionService.datasetCount();
        if(count == 1) {
            this._dialogService.openConfirm({
                message: 'This is the only dataset sampled.  Are you sure you want to remove this?  Doing so will alter your target schema',
                disableClose: true,
                title: 'Confirm dataset removal', //OPTIONAL, hides if not provided
                cancelButton: 'Cancel', //OPTIONAL, defaults to 'CANCEL'
                acceptButton: 'Accept', //OPTIONAL, defaults to 'ACCEPT'
                width: '500px', //OPTIONAL, defaults to 400px
            }).afterClosed().subscribe((accept: boolean) => {
                if (accept) {
                    this.previewDatasetCollectionService.remove(dataset);
                } else {
                    // no op
                }
            });
        }
        else {
            this.previewDatasetCollectionService.remove(dataset);
        }
    }

    onDataSetCollectionChanged(dataSets:PreviewDataSet[]){
       super.onDataSetCollectionChanged(dataSets);
       this.step.validate(this.feed);
       this.step.updateStepState();
    }


    public backToCatalog(){
        this.state.go(FEED_DEFINITION_SECTION_STATE_NAME+".datasources",{feedId:this.feed.id,jumpToSource:false})
    }

}

