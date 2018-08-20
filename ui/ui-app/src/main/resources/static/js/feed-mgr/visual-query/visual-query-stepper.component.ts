import * as angular from "angular";

import {FeedDataTransformation} from "../model/feed-data-transformation";
import {QueryEngine} from "./wrangler/query-engine";
import {PreviewDatasetCollectionService} from "../catalog/api/services/preview-dataset-collection.service";

import {Component, Input, Injector, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

import {StateRegistry, StateService} from "@uirouter/angular";
import {QueryEngineFactory} from "./wrangler/query-engine-factory.service";
import {MatStepper} from "@angular/material/stepper";

@Component({
    selector: 'visual-query-stepper',
    templateUrl: 'js/feed-mgr/visual-query/visual-query-stepper.component.html'
})
export class VisualQueryStepperComponent implements OnInit, OnDestroy{

    static LOADER = "VisualQueryComponent.LOADER";

    /**
     * Query engine and data transformation model
     */
    dataModel: { engine: QueryEngine<any>, model: FeedDataTransformation };

    /**
     * Query engine for the data model
     */
    @Input()
    engineName:string;

    @ViewChild("stepper")
    stepper:MatStepper;

    /**
     * Allow the user to change data sources and query for tables/files etc
     * This will be disabled when using the new ng2 feed stepper coming from a data wrangler feed as those are populated via the PreviewdatasetCollectionService
     */
    @Input()
    showDatasources:boolean = true;


    /**
     * Should we show the save step?
     */
    @Input()
    showSaveStep:boolean = true;

    @Input()
    toggleSideNavOnDestroy:boolean = true;

    /**
     * The Query Engine
     */
    engine:QueryEngine<any>;


    /**
     * Form Group for the drag and drop build query
     */
    buildQueryFormGroup: FormGroup;

    /**
     * Form Group for the wrangler
     */
    transformDataFormGroup: FormGroup;

    /**
     * Form group for the save step
     */
    saveStepFormGroup: FormGroup;

    private queryEngineFactory:QueryEngineFactory;

    private stateService: any;

    private sideNavService:any;

    private previewDataSetCollectionService : PreviewDatasetCollectionService

     /**
     * Constructs a {@code VisualQueryComponent}.
     */
    constructor(private _formBuilder: FormBuilder,
                private $$angularInjector: Injector) {
        // Manage the sidebar navigation
         this.previewDataSetCollectionService = $$angularInjector.get("PreviewDatasetCollectionService");
        console.log("PreviewDatasetCollectionService",this.previewDataSetCollectionService.datasets)
        this.sideNavService = $$angularInjector.get("SideNavService");
         this.stateService = $$angularInjector.get("StateService");

         this.sideNavService.hideSideNav();
         this.initFormGroups();
    }

    /**
     * Navigates to the Feeds page when the stepper is cancelled.
     */
    cancelStepper() {
        this.stateService.navigateToHome();
    }

    /**
     * Resets the side state.
     */
    ngOnDestroy(): void {
        if(this.toggleSideNavOnDestroy) {
            this.sideNavService.showSideNav();
        }
    }

    ngOnInit(): void {

        this.getEngine();
        this.dataModel = {engine: this.engine, model: {} as FeedDataTransformation};
        let collection = this.previewDataSetCollectionService.getSparkDataSets();
        this.dataModel.model.datasets = collection;
        console.log('collection',collection)


    }

    getEngine(){
        this.queryEngineFactory = this.$$angularInjector.get("VisualQueryEngineFactory");
        if(this.engineName == undefined) {
            this.engineName = 'spark';
        }
        this.queryEngineFactory.getEngine(this.engineName).then((engine:QueryEngine<any>) => {
            this.engine = engine;
        });
    }


    private initFormGroups(){
        this.buildQueryFormGroup = this._formBuilder.group({
        });
        this.transformDataFormGroup = this._formBuilder.group({
            secondCtrl: ['', Validators.required]
        });
        this.saveStepFormGroup = this._formBuilder.group({});
    }

}

