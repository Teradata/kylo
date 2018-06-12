import {Component, Input} from "@angular/core";
import {TdDataTableService} from "@covalent/core/data-table";
import {TdDialogService} from "@covalent/core/dialogs";
import {TdLoadingService} from "@covalent/core/loading";
import {StateService} from "@uirouter/angular";

import {CatalogService} from "../api/services/catalog.service";
import {Connector} from '../api/models/connector';

/**
 * Displays selected connector properties.
 */
@Component({
    selector: "catalog-connector",
    // styleUrls: ["js/feed-mgr/catalog/connector/connector.component.css"],
    templateUrl: "js/feed-mgr/catalog/connector/connector.component.html"
})
export class ConnectorComponent {

    static LOADER = "ConnectorComponent.LOADER";

    @Input("connector")
    public connector: Connector;

    constructor(private catalog: CatalogService, private dataTable: TdDataTableService, private dialog: TdDialogService, private loading: TdLoadingService, private state: StateService) {
    }

    public ngOnInit() {
    }

    /**
     * Creates a new datasource for this Connector
     */
    createDatasource() {
        const datasourceId = "ruslans-local-file-system"; //post new datasource, get its id
        this.state.go("catalog.datasource", {datasourceId: datasourceId});
    }
}
