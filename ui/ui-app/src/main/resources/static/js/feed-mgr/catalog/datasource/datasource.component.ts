import * as angular from "angular";

import {Component, Injector, Input, OnDestroy, OnInit} from "@angular/core";
import {StateRegistry, StateService} from "@uirouter/angular";

import {ConnectorTab} from "../api/models/connector-tab";
import {ConnectorPlugin} from '../api/models/connector-plugin';
import {DataSource} from '../api/models/datasource';
import {SelectionService} from '../api/services/selection.service';
import {Node} from '../api/models/node'
import {DatasetChangeEvent, PreviewDatasetCollectionService} from "../api/services/preview-dataset-collection.service";
import {PreviewDataSet} from "./preview-schema/model/preview-data-set";
import {ISubscription} from "rxjs/Subscription";

/**
 * Displays tabs for configuring a data set (or connection).
 */
@Component({
    selector: "catalog-dataset",
    templateUrl: "./datasource.component.html"
})
export class DatasourceComponent implements OnInit, OnDestroy {

    static readonly LOADER = "DatasourceComponent.LOADER";

    /**
     * Data set to be configured
     */
    @Input("datasource")
    public datasource: DataSource;

    @Input("connectorPlugin")
    public plugin: ConnectorPlugin;

    /**
     * List of tabs
     */
    tabs: ConnectorTab[] = [];

    /**
     * the total number of items in the collection
     * @type {number}
     */
    private dataSetCollectionSize: number = 0;

    private dataSetChangedSubscription :ISubscription;

    /**
     * Note:$$angularInjector is used here for previewDatasetCollectionService since its shared with the Angular 1 Wrangler
     * @param {StateService} state
     * @param {StateRegistry} stateRegistry
     * @param {SelectionService} selectionService
     * @param {SelectionService} selectionService
     * @param {Injector} $$angularInjector
     */
    constructor(protected state: StateService, protected stateRegistry: StateRegistry, protected selectionService: SelectionService,  protected previewDatasetCollectionService: PreviewDatasetCollectionService) {
        this.dataSetChangedSubscription = this.previewDatasetCollectionService.subscribeToDatasetChanges(this.onDataSetCollectionChanged.bind(this))
        this.selectionService.multiSelectionStrategy();
    }

    protected initTabs(statePrefix?:string ) {
        // Add tabs and register router states
        if (this.plugin.tabs) {
            this.tabs = angular.copy(this.plugin.tabs);
            for (let tab of this.tabs) {
                if (tab.state) {
                    this.stateRegistry.register(tab.state);
                }
            }
        }

        // Add system tabs
        this.tabs.push({label: "Preview", sref: ".preview"});
    }

    public ngOnInit() {

        this.selectionService.reset(this.datasource.id);
        this.initTabs();
        // Go to the first tab
        this.state.go(this.tabs[0].sref, {datasourceId:this.datasource.id}, {location: "replace"});
    }

    ngOnDestroy(){
        this.dataSetChangedSubscription.unsubscribe();
    }

    public testGo(){
        this.state.go("catalog.datasource.preview");//, {location: "replace"});
    }

    public isDisabled(tab: ConnectorTab) {
        let disabled = false;
        if (tab.sref === ".preview") {
            //disable preview until there is a selection
            const root: Node = <Node>this.selectionService.get(this.datasource.id);
            disabled = root ? root.countSelectedDescendants() === 0 : false;
        }
        return disabled;
    }

    /**
     * Listener for changes from the collection service
     * @param {PreviewDataSet[]} dataSets
     */
    onDataSetCollectionChanged(event:DatasetChangeEvent){
        this.dataSetCollectionSize = event.totalDatasets;
    }

    /**
     * Go to the visual query populating with the selected datasets
     * TODO go to a Feed Stepper thats modified to fit any-to-any
     */
    wrangleDataSets(){
        this.state.go("visual-query");

    }

}
