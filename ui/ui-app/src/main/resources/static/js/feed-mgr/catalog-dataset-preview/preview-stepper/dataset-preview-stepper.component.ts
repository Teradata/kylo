import {Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {MatStepper} from "@angular/material/stepper";
import {CatalogService} from "../../catalog/api/services/catalog.service";
import {PreviewDatasetStepComponent} from "./preview-dataset-step.component";
import {DataSourceSelectedEvent} from "../../catalog/datasources/datasources.component";
import {SelectionService} from "../../catalog/api/services/selection.service";
import {PreviewDataSet} from "../../catalog/datasource/preview-schema/model/preview-data-set";
import {DataSource} from "../../catalog/api/models/datasource";

export class DatasetPreviewStepperSavedEvent{
    constructor(public previews:PreviewDataSet[], public singleSelection:boolean) {}
}

@Component({
    selector: "dataset-preview-stepper",
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/dataset-preview-stepper.component.html"
})
export class DatasetPreviewStepperComponent implements OnInit, OnDestroy{

    @Input()
    saveLabel?:string = "Save";

    @Input()
    public showCancel?:boolean = true;

    /***
     * the datasources to choose
     */
    public datasources: DataSource[];

    /**
     * flag to indicate the datasources are populated
     */
    public datasourcesReady:boolean;

    /**
     * flag set when a user selects a datasource
     */
    public datasourceReady:boolean;


    @Output()
    previewSaved:EventEmitter<DatasetPreviewStepperSavedEvent> = new EventEmitter<DatasetPreviewStepperSavedEvent>();

    @Output()
    previewCanceled:EventEmitter<any> = new EventEmitter<any>();

    /**
     * first step form to choose the datasource
     */
    chooseDataSourceForm:FormGroup;

    /**
     * second step form to choose the sample(s)
     */
    sourceSample: FormGroup;

    /**
     * Final step to view the preview
     */
    previewForm:FormGroup;



    @ViewChild("stepper")
    stepper: MatStepper;

    @ViewChild("preview")
    preview:PreviewDatasetStepComponent

    activeDataSource:DataSource;

    activeParams:any;

    sourceSampleValid:boolean;

    previewFormValid:boolean;


    /**
     * is this a single selection mode  (or multi)
     */
    singleSelection: boolean;

    constructor(private selectionService: SelectionService,
                private catalogService:CatalogService) {

        this.singleSelection = this.selectionService.isSingleSelection();
        this.chooseDataSourceForm = new FormGroup({});
        this.chooseDataSourceForm.addControl("hiddenValidFormCheck",new FormControl())
        this.sourceSample = new FormGroup({})
        this.previewForm = new FormGroup({})

        this.sourceSample.statusChanges.debounceTime(10).subscribe(changes =>{
            this.sourceSampleValid = changes == "VALID"
        });

        this.previewForm.statusChanges.debounceTime(10).subscribe(changes =>{
            this.previewFormValid = changes == "VALID"
        });

    }



    onDatasourceSelected(event:DataSourceSelectedEvent) {
        this.activeDataSource = event.dataSource;
        setTimeout(()=>{
            this.datasourceReady = true;

        })
        this.activeParams = event.params;
        this.chooseDataSourceForm.get("hiddenValidFormCheck").setValue(this.activeDataSource.id);
        this.stepper.next();
    }


    ngOnInit(){

       this.selectionService.reset();
       this.catalogService.getDataSources().subscribe(datasources =>  {
           this.datasources = datasources;
           this.datasourcesReady = true;
       })


    }

    ngOnDestroy(){
      //  this.defineFeedSourceSampleService.viewingNone();
    }


    onSave(){
        let previews = this.preview.previews || [];
        let event = new DatasetPreviewStepperSavedEvent(previews,this.singleSelection);
        this.previewSaved.emit(event);
    }

    onCancel(){
        this.previewCanceled.emit();
    }


}