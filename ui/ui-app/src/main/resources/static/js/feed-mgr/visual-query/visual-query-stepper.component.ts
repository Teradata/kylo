import {Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Output} from "@angular/core";

import {PreviewDatasetCollectionService} from "../catalog/api/services/preview-dataset-collection.service";
import {FeedDataTransformation} from "../model/feed-data-transformation";
import {Feed} from "../model/feed/feed.model";
import {QueryEngine} from "./wrangler/query-engine";
import {StepperSelectionEvent} from "@angular/cdk/stepper";
import {MatIconRegistry} from "@angular/material/icon";
import {FeedLoadingService} from "../feeds/define-feed-ng2/services/feed-loading-service";
import {SparkQueryEngine} from './services/spark/spark-query-engine';

@Component({
    selector: 'visual-query-stepper',
    templateUrl: './visual-query-stepper.component.html'
})
export class VisualQueryStepperComponent implements OnInit, OnDestroy {

    static readonly LOADER = "VisualQueryComponent.LOADER";

    /**
     * Query engine for the data model
     */
    @Input()
    engineName: string;

    @Input()
    heightOffset: number;

    /**
     * Allow the user to change data sources and query for tables/files etc
     * This will be disabled when using the new ng2 feed stepper coming from a data wrangler feed as those are populated via the PreviewdatasetCollectionService
     */
    @Input()
    showDatasources: boolean = true;

    /**
     * Should we show the save step?
     */
    @Input()
    showSaveStep: boolean = true;

    @Input()
    toggleSideNavOnDestroy: boolean = true;

    /**
     * Is feed save in progress
     * @type {boolean}
     */
    @Input()
    isFeedSaving: boolean = false;

    /**
     * Event emitted to cancel the model changes
     */
    @Output()
    cancel = new EventEmitter<void>();

    /**
     * Event emitted to save the model changes
     */
    @Output()
    save = new EventEmitter<void>();

    /**
     * Query engine and data transformation model
     */
    dataModel: { engine: QueryEngine<any>, model: FeedDataTransformation };

    /**
     * The Query Engine
     */
    engine: QueryEngine<any>;

    /**
     * Feed model from NgModel
     */
    feed: Feed;

    /**
     * Indicates if the transform step has been visited
     */
    visitedTransform = false;

    /**
     * Indicates if query execution has failed
     */
    queryExecutionFailure = false;

    /**
     * Constructs a {@code VisualQueryComponent}.
     */
    constructor(@Inject("PreviewDatasetCollectionService") private previewDataSetCollectionService: PreviewDatasetCollectionService,
                @Inject("SideNavService") private sideNavService: any, @Inject("StateService") private stateService: any,
                private engineRef: SparkQueryEngine, private matIconRegistry: MatIconRegistry) {

        this.engine = engineRef;

        matIconRegistry.registerFontClassAlias ('fa');

        // Manage the sidebar navigation
        this.sideNavService.hideSideNav();
    }

    /**
     * Resets the side state.
     */
    ngOnDestroy(): void {
        if (this.toggleSideNavOnDestroy) {
            this.sideNavService.showSideNav();
        }
    }

    ngOnInit(): void {
        this.dataModel = {engine: this.engine, model: {} as FeedDataTransformation};
        let collection = this.previewDataSetCollectionService.getSparkDataSets();
        this.dataModel.model.datasets = collection;

    }

    /**
     * Emits a cancel event or navigates to the Feeds page when the stepper is cancelled.
     */
    onCancel() {
        if (this.cancel.observers.length !== 0) {
            this.cancel.emit();
        } else {
            this.stateService.navigateToHome();
        }
    }

    /**
     * Called when the selected step changes.
     */
    onStepChange(selection: StepperSelectionEvent) {
        if (selection.selectedIndex === 1) {
            this.visitedTransform = true;
        }
    }

    /**
     * Sets the feed for the transformations.
     */
    setFeed(feed: Feed) {
        if (feed != null) {
            this.feed = feed;

            if (typeof feed.dataTransformation !== "object" || feed.dataTransformation === null) {
                feed.dataTransformation = this.dataModel.model;
            } else {
                this.dataModel.model = feed.dataTransformation;
            }
        }
    }

    /**
     * Indicate save feed
     */
    doSave() {
        this.save.emit();
    }

    /**
     * Indicate query failure
     */
    queryFailed() {
        this.queryExecutionFailure = true;
    }

    /**
     * Indicate SQL has changed
     */
    sqlChanged() {
        //reset flag to allow new query's execution
        this.queryExecutionFailure = false;
    }
}
