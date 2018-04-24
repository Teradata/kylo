define(["require", "exports", "angular", "../module-name", "../module", "../module-require"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $sce, $http, $mdToast, $mdDialog, 
            //private $transition$: any,
            SearchService, Utils, CategoriesService, StateService, FeedService, PaginationDataService, DatasourcesService) {
            var _this = this;
            this.$scope = $scope;
            this.$sce = $sce;
            this.$http = $http;
            this.$mdToast = $mdToast;
            this.$mdDialog = $mdDialog;
            this.SearchService = SearchService;
            this.Utils = Utils;
            this.CategoriesService = CategoriesService;
            this.StateService = StateService;
            this.FeedService = FeedService;
            this.PaginationDataService = PaginationDataService;
            this.DatasourcesService = DatasourcesService;
            /**
             * The result object of what is returned from the search query
             * @type {null}
             */
            this.searchResult = null;
            /**
             * flag to indicate we are querying/searching
             * @type {boolean}
             */
            this.searching = false;
            this.resetPaging = this.$transition$.params().bcExclude_globalSearchResetPaging || false;
            //Page Name
            this.pageName = "search";
            /**
             * Pagination data
             * @type {{rowsPerPage: number, currentPage: number, rowsPerPageOptions: [*]}}
             */
            this.paginationData = this.PaginationDataService.paginationData(this.pageName, this.pageName, 10);
            this.hiveDatasource = this.DatasourcesService.getHiveDatasource();
            this.currentPage = this.PaginationDataService.currentPage(this.pageName) || 1;
            this.categoryForIndex = function (indexName) {
                var category = _this.CategoriesService.findCategoryByName(indexName);
                if (category != null) {
                    return category;
                }
                return null;
            };
            this.search = function (resetCurrentPage) {
                if (resetCurrentPage == undefined || resetCurrentPage == true) {
                    _this.PaginationDataService.currentPage(_this.pageName, null, 1);
                    _this.currentPage = 1;
                }
                _this._search();
            };
            this._search = function () {
                var _this = this;
                this.searchError = null;
                this.searching = true;
                var limit = this.paginationData.rowsPerPage;
                var start = (limit * this.currentPage) - limit; //self.query.page(self.selectedTab));
                this.SearchService.search(this.SearchService.searchQuery, limit, start)
                    .then(function (result) {
                    _this.searchResult = result;
                    _this.searching = false;
                }, function (response) {
                    _this.searching = false;
                    var err = response.data.message;
                    var detailedMessage = response.data.developerMessage;
                    if (detailedMessage) {
                        err += "<br/>" + detailedMessage;
                    }
                    _this.$mdDialog.show(_this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title('Error Searching')
                        .htmlContent('There was an error while searching.<br/>' + err)
                        .ariaLabel('Error Searching')
                        .ok('Got it!'));
                });
            };
            this.$scope.$watch(function () {
                return _this.SearchService.searchQuery;
            }, function (newVal, oldVal) {
                if (newVal != oldVal || _this.searchResult == null || _this.resetPaging == true) {
                    var resetCurrentPage = ((newVal != oldVal) || (_this.resetPaging == true));
                    _this.search(resetCurrentPage);
                }
            });
            this.PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
        }
        controller.prototype.onPaginationChange = function (page, limit) {
            var prevPage = this.PaginationDataService.currentPage(this.pageName);
            this.PaginationDataService.currentPage(this.pageName, null, page);
            this.currentPage = page;
            if (prevPage == undefined || prevPage != page) {
                this._search();
            }
        };
        ;
        controller.prototype.cleanValue = function (str) {
            return str.replace('[', '').replace(']', '');
        };
        ;
        controller.prototype.onSearchResultItemClick = function ($event, result) {
            var _this = this;
            switch (result.type) {
                case "KYLO_DATA":
                    this.StateService.Tables().navigateToTable(this.hiveDatasource.id, this.cleanValue(result.schemaName), this.cleanValue(result.tableName));
                    break;
                case "KYLO_SCHEMA":
                    this.StateService.Tables().navigateToTable(this.hiveDatasource.id, this.cleanValue(result.databaseName), this.cleanValue(result.tableName));
                    break;
                case "KYLO_FEEDS":
                    var category;
                    var feed;
                    this.CategoriesService.getCategoryById(this.cleanValue(result.feedCategoryId))
                        .then(function (category1) {
                        category = category1;
                        _this.FeedService.data.getFeedByName(category.systemName + "." + (result.feedSystemName.replace('[', '').replace(']', '')))
                            .then(function (feed1) {
                            feed = feed1;
                            _this.StateService.FeedManager().Feed().navigateToFeedDetails(feed.id);
                        });
                    });
                    break;
                case "KYLO_CATEGORIES":
                    this.CategoriesService.getCategoryBySystemName(this.cleanValue(result.categorySystemName))
                        .then(function (category) {
                        _this.StateService.Categories().navigateToCategoryDetails(category.id);
                    });
                    break;
                case "KYLO_UNKNOWN":
                    break;
                default:
                    break;
            }
        };
        ;
        controller.prototype.renderHtml = function (htmlCode) {
            return this.$sce.trustAsHtml(htmlCode);
        };
        ;
        controller.$inject = ["$scope", "$sce", "$http", "$mdToast", "$mdDialog", "SearchService", "Utils", "CategoriesService", "StateService", "FeedService", "PaginationDataService", "DatasourcesService"];
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName)
        .component("searchController", {
        bindings: {
            $transition$: '<'
        },
        controller: controller,
        controllerAs: "vm",
        templateUrl: "js/search/common/search.html"
    });
});
/*angular.module(moduleName).controller('SearchController',
                        ["$scope",
                        "$sce",
                        "$http",
                        "$mdToast",
                        "$mdDialog",
                        "$transition$",
                        "SearchService",
                        "Utils",
                        "CategoriesService",
                        "StateService",
                        "FeedService",
                        "PaginationDataService",
                        "DatasourcesService",
                        controller]);*/ 
//# sourceMappingURL=SearchController.js.map