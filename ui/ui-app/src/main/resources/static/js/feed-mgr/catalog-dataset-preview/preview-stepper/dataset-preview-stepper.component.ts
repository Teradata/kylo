import {ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Output, TemplateRef, ViewChild} from "@angular/core";
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
import {AccessControlService} from "../../../services/AccessControlService";
import AccessConstants from "../../../constants/AccessConstants";
import {PreviewStepperStep} from "./preview-stepper-step";

export class DatasetPreviewStepperSavedEvent{
    /**
     * Additional data that can be passed to a subscriber of the save event
     */
    data?:any;
    constructor(public previews:PreviewDataSet[], public singleSelection:boolean) {}
}

export class DatasetPreviewStepperCanceledEvent{
    constructor(public skip:boolean) {}
}

@Component({
    selector: "dataset-preview-stepper",
    templateUrl: "./dataset-preview-stepper.component.html",
    changeDetection:ChangeDetectionStrategy.OnPush
})
export class DatasetPreviewStepperComponent implements OnInit, OnDestroy{

    @Input()
    saveLabel?:string = "Save";

    @Input()
    public showCancel?:boolean = true;

    @Input()
    displayBottomActionButtons:boolean;

    @Input()
    displayTopActionButtons:boolean;

    @Input()
    allowMultiSelection:boolean;

    @Input()
    showSkipSourceButton:boolean;

    /***
     * the data sources to choose
     */
    public datasources: DataSource[] = [];

    /***
     * Whether data sources have been loaded
     */
    public loading : boolean = true;

    @Output()
    previewSaved:EventEmitter<DatasetPreviewStepperSavedEvent> = new EventEmitter<DatasetPreviewStepperSavedEvent>();

    @Output()
    previewCanceled:EventEmitter<DatasetPreviewStepperCanceledEvent> = new EventEmitter<DatasetPreviewStepperCanceledEvent>();

    /**
     * first step form to choose the datasource
     */
    chooseDataSourceForm:FormGroup;

    /**
     * second step form to choose the sample(s)
     */
    selectDataForm: FormGroup;

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

    accessDenied:boolean;

    requiredPermissions= [AccessConstants.DATASOURCE_ACCESS];

    stateParams = undefined;

    @Input()
    additionalSteps:PreviewStepperStep<any>[] = []

    constructor(private selectionService: SelectionService,
                private catalogService:CatalogService,
                private dataSourceService:DatasetPreviewStepperService,
                private cd:ChangeDetectorRef,
                @Inject("AccessControlService") private accessControlService:AccessControlService) {


        this.singleSelection = this.selectionService.isSingleSelection();
        this.chooseDataSourceForm = new FormGroup({});
        this.chooseDataSourceForm.addControl("hiddenValidFormCheck",new FormControl())
        this.selectDataForm = new FormGroup({})
        this.previewForm = new FormGroup({})

        this.selectDataForm.statusChanges.debounceTime(10).subscribe(changes =>{
            this.cd.markForCheck()
        });

        this.previewForm.statusChanges.debounceTime(10).subscribe(changes =>{
            this.cd.markForCheck()
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
        this.cd.markForCheck()
    }

    /**
     * go to next step
     */
    next(){
        this.stepper.next();
    }

    /**
     * go to prev step
     */
    previous(){
        this.stepper.previous();
    }

    /**
     * show the next button?
     * @return {boolean}
     */
    showNext(){
        let idx = this.dataSourceService.stepIndex;
        //dont show next on first step.  Require them to select a data source
        let lastIndex = this.getLastIndex();
        return idx != undefined && idx >0 && idx <lastIndex;
    }

    showSave(){
        let idx = this.dataSourceService.stepIndex;
      // show on last step
        let lastIndex = this.getLastIndex();
        return idx != undefined && idx == lastIndex ;
    }

    private getLastIndex(){
        return this.additionalSteps ? this.additionalSteps.length +2 : 2;
    }



    saveDisabled(){
        if(this.additionalSteps){
         let lastStep = this.getAdditionalStepForIndex(this.getLastIndex());
         return lastStep != undefined ? lastStep.stepControl.invalid : false;
        }
        else {
            return this.previewForm.invalid;
        }
    }

    /**
     * Show the back button
     * @return {boolean}
     */
    showBack(){
        //dont show back on first step.
        let idx = this.dataSourceService.stepIndex;
        return idx != undefined && idx >0;
    }

    /**
     * is the next button disabled?
     * @return {boolean}
     */
    nextDisabled(){
        let idx = this.dataSourceService.stepIndex;
        if(idx ==1){
            //select data form
            return this.selectDataForm.invalid;
        }
        else if(idx ==2) {
            //preview data form
            return this.previewForm.invalid;
        }
        else if(this.additionalSteps){
            let step = this.getAdditionalStepForIndex(idx);
            return step != null ? step.stepControl.invalid : false;
        }
        else {
            return false;
        }
    }

    /**
     * Get the additional step object for a given index
     * Additional steps start on index 3
     * @param idx
     */
    private getAdditionalStepForIndex(idx:number) {
        if(this.additionalSteps && idx >2) {
            let additionalIndex =  idx - 3;
            let step = this.additionalSteps.length >= additionalIndex + 1 ? this.additionalSteps[additionalIndex] : null;
            return step != null ? step : null;
        }
        return null;
    }

    public getLastAdditionalStep() {
        if (this.additionalSteps != null && this.additionalSteps.length > 0) {
            let idx = this.getLastIndex();
            return this.getAdditionalStepForIndex(idx)
        }
        else {
            return null;
        }
    }

    ngOnInit(){
       //clear any previous selections
       this.selectionService.reset();
       if(this.allowMultiSelection){
           this.selectionService.multiSelectionStrategy();
       }
       else {
           this.selectionService.singleSelectionStrategy();
       }
       //get the datasources
        //user needs access to datasource to get the
        var allowedActions = this.accessControlService.cachedUserAllowedActions[this.accessControlService.DEFAULT_MODULE];

        if(!this.accessControlService.hasAllActions(this.requiredPermissions,allowedActions)) {
            this.accessDenied = true;
            this.loading = false;
        }
        else {
            this.catalogService.getDataSources().subscribe(datasources => {
                datasources.forEach(ds => {
                    this.datasources.push(ds);
                })
                //this.datasources = datasources;
                //manually notify the view to check for changes
                this.catalogDatasources.search(" ")
                this.cd.markForCheck();
                this.loading = false;
            });
        }


    }

    ngOnDestroy(){

    }

    getDataSets() {
        return this.preview.previews || [];
    }

    getDataSetsLength(){
        return this.getDataSets().length
    }


    getPreviewSaveEvent() {
        let previews = this.preview.previews || [];
        return new DatasetPreviewStepperSavedEvent(previews,this.singleSelection);
    }

    saved(saveEvent:DatasetPreviewStepperSavedEvent){
        this.previewSaved.emit(saveEvent);
    }
    /**
     * Callback when the user clicks the "Save/Add" button from the last step to do something with one or more datasets with previews
     */
    onSave(){
        let event = this.getPreviewSaveEvent()
        this.saved(event);
    }

    /**
     * When a user cancels the stepper
     */
    onCancel(){
        this.previewCanceled.emit(new DatasetPreviewStepperCanceledEvent(false));
    }

    onSkip(){
        this.previewCanceled.emit(new DatasetPreviewStepperCanceledEvent(true));
    }


}