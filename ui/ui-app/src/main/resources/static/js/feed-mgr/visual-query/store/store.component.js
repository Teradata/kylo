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
    var SaveMode;
    (function (SaveMode) {
        SaveMode[SaveMode["INITIAL"] = 0] = "INITIAL";
        SaveMode[SaveMode["SAVING"] = 1] = "SAVING";
        SaveMode[SaveMode["SAVED"] = 2] = "SAVED";
    })(SaveMode = exports.SaveMode || (exports.SaveMode = {}));
    var VisualQueryStoreComponent = /** @class */ (function () {
        function VisualQueryStoreComponent($http, DatasourcesService, RestUrlService, VisualQuerySaveService) {
            var _this = this;
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
            this.saveMode = SaveMode.INITIAL;
            /**
             * Output configuration
             */
            this.target = {};
            // Listen for notification removals
            this.removeSubscription = this.VisualQuerySaveService.subscribeRemove(function (event) {
                if (event.id === _this.downloadId) {
                    _this.downloadId = null;
                    _this.downloadUrl = null;
                }
            });
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
            if (this.removeSubscription) {
                this.removeSubscription.unsubscribe();
            }
            if (this.saveSubscription) {
                this.saveSubscription.unsubscribe();
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
            // Clear download buttons
            this.VisualQuerySaveService.removeNotification(this.downloadId);
            this.downloadId = null;
            this.downloadUrl = null;
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
         * Should the Results card be shown, or the one showing the Download options
         * @return {boolean}
         */
        VisualQueryStoreComponent.prototype.showSaveResultsCard = function () {
            return this.saveMode == SaveMode.SAVING || this.saveMode == SaveMode.SAVED;
        };
        /**
         * Is a save for a file download in progress
         * @return {boolean}
         */
        VisualQueryStoreComponent.prototype.isSavingFile = function () {
            return this.saveMode == SaveMode.SAVING && this.destination === "DOWNLOAD";
        };
        /**
         * is a save for a table export in progress
         * @return {boolean}
         */
        VisualQueryStoreComponent.prototype.isSavingTable = function () {
            return this.saveMode == SaveMode.SAVING && this.destination === "TABLE";
        };
        /**
         * is a save for a table export complete
         * @return {boolean}
         */
        VisualQueryStoreComponent.prototype.isSavedTable = function () {
            return this.saveMode == SaveMode.SAVED && this.destination === "TABLE";
        };
        /**
         * have we successfully saved either to a file or table
         * @return {boolean}
         */
        VisualQueryStoreComponent.prototype.isSaved = function () {
            return this.saveMode == SaveMode.SAVED;
        };
        /**
         * Navigate back from the saved results card and show the download options
         */
        VisualQueryStoreComponent.prototype.downloadAgainAs = function () {
            this._reset();
        };
        /**
         * Navigate back and take the user from the Save/store screen to the Transform table screen
         */
        VisualQueryStoreComponent.prototype.modifyTransformation = function () {
            this._reset();
            var prevStep = this.stepperController.previousActiveStep(parseInt(this.stepIndex));
            if (angular.isDefined(prevStep)) {
                this.stepperController.selectedStepIndex = prevStep.index;
            }
        };
        /**
         * Reset download options
         * @private
         */
        VisualQueryStoreComponent.prototype._reset = function () {
            this.saveMode = SaveMode.INITIAL;
            this.downloadUrl = null;
            this.error = null;
            this.loading = false;
        };
        /**
         * Exit the Visual Query and go to the Feeds list
         */
        VisualQueryStoreComponent.prototype.exit = function () {
            this.stepperController.cancelStepper();
        };
        /**
         * Reset options when format changes.
         */
        VisualQueryStoreComponent.prototype.onFormatChange = function () {
            this.target.options = {};
        };
        /**
         * Saves the results.
         */
        VisualQueryStoreComponent.prototype.save = function () {
            var _this = this;
            this.saveMode = SaveMode.SAVING;
            // Remove current subscription
            if (this.saveSubscription) {
                this.saveSubscription.unsubscribe();
                this.saveSubscription = null;
            }
            // Build request
            var request;
            if (this.destination === "DOWNLOAD") {
                request = {
                    format: this.target.format,
                    options: this.getOptions()
                };
            }
            else {
                request = angular.copy(this.target);
                request.options = this.getOptions();
            }
            // Save transformation
            this.downloadUrl = null;
            this.error = null;
            this.loading = true;
            this.saveSubscription = this.VisualQuerySaveService.save(request, this.engine)
                .subscribe(function (response) {
                _this.loading = false;
                if (response.status === rest_model_1.SaveResponseStatus.SUCCESS && _this.destination === "DOWNLOAD") {
                    _this.downloadId = response.id;
                    _this.downloadUrl = response.location;
                    //reset the save mode if its Saving
                    if (_this.saveMode == SaveMode.SAVING) {
                        _this.saveMode = SaveMode.SAVED;
                    }
                }
            }, function (response) {
                _this.error = response.message;
                _this.loading = false;
                _this.saveMode = SaveMode.INITIAL;
            }, function () { return _this.loading = false; });
        };
        /**
         * Gets the target output options by parsing the properties object.
         */
        VisualQueryStoreComponent.prototype.getOptions = function () {
            var options = angular.copy(this.target.options);
            this.properties.forEach(function (property) { return options[property.systemName] = property.value; });
            return options;
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