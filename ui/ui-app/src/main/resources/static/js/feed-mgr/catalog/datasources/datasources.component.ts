import {Component, EventEmitter, Inject, Input, OnInit, Output} from "@angular/core";
import {MatSnackBar} from '@angular/material/snack-bar';
import {TdDataTableService} from "@covalent/core/data-table";
import {TdDialogService} from "@covalent/core/dialogs";
import {LoadingMode, LoadingType, TdLoadingService} from "@covalent/core/loading";
import {TranslateService} from "@ngx-translate/core";
import {StateService} from "@uirouter/angular";
import {concatMap} from "rxjs/operators/concatMap";
import {filter} from "rxjs/operators/filter";
import {finalize} from 'rxjs/operators/finalize';
import {tap} from "rxjs/operators/tap";

import {AccessControlService} from "../../../services/AccessControlService";
import {EntityAccessControlService} from "../../shared/entity-access-control/EntityAccessControlService";
import {DataSource} from "../api/models/datasource";
import {CatalogService} from "../api/services/catalog.service";


export class DataSourceSelectedEvent {
    constructor(public dataSource: DataSource, public params: any, public stateRef: string) {
    }
}

/**
 * Displays available datasources
 */
@Component({
    selector: "catalog-datasources",
    styleUrls: ["./datasources.component.scss"],
    templateUrl: "./datasources.component.html"
})
export class DataSourcesComponent implements OnInit {

    static readonly LOADER = "DataSourcesComponent.LOADER";
    private static topOfPageLoader: string = "DataSourcesComponent.topOfPageLoader";

    /**
     * List of available data sources
     */
    @Input("datasources")
    public datasources: DataSource[];

    @Input("loading")
    public loading: boolean;

    /**
     * List of available data sources
     */
    @Input()
    public selection: string;

    @Input()
    public selectedDatasourceState: string;

    @Input()
    public stateParams: {};

    @Input()
    public readOnly?: boolean = false;

    @Input()
    public displayInCard?: boolean = true;

    public displayHeight?: string = "42vh";

    /**
     * Indicates that edit actions are allowed
     */
    allowEdit = false;

    @Output()
    datasourceSelected: EventEmitter<DataSourceSelectedEvent> = new EventEmitter<DataSourceSelectedEvent>();

    /**
     * Filtered list of datasources to display
     */
    filteredDatasources: DataSource[];

    /**
     * Search term for filtering datasources
     */
    searchTerm: string;

    /**
     * cache of the datasource.id to boolean flag (true has access, false no access)
     * @type {{}}
     */
    cachedDataSourceEditEntityAccess :{[key: string]: boolean} = {};

    constructor(private catalog: CatalogService,
                private dataTable: TdDataTableService,
                private dialog: TdDialogService,
                private loadingService: TdLoadingService,
                private state: StateService,
                @Inject("AccessControlService") private accessControlService: AccessControlService,
                private snackBarService: MatSnackBar,
                private translateService: TranslateService) {
        this.loadingService.create({
            name: DataSourcesComponent.topOfPageLoader,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: 'accent',
        });

        accessControlService.getUserAllowedActions()
            .then((actionSet: any) => this.allowEdit = accessControlService.hasAction(AccessControlService.DATASOURCE_EDIT, actionSet.actions));
    }


    public ngOnInit() {
        this.filter();
        this.displayHeight = (this.displayInCard ? "70vh" : "42vh");
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
        if (this.selectedDatasourceState != undefined) {
            stateRef = this.selectedDatasourceState;
        }
        let params = {datasourceId: datasource.id};
        if (this.stateParams) {
            params = {...params, ...this.stateParams};
        }
        if (this.datasourceSelected.observers.length > 0) {
            this.datasourceSelected.emit(new DataSourceSelectedEvent(datasource, params, stateRef));
        }
        else {
            this.state.go(stateRef, params);
        }
    }

    private hasEditEntityAccess(datasource:DataSource){
        if(this.cachedDataSourceEditEntityAccess[datasource.id] != undefined){
            return this.cachedDataSourceEditEntityAccess[datasource.id] == true;
        }
        else {
            let access = !this.accessControlService.isEntityAccessControlled()
                || this.accessControlService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.DATASOURCE.EDIT_DETAILS, datasource, EntityAccessControlService.entityRoleTypes.DATASOURCE);
            this.cachedDataSourceEditEntityAccess[datasource.id] = access;
            return access;
        }
    }
    isEditable(datasource: DataSource): boolean {
        return (this.allowEdit && !this.readOnly
            && this.hasEditEntityAccess(datasource));
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
        this.translateService.get("CATALOG.DATA_SOURCES.CONFIRM_DELETE").pipe(
            concatMap(messages => {
                return this.dialog.openConfirm({
                    message: messages["MESSAGE"],
                    title: messages["TITLE"],
                    acceptButton: messages["ACCEPT"],
                    cancelButton: messages["CANCEL"]
                }).afterClosed();
            }),
            filter(accept => accept),
            tap(() => this.loadingService.register(DataSourcesComponent.topOfPageLoader)),
            concatMap(() => this.catalog.deleteDataSource(datasource)),
            finalize(() => this.loadingService.resolve(DataSourcesComponent.topOfPageLoader))
        ).subscribe(
            () => this.state.go("catalog.datasources", {}, {reload: true}),
            err => {
                console.error(err);
                if (err.status == 409) {
                    this.showSnackBar("Failed to delete. This data source is currently being used by a feed.");
                } else {
                    this.showSnackBar('Failed to delete.', err.message);
                }
            }
        );
    }

    /**
     * Updates filteredDatasources by filtering availableDatasources.
     */
    private filter() {
        let filteredConnectors = this.dataTable.filterData(this.datasources, this.searchTerm, true);
        filteredConnectors = this.dataTable.sortData(filteredConnectors, "title");
        this.filteredDatasources = filteredConnectors;
    }

    showSnackBar(msg: string, err?: string): void {
        this.snackBarService
            .open(msg + ' ' + (err ? err : ""), 'OK', {duration: 5000});
    }
}
