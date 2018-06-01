import {Component, Injector, Input, OnInit} from "@angular/core";
import {TdDataTableService} from "@covalent/core/data-table";
import {TdDialogService} from "@covalent/core/dialogs";
import {TdLoadingService} from "@covalent/core/loading";
import {StateService} from "@uirouter/angular";

import {DataSource} from "../api/models/datasource";
import {CatalogService} from "../api/services/catalog.service";

/**
 * Displays available datasources
 */
@Component({
    selector: "catalog-datasources",
    styleUrls: ["js/feed-mgr/catalog/datasources/datasources.component.css"],
    templateUrl: "js/feed-mgr/catalog/datasources/datasources.component.html"
})
export class DataSourcesComponent implements OnInit {

    static readonly LOADER = "DataSourcesComponent.LOADER";

    /**
     * List of available data sources
     */
    @Input("datasources")
    public datasources: DataSource[];

    /**
     * Filtered list of datasources to display
     */
    filteredDatasources: DataSource[];

    /**
     * Search term for filtering datasources
     */
    searchTerm: string;

    constructor(private catalog: CatalogService, private dataTable: TdDataTableService, private dialog: TdDialogService, private loadingService: TdLoadingService,
                private state: StateService, private $$angularInjector: Injector) {
        // Register Add button
        let accessControlService = $$angularInjector.get("AccessControlService");
        let addButtonService = $$angularInjector.get("AddButtonService");
        accessControlService.getUserAllowedActions()
            .then(function (actionSet:any) {
                if (accessControlService.hasAction(accessControlService.DATASOURCE_EDIT, actionSet.actions)) {
                    addButtonService.registerAddButton("catalog", function () {
                        state.go(".connectors")
                    });
                }
            });
    }

    public ngOnInit() {
        this.filter();
    }

    search(term: string) {
        this.searchTerm = term;
        this.filter();
    }

    /**
     * Creates a new data set from the specified datasource.
     */
    selectDatasource(datasource: DataSource) {
        this.state.go("catalog.datasource", {datasourceId: datasource.id});
    }

    /**
     * Updates filteredDatasources by filtering availableDatasources.
     */
    private filter() {
        let filteredConnectors = this.dataTable.filterData(this.datasources, this.searchTerm, true);
        filteredConnectors = this.dataTable.sortData(filteredConnectors, "title");
        this.filteredDatasources = filteredConnectors;
    }
}
