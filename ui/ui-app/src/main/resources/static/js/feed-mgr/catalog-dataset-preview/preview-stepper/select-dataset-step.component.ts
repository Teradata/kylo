import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {StateRegistry, StateService} from "@uirouter/angular";
import {MatCheckboxChange} from "@angular/material/checkbox";
import {MatDialogConfig} from "@angular/material/dialog"
import {TdDialogService} from "@covalent/core/dialogs";
import {DatasourceComponent} from "../../catalog/datasource/datasource.component";
import {SelectionService} from "../../catalog/api/services/selection.service";
import {PreviewDatasetCollectionService} from "../../catalog/api/services/preview-dataset-collection.service";
import {PreviewSchemaService} from "../../catalog/datasource/preview-schema/service/preview-schema.service";
import {CatalogService} from "../../catalog/api/services/catalog.service";
import {BrowserComponent} from "../../catalog/datasource/api/browser.component";
import {BrowserObject} from "../../catalog/api/models/browser-object";
import {PreviewDataSetRequest} from "../../catalog/datasource/preview-schema/model/preview-data-set-request";
import {FileMetadataTransformResponse} from "../../catalog/datasource/preview-schema/model/file-metadata-transform-response";
import {Node} from "../../catalog/api/models/node";
import {DataSource} from "../../catalog/api/models/datasource";
import {Observable} from "rxjs/Observable";
import {DatasetPreviewStepperService} from "./dataset-preview-stepper.service";
import { DataSourceChangedEvent, PreviewDataSetResultEvent} from "../../catalog/datasource/preview-schema/service/dataset-preview.service"
import {ISubscription} from "rxjs/Subscription";
import {BrowserService} from "../../catalog/datasource/api/browser.service";
import {UploadComponent, UploadFilesChangeEvent} from "../../catalog/datasource/upload/upload.component";
import {FileUpload} from "../../catalog/datasource/upload/models/file-upload";
import {RemoteFile} from "../../catalog/datasource/files/remote-file";
import {DatasetPreviewDialogComponent, DatasetPreviewDialogData} from "../../catalog/datasource/preview-schema/preview-dialog/dataset-preview-dialog.component";

export enum DataSetMode {
    COLLECT="COLLECT", PREVIEW_AND_COLLECT="PREVIEW_AND_COLLECT"
}

@Component({
    selector: "select-dataset-step",
    templateUrl: "./select-dataset-step.component.html",
    styleUrls:["./select-dataset-step.component.scss"],
    changeDetection:ChangeDetectionStrategy.OnPush
})
export class SelectDatasetStepComponent  extends DatasourceComponent implements OnInit, OnDestroy {

    @Input()
    formGroup:FormGroup;


    @Input()
    public params:any = {};

    @ViewChild("fileUpload")
    private fileUpload:UploadComponent
    /**
     * flag to indicate only single selection is supported
     */
    singleSelection:boolean;

    /**
     * set by the datasource plugin to determine the right browser to use
     */
    datasourceSref:string;

    dataSourceChangedSubscription:ISubscription;

    browserComponentUpdatedSubscription:ISubscription;

    constructor(state: StateService, stateRegistry: StateRegistry, selectionService: SelectionService,previewDatasetCollectionService: PreviewDatasetCollectionService,
        private _dialogService: TdDialogService,
                private catalogService:CatalogService,
                private _dataSetPreviewStepperService:DatasetPreviewStepperService,
                private browserService:BrowserService,
                private cd:ChangeDetectorRef
                ) {
       super(state,stateRegistry,selectionService,previewDatasetCollectionService);
      this.singleSelection = this.selectionService.isSingleSelection();
     this.browserComponentUpdatedSubscription = this.browserService.subscribeToDataFiltered(this.onBrowserComponentFiltered.bind(this))
     }

onBrowserComponentFiltered(files:BrowserObject[]){

    this.cd.markForCheck();
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
        this.dataSourceChangedSubscription =  this._dataSetPreviewStepperService.subscribeToDataSourceChanges(this.onDataSourceChanged.bind(this));
        }

    }

    /**
     * Called when the upload is ready
     * @param {UploadFilesChangeEvent} $event
     */
    public onUploadFilesChange($event:UploadFilesChangeEvent){
        if($event.isReady){
            //add the files to the selectionService
            let parent:Node = new Node("upload");
             $event.files.forEach((file:FileUpload) => {
                let node = new Node(file.name)
                let remoteFile = new RemoteFile(file.name,file.path,false,file.size,new Date())
                node.setBrowserObject(remoteFile);
                node.setSelected(true);
                parent.addChild(node);
            });
            this.selectionService.reset(this.datasource.id)
            this.selectionService.set(this.datasource.id,parent);
            this.formGroup.get("hiddenValidFormCheck").setValue("valid");
        }
        else {
            this.formGroup.get("hiddenValidFormCheck").setValue("");
        }
        this.cd.markForCheck();
    }

    /**
     * when a datasource changes, reinit the datasource
     * @param {DataSourceChangedEvent} $event
     */
    private onDataSourceChanged($event:DataSourceChangedEvent){
        this.datasource = $event.dataSource;
        this.params = $event.params
        this.initDataSource();



    }

    /**
     * initialize the selected datasource and get any plugin tabs associated with the source
     * When complete notify the view changed
     */
    private initDataSource(){
        this.datasourceSref = '';
        this.catalogService.getDataSourceConnectorPlugin(this.datasource.id).subscribe(plugin => {
            this.plugin = plugin;
            if (this.plugin.tabs) {
                //find the first tab get the sref and determine the component to use for selecting
                this.datasourceSref = this.plugin.tabs[0].sref
            }

            this.cd.markForCheck()
        })
        this.cd.markForCheck()
    }

    /**
     * destroy and unsubscribe the events
     */
    ngOnDestroy(){
        super.ngOnDestroy();
        if(this.dataSourceChangedSubscription){
            this.dataSourceChangedSubscription.unsubscribe();
        }
        if(this.browserComponentUpdatedSubscription){
            this.browserComponentUpdatedSubscription.unsubscribe();
        }
    }

    /**
     * inline preview the data
     * @param {BrowserObject} file
     */
    preview(file:BrowserObject){
        this._previewDataSet(file);
    }
    onSelectionChange() {
        this.cd.markForCheck()
    }

    onCheckboxChange() {
        let valid = false;
        let node:Node = <Node> this.selectionService.get(this.datasource.id);
        if(node){
            let selectionCount = node.countSelectedDescendants()
            valid = this.selectionService.isSingleSelection() ? selectionCount == 1 : selectionCount > 0;
        }
        if(valid) {
            this.formGroup.get("hiddenValidFormCheck").setValue("valid");
        }
        else {
            this.formGroup.get("hiddenValidFormCheck").setValue("");
        }
    }
/*
    onToggleChange($event:MatCheckboxChange,file:BrowserObject,parent:BrowserComponent){
        parent.onToggleChild($event,file);
        let valid = false;
        let node:Node = <Node> this.selectionService.get(this.datasource.id);
        if(node){
           let selectionCount = node.countSelectedDescendants()
           valid = this.selectionService.isSingleSelection() ? selectionCount == 1 : selectionCount > 0;
        }
        if(valid) {
            this.formGroup.get("hiddenValidFormCheck").setValue("valid");
        }
        else {
            this.formGroup.get("hiddenValidFormCheck").setValue("");
        }
    }
*/
    public _previewDataSet(file: BrowserObject) {
        this.showPreviewDialog(file,this.datasource);


    }


    private showPreviewDialog(file: BrowserObject,datasource:DataSource) {
        let dialogConfig: MatDialogConfig = DatasetPreviewDialogComponent.DIALOG_CONFIG()
        let dialogData: DatasetPreviewDialogData = new DatasetPreviewDialogData()
        dialogData.datasource = datasource;
        dialogData.file = file;
        dialogConfig.data = dialogData;

        this._dialogService.open(DatasetPreviewDialogComponent, dialogConfig);

    }



}

