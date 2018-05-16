import {Component, Input, OnInit} from "@angular/core";
import {StateRegistry, StateService} from "@uirouter/angular";

import {ConnectorTab} from "../api/models/connector-tab";
import {DataSet} from "../api/models/dataset";

/**
 * Displays tabs for configuring a data set (or connection).
 */
@Component({
    selector: "explorer-dataset",
    templateUrl: "js/feed-mgr/catalog/dataset/dataset.component.html"
})
export class DatasetComponent implements OnInit {

    /**
     * Data set to be configured
     */
    @Input()
    public dataSet: DataSet;

    /**
     * List of tabs
     */
    tabs: ConnectorTab[] = [];

    constructor(private state: StateService, private stateRegistry: StateRegistry) {
    }

    public ngOnInit() {
        // Add tabs and register router states
        if (this.dataSet.connector.tabs) {
            this.tabs = this.dataSet.connector.tabs;
            for (let tab of this.dataSet.connector.tabs) {
                if (tab.state) {
                    this.stateRegistry.register(tab.state);
                }
            }
        }

        // Add system tabs
        this.tabs.push({label: "Preview", sref: ".preview"});

        // Go to the first tab
        this.state.go(this.tabs[0].sref);
    }
}
