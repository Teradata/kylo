import * as angular from 'angular';
import {Component, Injector, Input, OnInit} from "@angular/core";
import {TdDataTableService} from "@covalent/core/data-table";
import {TdDialogService} from "@covalent/core/dialogs";
import {LoadingMode, LoadingType, TdLoadingService} from "@covalent/core/loading";
import {StateService} from "@uirouter/angular";

import {DataSource} from "../api/models/datasource";
import {CatalogService} from "../api/services/catalog.service";
import {finalize} from 'rxjs/operators/finalize';
import {catchError} from 'rxjs/operators/catchError';
import {MatSnackBar} from '@angular/material/snack-bar';
import * as _ from "underscore";
import { TranslateService } from '@ngx-translate/core';

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
    private static topOfPageLoader: string = "DataSourcesComponent.topOfPageLoader";

    /**
     * List of available data sources
     */
    @Input("datasources")
    public datasources: DataSource[];

    /**
     * List of available data sources
     */
    @Input()
    public selection: string;

    @Input()
    public selectedDatasourceState:string

    @Input()
    public stateParams:{}

    /**
     * Filtered list of datasources to display
     */
    filteredDatasources: DataSource[];

    /**
     * Search term for filtering datasources
     */
    searchTerm: string;

    constructor(private catalog: CatalogService, private dataTable: TdDataTableService, private dialog: TdDialogService, private loadingService: TdLoadingService,
                private state: StateService, private $$angularInjector: Injector, private snackBarService: MatSnackBar, private translate: TranslateService) {
        this.loadingService.create({
            name: DataSourcesComponent.topOfPageLoader,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: 'accent',
        });

        // Register Add button
        let accessControlService = $$angularInjector.get("AccessControlService");
        let addButtonService = $$angularInjector.get("AddButtonService");
        accessControlService.getUserAllowedActions()
            .then(function (actionSet:any) {
                if (accessControlService.hasAction(accessControlService.DATASOURCE_EDIT, actionSet.actions)) {
                    addButtonService.registerAddButton("catalog.datasources", function () {
                        state.go("catalog.connectors")
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
        let stateRef = "catalog.datasource";
        if(this.selectedDatasourceState != undefined){
            stateRef = this.selectedDatasourceState;
        }
        let params = {datasourceId: datasource.id};
        if(this.stateParams){
            _.extend(params,this.stateParams);
        }
        this.state.go(stateRef, params);
    }

    isEditable(datasource: DataSource): boolean {
        return datasource.id !== "file-uploads" && datasource.id !== "hive";
    }

    /**
     * Edit properties of datasource
     */
    editDatasource(event: any, datasource: DataSource) {
        event.stopPropagation();
        this.state.go("catalog.new-datasource", {connectorId: datasource.connector.id, datasourceId: datasource.id});
    }

    /**
     * Delete datasource
     */
    deleteDatasource(event: any, datasource: DataSource) {
        event.stopPropagation();
        this.loadingService.register(DataSourcesComponent.topOfPageLoader);
        this.catalog.deleteDataSource(datasource)
            .pipe(finalize(() => {
                this.loadingService.resolve(DataSourcesComponent.topOfPageLoader);
            }))
            .pipe(catchError((err) => {
                this.showSnackBar(this.translate.instant('views.common.delete.failure',{entity:''}), err.message);
                return [];
            }))
            .subscribe(() => {
                this.state.go("catalog.datasources", {}, {reload: true});
            });
    }

    /**
     * Updates filteredDatasources by filtering availableDatasources.
     */
    private filter() {
        let filteredConnectors = this.dataTable.filterData(this.datasources, this.searchTerm, true);
        filteredConnectors = this.dataTable.sortData(filteredConnectors, "title");
        this.filteredDatasources = filteredConnectors;
    }

    showSnackBar(msg: string, err: string): void {
        this.snackBarService
            .open(msg + ' ' + (err ? err : ""), 'OK', { duration: 5000 });
    }
}
