import {Component, Input} from "@angular/core";
import {TdDataTableService} from "@covalent/core/data-table";
import {TdDialogService} from "@covalent/core/dialogs";
import {TdLoadingService} from "@covalent/core/loading";
import {StateService} from "@uirouter/angular";

import {CatalogService} from "../api/services/catalog.service";
import {Connector} from '../api/models/connector';

/**
 * Displays available connectors.
 */
@Component({
    selector: "catalog-connectors",
    styleUrls: ["js/feed-mgr/catalog/connectors/connectors.component.css"],
    templateUrl: "js/feed-mgr/catalog/connectors/connectors.component.html"
})
export class ConnectorsComponent {

    static LOADER = "ConnectorsComponent.LOADER";

    /**
     * List of available connectors
     */
    @Input("connectors")
    public availableConnectors: Connector[];

    /**
     * Filtered list of connectors to display
     */
    filteredConnectors: Connector[];

    /**
     * Search term for filtering connectors
     */
    searchTerm: string;

    constructor(private catalog: CatalogService, private dataTable: TdDataTableService, private dialog: TdDialogService, private loading: TdLoadingService, private state: StateService) {
    }

    public ngOnInit() {
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
        this.state.go("catalog.new-datasource", {connectorId: connector.id});
    }

    /**
     * Updates filteredConnectors by filtering availableConnectors.
     */
    private filter() {
        let filteredConnectorTypes = this.dataTable.filterData(this.availableConnectors, this.searchTerm, true);
        filteredConnectorTypes = this.dataTable.sortData(filteredConnectorTypes, "title");
        this.filteredConnectors = filteredConnectorTypes;
    }
}
