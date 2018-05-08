var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "@covalent/core/data-table", "@covalent/core/dialogs", "@covalent/core/loading", "@uirouter/angular", "rxjs/operators/finalize", "../catalog/services/catalog.service"], function (require, exports, core_1, data_table_1, dialogs_1, loading_1, angular_1, finalize_1, catalog_service_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Displays the available connectors and creates new data sets.
     */
    var ConnectorsComponent = /** @class */ (function () {
        function ConnectorsComponent(catalog, dataTable, dialog, loading, state) {
            this.catalog = catalog;
            this.dataTable = dataTable;
            this.dialog = dialog;
            this.loading = loading;
            this.state = state;
        }
        ConnectorsComponent_1 = ConnectorsComponent;
        ConnectorsComponent.prototype.ngOnInit = function () {
            this.filter();
        };
        ConnectorsComponent.prototype.search = function (term) {
            this.searchTerm = term;
            this.filter();
        };
        /**
         * Creates a new data set from the specified connector.
         */
        ConnectorsComponent.prototype.selectConnector = function (connector) {
            var _this = this;
            this.loading.register(ConnectorsComponent_1.LOADER);
            this.catalog.createDataSet(connector)
                .pipe(finalize_1.finalize(function () { return _this.loading.resolve(ConnectorsComponent_1.LOADER); }))
                .subscribe(function (dataSet) { return _this.state.go(".dataset", { dataSetId: dataSet.id }); }, function () { return _this.dialog.openAlert({ message: "The connector is not available" }); });
        };
        /**
         * Updates filteredConnectors by filtering availableConnectors.
         */
        ConnectorsComponent.prototype.filter = function () {
            var filteredConnectors = this.availableConnectors.filter(function (connector) { return connector.hidden !== true; });
            filteredConnectors = this.dataTable.filterData(filteredConnectors, this.searchTerm, true);
            filteredConnectors = this.dataTable.sortData(filteredConnectors, "title");
            this.filteredConnectors = filteredConnectors;
        };
        ConnectorsComponent.LOADER = "connectorsLoader";
        __decorate([
            core_1.Input("connectors"),
            __metadata("design:type", Array)
        ], ConnectorsComponent.prototype, "availableConnectors", void 0);
        ConnectorsComponent = ConnectorsComponent_1 = __decorate([
            core_1.Component({
                selector: "explorer-connectors",
                styleUrls: ["js/feed-mgr/explorer/connectors/connectors.component.css"],
                templateUrl: "js/feed-mgr/explorer/connectors/connectors.component.html"
            }),
            __metadata("design:paramtypes", [catalog_service_1.CatalogService, data_table_1.TdDataTableService, dialogs_1.TdDialogService, loading_1.TdLoadingService, angular_1.StateService])
        ], ConnectorsComponent);
        return ConnectorsComponent;
        var ConnectorsComponent_1;
    }());
    exports.ConnectorsComponent = ConnectorsComponent;
});
//# sourceMappingURL=connectors.component.js.map