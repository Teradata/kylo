var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "angular", "../wrangler/api/rest-model", "../wrangler/query-engine"], function (require, exports, core_1, angular, rest_model_1, query_engine_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var VisualQueryStoreComponent = /** @class */ (function () {
        function VisualQueryStoreComponent($http, DatasourcesService, RestUrlService, VisualQuerySaveService) {
            this.$http = $http;
            this.DatasourcesService = DatasourcesService;
            this.RestUrlService = RestUrlService;
            this.VisualQuerySaveService = VisualQuerySaveService;
            /**
             * List of Kylo data sources
             */
            this.kyloDataSources = [];
            /**
             * Indicates the page is loading
             */
            this.loading = true;
            /**
             * Output configuration
             */
            this.target = {};
        }
        ;
        VisualQueryStoreComponent.prototype.$onDestroy = function () {
            this.ngOnDestroy();
        };
        VisualQueryStoreComponent.prototype.$onInit = function () {
            this.ngOnInit();
        };
        /**
         * Release resources when component is destroyed.
         */
        VisualQueryStoreComponent.prototype.ngOnDestroy = function () {
            if (this.subscription) {
                this.subscription.unsubscribe();
            }
        };
        /**
         * Initialize resources when component is initialized.
         */
        VisualQueryStoreComponent.prototype.ngOnInit = function () {
            var _this = this;
            // Get list of Kylo data sources
            var kyloSourcesPromise = Promise.all([this.engine.getNativeDataSources(), this.DatasourcesService.findAll()])
                .then(function (resultList) {
                _this.kyloDataSources = resultList[0].concat(resultList[1]);
                if (_this.model.$selectedDatasourceId) {
                    _this.target.jdbc = _this.kyloDataSources.find(function (datasource) { return datasource.id === _this.model.$selectedDatasourceId; });
                }
            });
            // Get list of Spark data sources
            var sparkSourcesPromise = this.$http.get(this.RestUrlService.SPARK_SHELL_SERVICE_URL + "/data-sources")
                .then(function (response) {
                _this.formats = response.data.sort();
            });
            // Wait for completion
            Promise.all([kyloSourcesPromise, sparkSourcesPromise])
                .then(function () { return _this.loading = false; }, function () { return _this.error = "Invalid response from server."; });
        };
        /**
         * Downloads the saved results.
         */
        VisualQueryStoreComponent.prototype.download = function () {
            window.open(this.downloadUrl, "_blank");
        };
        /**
         * Find tables matching the specified name.
         */
        VisualQueryStoreComponent.prototype.findTables = function (name) {
            var _this = this;
            var tables = [];
            if (this.target.jdbc) {
                tables = this.engine.searchTableNames(name, this.target.jdbc.id);
                if (tables instanceof Promise) {
                    tables = tables.then(function (response) {
                        _this.form.datasource.$setValidity("connectionError", true);
                        return response;
                    }, function () {
                        _this.form.datasource.$setValidity("connectionError", false);
                        return [];
                    });
                }
                else {
                    this.form.datasource.$setValidity("connectionError", true);
                }
            }
            return tables;
        };
        /**
         * Saves the results.
         */
        VisualQueryStoreComponent.prototype.save = function () {
            var _this = this;
            // Remove current subscription
            if (this.subscription) {
                this.subscription.unsubscribe();
                this.subscription = null;
            }
            // Build request
            var request;
            if (this.destination === "DOWNLOAD") {
                request = {
                    format: this.target.format
                };
            }
            else {
                request = angular.copy(this.target);
            }
            // Save transformation
            this.downloadUrl = null;
            this.error = null;
            this.loading = true;
            this.subscription = this.VisualQuerySaveService.save(request, this.engine)
                .subscribe(function (response) {
                _this.loading = false;
                if (response.status === rest_model_1.SaveResponseStatus.SUCCESS && _this.destination === "DOWNLOAD") {
                    _this.downloadUrl = response.location;
                }
            }, function (response) {
                _this.error = response.message;
                _this.loading = false;
            }, function () { return _this.loading = false; });
        };
        VisualQueryStoreComponent.$inject = ["$http", "DatasourcesService", "RestUrlService", "VisualQuerySaveService"];
        __decorate([
            core_1.Input(),
            __metadata("design:type", query_engine_1.QueryEngine)
        ], VisualQueryStoreComponent.prototype, "engine", void 0);
        __decorate([
            core_1.Input(),
            __metadata("design:type", Object)
        ], VisualQueryStoreComponent.prototype, "model", void 0);
        return VisualQueryStoreComponent;
    }());
    exports.VisualQueryStoreComponent = VisualQueryStoreComponent;
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
});
//# sourceMappingURL=store.component.js.map