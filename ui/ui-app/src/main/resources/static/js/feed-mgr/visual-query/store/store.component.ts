import {Input, OnDestroy, OnInit} from "@angular/core";
import * as angular from "angular";
import {Subscription} from "rxjs/Subscription";

import {UserDatasource} from "../../model/user-datasource";
import {DatasourcesServiceStatic} from "../../services/DatasourcesService.typings";
import {VisualQuerySaveService} from "../services/save.service";
import {SaveRequest, SaveResponseStatus} from "../wrangler/api/rest-model";
import {QueryEngine} from "../wrangler/query-engine";
import DatasourcesService = DatasourcesServiceStatic.DatasourcesService;
import JdbcDatasource = DatasourcesServiceStatic.JdbcDatasource;
import TableReference = DatasourcesServiceStatic.TableReference;

export class VisualQueryStoreComponent implements OnDestroy, OnInit {

    /**
     * Target destination type. Either DOWNLOAD or TABLE.
     */
    destination: string;

    /**
     * Identifier for download
     */
    downloadId: string;

    /**
     * Url to download results
     */
    downloadUrl: string;

    /**
     * Query engine
     */
    @Input()
    engine: QueryEngine<any>;

    /**
     * Error message
     */
    error: string;

    /**
     * Spark data source names
     */
    formats: string[];

    /**
     * HTML form
     */
    form: any;

    /**
     * List of Kylo data sources
     */
    kyloDataSources: UserDatasource[] = [];

    /**
     * Indicates the page is loading
     */
    loading = true;

    /**
     * Transformation model
     */
    @Input()
    model: any;

    /**
     * Subscription to notification removal
     */
    removeSubscription: Subscription;

    /**
     * Subscription to save response
     */
    saveSubscription: Subscription;

    /**
     * Index of this step
     */
    stepIndex: string;

    /**
     * Output configuration
     */
    target: SaveRequest = {};

    static readonly $inject: string[] = ["$http", "DatasourcesService", "RestUrlService", "VisualQuerySaveService"];

    constructor(private $http: angular.IHttpService, private DatasourcesService: DatasourcesService, private RestUrlService: any, private VisualQuerySaveService: VisualQuerySaveService) {
        // Listen for notification removals
        this.removeSubscription = this.VisualQuerySaveService.subscribeRemove((event) => {
            if (event.id === this.downloadId) {
                this.downloadId = null;
                this.downloadUrl = null;
            }
        });
    };

    $onDestroy(): void {
        this.ngOnDestroy();
    }

    $onInit(): void {
        this.ngOnInit();
    }

    /**
     * Release resources when component is destroyed.
     */
    ngOnDestroy(): void {
        if (this.removeSubscription) {
            this.removeSubscription.unsubscribe();
        }
        if (this.saveSubscription) {
            this.saveSubscription.unsubscribe();
        }
    }

    /**
     * Initialize resources when component is initialized.
     */
    ngOnInit(): void {
        // Get list of Kylo data sources
        const kyloSourcesPromise = Promise.all([this.engine.getNativeDataSources(), this.DatasourcesService.findAll()])
            .then(resultList => {
                this.kyloDataSources = resultList[0].concat(resultList[1]);
                if (this.model.$selectedDatasourceId) {
                    this.target.jdbc = this.kyloDataSources.find(datasource => datasource.id === this.model.$selectedDatasourceId) as JdbcDatasource;
                }
            });

        // Get list of Spark data sources
        const sparkSourcesPromise = this.$http.get<string[]>(this.RestUrlService.SPARK_SHELL_SERVICE_URL + "/data-sources")
            .then(response => {
                this.formats = response.data.sort();
            });

        // Wait for completion
        Promise.all([kyloSourcesPromise, sparkSourcesPromise])
            .then(() => this.loading = false, () => this.error = "Invalid response from server.");
    }

    /**
     * Downloads the saved results.
     */
    download(): void {
        window.open(this.downloadUrl, "_blank");

        // Clear download buttons
        this.VisualQuerySaveService.removeNotification(this.downloadId);
        this.downloadId = null;
        this.downloadUrl = null;
    }

    /**
     * Find tables matching the specified name.
     */
    findTables(name: any): TableReference[] | Promise<TableReference[]> {
        let tables: TableReference[] | Promise<TableReference[]> = [];

        if (this.target.jdbc) {
            tables = this.engine.searchTableNames(name, this.target.jdbc.id);
            if (tables instanceof Promise) {
                tables = tables.then(response => {
                    this.form.datasource.$setValidity("connectionError", true);
                    return response;
                }, () => {
                    this.form.datasource.$setValidity("connectionError", false);
                    return [];
                });
            } else {
                this.form.datasource.$setValidity("connectionError", true);
            }
        }

        return tables;
    }

    /**
     * Reset options when format changes.
     */
    onFormatChange(): void {
        this.target.options = {};
    }

    /**
     * Saves the results.
     */
    save(): void {
        // Remove current subscription
        if (this.saveSubscription) {
            this.saveSubscription.unsubscribe();
            this.saveSubscription = null;
        }

        // Build request
        let request: SaveRequest;

        if (this.destination === "DOWNLOAD") {
            request = {
                format: this.target.format,
                options: this.target.options
            };
        } else {
            request = angular.copy(this.target);
        }

        // Save transformation
        this.downloadUrl = null;
        this.error = null;
        this.loading = true;
        this.saveSubscription = this.VisualQuerySaveService.save(request, this.engine)
            .subscribe(response => {
                    this.loading = false;
                    if (response.status === SaveResponseStatus.SUCCESS && this.destination === "DOWNLOAD") {
                        this.downloadId = response.id;
                        this.downloadUrl = response.location;
                    }
                },
                response => {
                    this.error = response.message;
                    this.loading = false;
                },
                () => this.loading = false);
    }
}

angular.module(require("feed-mgr/visual-query/module-name"))
    .component("thinkbigVisualQueryStore", {
        bindings: {
            engine: "=",
            model: "=",
            stepIndex: "@"
        },
        controller: VisualQueryStoreComponent,
        controllerAs: "$st",
        require: {
            stepperController: "^thinkbigStepper"
        },
        templateUrl: "js/feed-mgr/visual-query/store/store.component.html"
    });
