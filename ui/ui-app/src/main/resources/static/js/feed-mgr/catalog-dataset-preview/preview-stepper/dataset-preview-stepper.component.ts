import {ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {MatStepper} from "@angular/material/stepper";
import {CatalogService} from "../../catalog/api/services/catalog.service";
import {PreviewDatasetStepComponent} from "./preview-dataset-step.component";
import {DataSourcesComponent, DataSourceSelectedEvent} from "../../catalog/datasources/datasources.component";
import {SelectionService} from "../../catalog/api/services/selection.service";
import {PreviewDataSet} from "../../catalog/datasource/preview-schema/model/preview-data-set";
import {DataSource} from "../../catalog/api/models/datasource";
import { StepperSelectionEvent } from '@angular/cdk/stepper';
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {Subject} from "rxjs/Subject";
import {DatasetPreviewStepperService} from "./dataset-preview-stepper.service";

export class DatasetPreviewStepperSavedEvent{
    constructor(public previews:PreviewDataSet[], public singleSelection:boolean) {}
}

@Component({
    selector: "dataset-preview-stepper",
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/dataset-preview-stepper.component.html",
    changeDetection:ChangeDetectionStrategy.OnPush
})
export class DatasetPreviewStepperComponent implements OnInit, OnDestroy{

    @Input()
    saveLabel?:string = "Save";

    @Input()
    public showCancel?:boolean = true;

    /***
     * the datasources to choose
     */
    public datasources: DataSource[] = [];

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

    @ViewChild("catalogDatasources")
    catalogDatasources:DataSourcesComponent

    /**
     * is this a single selection mode  (or multi)
     */
    singleSelection: boolean;

    constructor(private selectionService: SelectionService,
                private catalogService:CatalogService,
                private dataSourceService:DatasetPreviewStepperService,
                private cd:ChangeDetectorRef) {

        this.singleSelection = this.selectionService.isSingleSelection();
        this.chooseDataSourceForm = new FormGroup({});
        this.chooseDataSourceForm.addControl("hiddenValidFormCheck",new FormControl())
        this.sourceSample = new FormGroup({})
        this.previewForm = new FormGroup({})

        this.sourceSample.statusChanges.debounceTime(10).subscribe(changes =>{
        //    this.sourceSampleValid = changes == "VALID"
        });

        this.previewForm.statusChanges.debounceTime(10).subscribe(changes =>{
       //     this.previewFormValid = changes == "VALID"
        });


    }


    /**
     * Callback when the user selects a datasource
     * set the datasource to the shared stepper service and jump to the next step
     * @param {DataSourceSelectedEvent} event
     */
    onDatasourceSelected(event:DataSourceSelectedEvent) {
        this.dataSourceService.setDataSource(event.dataSource, event.params);
        this.chooseDataSourceForm.get("hiddenValidFormCheck").setValue(event.dataSource.id);
        this.stepper.next();
    }

    /**
     * Callback when the user changes a step
     * @param {StepperSelectionEvent} event
     */
    onStepSelectionChanged(event:StepperSelectionEvent){
        let idx = event.selectedIndex;
        this.dataSourceService.setStepIndex(idx)
    }



    ngOnInit(){
       //clear any previous selections
       this.selectionService.reset();
       //get the datasources
       this.catalogService.getDataSources().subscribe(datasources =>  {
           datasources.forEach(ds => {
               this.datasources.push(ds);
           })
           //this.datasources = datasources;
           //manually notify the view to check for changes
           this.catalogDatasources.search(" ")
           this.cd.markForCheck();
       })


    }

    ngOnDestroy(){

    }

    /**
     * Callback when the user clicks the "Save/Add" button from the last step to do something with one or more datasets with previews
     */
    onSave(){
        let previews = this.preview.previews || [];
        let event = new DatasetPreviewStepperSavedEvent(previews,this.singleSelection);
        this.previewSaved.emit(event);
    }

    /**
     * When a user cancels the stepper
     */
    onCancel(){
        this.previewCanceled.emit();
    }


}