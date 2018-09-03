import * as angular from "angular";
import {Component, Injector, Input, OnInit, OnDestroy, ViewContainerRef, ViewChild, ContentChild, TemplateRef} from "@angular/core";
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
import {DatasetChangeEvent, PreviewDatasetCollectionService} from "../../../../catalog/api/services/preview-dataset-collection.service";
import {Node} from "../../../../catalog/api/models/node";
import {FileMetadataTransformService} from "../../../../catalog/datasource/preview-schema/service/file-metadata-transform.service";
import {StepperSelectionEvent} from "@angular/cdk/stepper";
import {FEED_DEFINITION_SECTION_STATE_NAME} from "../../../../model/feed/feed-constants";
import {BrowserObject} from "../../../../catalog/api/models/browser-object";
import {BrowserComponent} from "../../../../catalog/datasource/api/browser.component";
import {FileMetadataTransformResponse} from "../../../../catalog/datasource/preview-schema/model/file-metadata-transform-response";
import {PreviewSchemaService} from "../../../../catalog/datasource/preview-schema/service/preview-schema.service";
import {DatasetCollectionPreviewDialogComponent, DatasetCollectionPreviewDialogData, DataSetPreviewMode} from "./dataset-collection-preview-dialog.component";
import {PreviewDataSetRequest} from "../../../../catalog/datasource/preview-schema/model/preview-data-set-request";
import {DatasetCollectionPreviewCartComponent} from "./dataset-collection-preview-cart.component";
import {MatDialogConfig} from "@angular/material";
import {FeedSideNavService} from "../../shared/feed-side-nav.service";

export enum DataSetMode {
    COLLECT="COLLECT", PREVIEW_AND_COLLECT="PREVIEW_AND_COLLECT"
}

@Component({
    selector: "define-feed-source-sample-catalog-dataset",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/source-sample/define-feed-step-source-sample-datasource.component.html"
})
export class DefineFeedStepSourceSampleDatasourceComponent  extends DatasourceComponent implements OnInit, OnDestroy {

    private mode:DataSetMode = DataSetMode.COLLECT

    /**
     * Data set to be configured
     */
    @Input()
    public datasource: DataSource;

    @Input("connectorPlugin")
    public plugin: ConnectorPlugin;

    @Input()
    public params:any = {};

    //@ViewChild("datasetCollectionCart")
   // datasetCollectionCart:DatasetCollectionPreviewCartComponent;

    @ViewChild("toolbarActionTemplate")
    public toolbarActionTemplate: TemplateRef<any>


    /**
     * an array of paths used for the preview tab
     */
    public paths:string[];

    public feed: Feed;

    public step :Step;

    /**
     * flag to indicate only single selection is supported
     */
    singleSelection:boolean;

    constructor(state: StateService, stateRegistry: StateRegistry, selectionService: SelectionService,previewDatasetCollectionService: PreviewDatasetCollectionService,private  defineFeedService:DefineFeedService,
        private _dialogService: TdDialogService,
                private _fileMetadataTransformService: FileMetadataTransformService,
                private previewSchemaService:PreviewSchemaService,
                private feedSideNavService:FeedSideNavService
                ) {
       super(state,stateRegistry,selectionService,previewDatasetCollectionService);
      this.singleSelection = this.selectionService.isSingleSelection();
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

        //Register the action template
        this.feedSideNavService.registerToolbarActionTemplate(this.step.name,this.toolbarActionTemplate)

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
/******
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

    onDataSetCollectionChanged(event:DatasetChangeEvent){
       super.onDataSetCollectionChanged(event);
      // this.step.validate(this.feed);
      // this.step.updateStepState();
    }
****/

    public backToCatalog(){
        this.state.go(FEED_DEFINITION_SECTION_STATE_NAME+".datasources",{feedId:this.feed.id,jumpToSource:false})
    }

    /**
     *
     * @param {BrowserObject} file
     */
    preview(file:BrowserObject){
        this._previewDataSet(file, DataSetPreviewMode.PREVIEW);
    }

    getCartDialogConfig():MatDialogConfig{
        let config = DatasetCollectionPreviewCartComponent.DIALOG_CONFIG();
        //  let cartPosition = this.datasetCollectionCart.applyCartPositionSettings(config.position);
        return config;

    }

    onSave(){

        let datasets = this.previewDatasetCollectionService.datasets;
        //determine if the datasets differ from those on the feed
        let feedDatasets = this.feed.sourceDataSets;
        if(feedDatasets){
            let feedDatasetKeys  = feedDatasets.map(ds => ds.id).sort().toString();
            let newDatasetKeys = datasets.map(ds => ds.key).sort().toString();
            if(feedDatasetKeys != newDatasetKeys){
                //WARN different datasets
                this._dialogService.openConfirm({
                    message: 'The dataset you have selected differs from the one existing on this feed. Switching the source will result in a new target schema.  Are you sure you want to do this?',
                    disableClose: true,
                    title: 'Confirm source dataset change', //OPTIONAL, hides if not provided
                    cancelButton: 'Cancel', //OPTIONAL, defaults to 'CANCEL'
                    acceptButton: 'Accept', //OPTIONAL, defaults to 'ACCEPT'
                    width: '500px', //OPTIONAL, defaults to 400px
                }).afterClosed().subscribe((accept: boolean) => {
                    if (accept) {
                        if(this.singleSelection) {
                            this.feed.setSourceDataSetAndUpdateTarget(datasets.map(ds => ds.toSparkDataSet())[0])
                        }
                        else {
                            //wrangler feed
                            this.feed.sourceDataSets = datasets.map(ds => ds.toSparkDataSet());
                            //TODO reset feed.tableSchema

                        }
                        this.defineFeedService.saveFeed(this.feed)
                    } else {
                        // no op
                    }
                });


            }

        }
        else {
            if(this.singleSelection) {
                this.feed.setSourceDataSetAndUpdateTarget(datasets.map(ds => ds.toSparkDataSet())[0])
            }
            else {
                //wrangler feed
                this.feed.sourceDataSets = datasets.map(ds => ds.toSparkDataSet());
                //TODO reset feed.tableSchema

            }
            //modify the source and target schemas
            this.defineFeedService.saveFeed(this.feed)
        }
    }

    onCancel(){

    }

  private  _previewDataSet(file:BrowserObject,mode:DataSetPreviewMode){
        let collect:boolean = DataSetPreviewMode.CART == mode ? true : false;
        let dialogConfig:MatDialogConfig = DataSetPreviewMode.CART == mode ? this.getCartDialogConfig(): DatasetCollectionPreviewDialogComponent.DIALOG_CONFIG()
        this._fileMetadataTransformService.detectFormatForPaths([file.getPath()],this.datasource).subscribe((response:FileMetadataTransformResponse) => {
            let obj = response.results.datasets;
            if(obj && Object.keys(obj).length >0){
                let dataSet = obj[Object.keys(obj)[0]];
                //auto preview and add
                let previewRequest = new PreviewDataSetRequest();
                previewRequest.dataSource = this.datasource;
                this.previewSchemaService.preview(dataSet,previewRequest,collect);
                //open side dialog
                let dialogData:DatasetCollectionPreviewDialogData = new DatasetCollectionPreviewDialogData(mode,dataSet)
                if(mode == DataSetPreviewMode.CART){
                    //save the cart position back to the data for reference
                    dialogData.dialogPosition = dialogConfig.position;
                }
                dialogConfig.data = dialogData;
                this._dialogService.open(DatasetCollectionPreviewDialogComponent,dialogConfig);
            }
        } )
    }

    /**
     *
     * @param {BrowserObject} file
     * @param {BrowserComponent} parent
     */
    addDataSet(event:MouseEvent,file:BrowserObject, parent:BrowserComponent){
        parent.onToggleChild({checked:true}, file)
        if(this.singleSelection) {
            //remove everything else and then add this one
            this.previewDatasetCollectionService.reset();
        }
        this._previewDataSet(file, DataSetPreviewMode.CART);
    }

    removeDataSet(event:MouseEvent,file:BrowserObject, parent:BrowserComponent){
        parent.onToggleChild({checked:false}, file)
        let path = file.getPath();
        let existingDataSets = this.previewDatasetCollectionService.findByPath(path);
        if(existingDataSets){
            existingDataSets.forEach((ds) => {
                this.previewDatasetCollectionService.remove(ds);
            });
        }

        //invalidate if the datasets are empty
    }

}

