import {Component, Input} from "@angular/core";
import {TdDataTableService} from "@covalent/core/data-table";
import {TdDialogService} from "@covalent/core/dialogs";
import {TdLoadingService} from "@covalent/core/loading";
import {StateService} from "@uirouter/angular";
import {finalize} from "rxjs/operators/finalize";

import {Connector} from "../catalog/models/connector";
import {CatalogService} from "../catalog/services/catalog.service";

/**
 * Displays the available connectors and creates new data sets.
 */
@Component({
    selector: "explorer-connectors",
    styleUrls: ["js/feed-mgr/explorer/connectors/connectors.component.css"],
    templateUrl: "js/feed-mgr/explorer/connectors/connectors.component.html"
})
export class ConnectorsComponent {

    static readonly LOADER = "connectorsLoader";

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
        this.loading.register(ConnectorsComponent.LOADER);
        this.catalog.createDataSet(connector)
            .pipe(finalize(() => this.loading.resolve(ConnectorsComponent.LOADER)))
            .subscribe(
                dataSet => this.state.go(".dataset", {dataSetId: dataSet.id}),
                () => this.dialog.openAlert({message: "The connector is not available"})
            );
    }

    /**
     * Updates filteredConnectors by filtering availableConnectors.
     */
    private filter() {
        let filteredConnectors = this.availableConnectors.filter(connector => connector.hidden !== true);
        filteredConnectors = this.dataTable.filterData(filteredConnectors, this.searchTerm, true);
        filteredConnectors = this.dataTable.sortData(filteredConnectors, "title");
        this.filteredConnectors = filteredConnectors;
    }
}
