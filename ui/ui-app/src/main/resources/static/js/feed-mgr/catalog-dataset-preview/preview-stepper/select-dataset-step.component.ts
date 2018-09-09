import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {StateRegistry, StateService} from "@uirouter/angular";
import {MatCheckboxChange} from "@angular/material/checkbox";
import {MatDialogConfig} from "@angular/material/dialog"
import {TdDialogService} from "@covalent/core/dialogs";
import {DatasourceComponent} from "../../catalog/datasource/datasource.component";
import {SelectionService} from "../../catalog/api/services/selection.service";
import {PreviewDatasetCollectionService} from "../../catalog/api/services/preview-dataset-collection.service";
import {FileMetadataTransformService} from "../../catalog/datasource/preview-schema/service/file-metadata-transform.service";
import {PreviewSchemaService} from "../../catalog/datasource/preview-schema/service/preview-schema.service";
import {CatalogService} from "../../catalog/api/services/catalog.service";
import {BrowserComponent} from "../../catalog/datasource/api/browser.component";
import {BrowserObject} from "../../catalog/api/models/browser-object";
import {DatasetPreviewDialogComponent, DatasetPreviewDialogData} from "./preview-dialog/dataset-preview-dialog.component";
import {PreviewDataSetRequest} from "../../catalog/datasource/preview-schema/model/preview-data-set-request";
import {FileMetadataTransformResponse} from "../../catalog/datasource/preview-schema/model/file-metadata-transform-response";
import {Node} from "../../catalog/api/models/node";
import {DataSource} from "../../catalog/api/models/datasource";
import {Observable} from "rxjs/Observable";
import {DatasetPreviewStepperService, DataSourceChangedEvent} from "./dataset-preview-stepper.service";
import {ISubscription} from "rxjs/Subscription";

export enum DataSetMode {
    COLLECT="COLLECT", PREVIEW_AND_COLLECT="PREVIEW_AND_COLLECT"
}

@Component({
    selector: "select-dataset-step",
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/select-dataset-step.component.html",
    changeDetection:ChangeDetectionStrategy.OnPush
})
export class SelectDatasetStepComponent  extends DatasourceComponent implements OnInit, OnDestroy {

    @Input()
    formGroup:FormGroup;


    @Input()
    public params:any = {};


    public showPreview:boolean;

    /**
     * flag to indicate only single selection is supported
     */
    singleSelection:boolean;

    /**
     * set by the datasource plugin to determine the right browser to use
     */
    datasourceSref:string;

    dataSourceChangedSubscription:ISubscription;

    constructor(state: StateService, stateRegistry: StateRegistry, selectionService: SelectionService,previewDatasetCollectionService: PreviewDatasetCollectionService,
        private _dialogService: TdDialogService,
                private _fileMetadataTransformService: FileMetadataTransformService,
                private previewSchemaService:PreviewSchemaService,
                private catalogService:CatalogService,
                private dataSourceService:DatasetPreviewStepperService,
                private cd:ChangeDetectorRef
                ) {
       super(state,stateRegistry,selectionService,previewDatasetCollectionService);
      this.singleSelection = this.selectionService.isSingleSelection();
     }


    ngOnInit(){
        if(this.formGroup == undefined){
            this.formGroup = new FormGroup({});
        }
        this.formGroup.addControl("hiddenValidFormCheck",new FormControl())
        if(this.datasource) {
            this.initDataSource();

        }
        else {
        this.dataSourceChangedSubscription =  this.dataSourceService.subscribeToDataSourceChanges(this.onDataSourceChanged.bind(this));
        }
/*
        if (this.plugin && this.plugin.tabs) {
            this.tabs = angular.copy(this.plugin.tabs);
        }
        // Add system tabs
        this.tabs.push({label: "Preview", sref: ".preview"});
*/
    }

    private onDataSourceChanged($event:DataSourceChangedEvent){
        this.datasource = $event.dataSource;
        this.params = $event.params
        this.initDataSource();


        console.log("DS Changed!!!")
    }

    private initDataSource(){
        this.catalogService.getDataSourceConnectorPlugin(this.datasource.id).subscribe(plugin => {
            //???
            this.plugin = plugin;
            if (this.plugin.tabs) {
                //find the first tab get the sref and determine the component to use for selecting
                this.datasourceSref = this.plugin.tabs[0].sref
            }

            console.log("mark for check!!")
            this.cd.markForCheck()
        })
        this.cd.markForCheck()
    }

    ngOnDestroy(){
        super.ngOnDestroy();
        if(this.dataSourceChangedSubscription){
            this.dataSourceChangedSubscription.unsubscribe();
        }
    }

    /**
     *
     * @param {BrowserObject} file
     */
    preview(file:BrowserObject){
        this._previewDataSet(file);
    }

    onToggleChange($event:MatCheckboxChange,file:BrowserObject,parent:BrowserComponent){
        parent.onToggleChild($event,file);
        let valid = false;
        let node:Node = <Node> this.selectionService.get(this.datasource.id);
        if(node){
           let selectionCount = node.countSelectedDescendants()
           valid = this.singleSelection ? selectionCount == 1 : selectionCount > 0;
        }
        if(valid) {
            this.formGroup.get("hiddenValidFormCheck").setValue("valid");
        }
        else {
            this.formGroup.get("hiddenValidFormCheck").setValue("");
        }
    }

    private  _previewDataSet(file:BrowserObject){
        let dialogConfig:MatDialogConfig = DatasetPreviewDialogComponent.DIALOG_CONFIG()
        this._fileMetadataTransformService.detectFormatForPaths([file.getPath()],this.datasource).subscribe((response:FileMetadataTransformResponse) => {
            let obj = response.results.datasets;
            if(obj && Object.keys(obj).length >0){
                let dataSet = obj[Object.keys(obj)[0]];
                let previewRequest = new PreviewDataSetRequest();
                previewRequest.dataSource = this.datasource;
                this.previewSchemaService.preview(dataSet,previewRequest,false);
                //open side dialog
                let dialogData:DatasetPreviewDialogData = new DatasetPreviewDialogData(dataSet)
                dialogConfig.data = dialogData;
                this._dialogService.open(DatasetPreviewDialogComponent,dialogConfig);
            }
        } )
    }


}

