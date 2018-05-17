import {Component, Input} from "@angular/core";
import {TdDataTableService} from "@covalent/core/data-table";
import {TdDialogService} from "@covalent/core/dialogs";
import {TdLoadingService} from "@covalent/core/loading";
import {StateService} from "@uirouter/angular";

import {CatalogService} from "../api/services/catalog.service";
import {Connector} from '../api/models/connector';

/**
 * Displays the available connectors and creates new data sets.
 */
@Component({
    selector: "explorer-connector-types",
    styleUrls: ["js/feed-mgr/catalog/connectors/connector-types.component.css"],
    templateUrl: "js/feed-mgr/catalog/connectors/connector-types.component.html"
})
export class ConnectorsComponent {

    static LOADER = "ConnectorsComponent.LOADER";

    /**
     * List of available connectors
     */
    @Input("connectors")
    public availableConnectorTypes: Connector[];

    /**
     * Filtered list of connectors to display
     */
    filteredConnectorTypes: Connector[];

    /**
     * Search term for filtering connectors
     */
    searchTerm: string;

    constructor(private catalog: CatalogService, private dataTable: TdDataTableService, private dialog: TdDialogService, private loading: TdLoadingService, private state: StateService) {
    }

    public ngOnInit() {
        console.log('ngOnInit connector types');
        this.filter();
    }

    search(term: string) {
        this.searchTerm = term;
        this.filter();
    }

    /**
     * Creates a new data set from the specified connector.
     */
    selectConnector(connector: Connector) {
        console.log('selectConnector');
        this.state.go(".new-datasource", {connectorId: connector.id});
    }

    /**
     * Updates filteredConnectorTypes by filtering availableConnectorTypes.
     */
    private filter() {
        let filteredConnectorTypes = this.availableConnectorTypes.filter(connector => connector.hidden !== true);
        filteredConnectorTypes = this.dataTable.filterData(filteredConnectorTypes, this.searchTerm, true);
        filteredConnectorTypes = this.dataTable.sortData(filteredConnectorTypes, "title");
        this.filteredConnectorTypes = filteredConnectorTypes;
    }
}
