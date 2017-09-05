define(['angular', "search/module-name"], function (angular, moduleName) {

    var controller = function ($scope, $sce, $http, $mdToast, $mdDialog, $transition$, SearchService, Utils, CategoriesService, StateService, FeedService,PaginationDataService) {

        var self = this;
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

        var resetPaging = $transition$.params().bcExclude_globalSearchResetPaging || false;

        //Pagination Data
        this.pageName = "search";
        /**
         * Pagination data
         * @type {{rowsPerPage: number, currentPage: number, rowsPerPageOptions: [*]}}
         */
        this.paginationData = PaginationDataService.paginationData(this.pageName, this.pageName,10);
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
        this.currentPage = PaginationDataService.currentPage(self.pageName) || 1;

        this.categoryForIndex = function (indexName) {
            var category = CategoriesService.findCategoryByName(indexName);
            if (category != null) {
                return category;
            }
            return null;
        };

        $scope.$watch(function () {
            return SearchService.searchQuery
        }, function (newVal,oldVal) {
            if(newVal != oldVal || self.searchResult == null || resetPaging == true) {
                var resetCurrentPage = ((newVal != oldVal) || (resetPaging == true));
                self.search(resetCurrentPage);
            }
        });

        this.search = function (resetCurrentPage) {
            if(resetCurrentPage == undefined || resetCurrentPage == true) {
                PaginationDataService.currentPage(self.pageName, null, 1);
                self.currentPage = 1;
            }
                self._search();
        };
        this._search = function () {
            self.searchError = null;
            self.searching = true;
            var limit = self.paginationData.rowsPerPage;
            var start = (limit * self.currentPage) - limit; //self.query.page(self.selectedTab));

            SearchService.search(SearchService.searchQuery, limit, start).then(function (result) {
                self.searchResult = result;
                self.searching = false;
            }, function (response) {
                self.searching = false;
                var err = response.data.message;
                var detailedMessage = response.data.developerMessage;
                if (detailedMessage) {
                    err += "<br/>" + detailedMessage;
                }
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title('Error Searching')
                        .htmlContent('There was an error while searching.<br/>' + err)
                        .ariaLabel('Error Searching')
                        .ok('Got it!')
                );
            });
        };

        this.onPaginationChange = function (page, limit) {
            var prevPage = PaginationDataService.currentPage(self.pageName);
            PaginationDataService.currentPage(self.pageName, null, page);
            self.currentPage = page;
            if(prevPage == undefined || prevPage != page) {
                self._search();
            }
        };

        this.cleanValue = function (str) {
            return retVal = str.replace('[', '').replace(']', '');
        };

        this.onSearchResultItemClick = function ($event, result) {

            switch (result.type) {
                case "KYLO_DATA":
                    StateService.Tables().navigateToTable(this.cleanValue(result.schemaName), this.cleanValue(result.tableName));
                    break;

                case "KYLO_SCHEMA":
                    StateService.Tables().navigateToTable(this.cleanValue(result.databaseName), this.cleanValue(result.tableName));
                    break;

                case "KYLO_FEEDS":
                    var category;
                    var feed;

                    CategoriesService.getCategoryById(this.cleanValue(result.feedCategoryId)).then(function (category1) {
                        category = category1;
                        FeedService.getFeedByName(category.systemName + "." + (result.feedSystemName.replace('[', '').replace(']', ''))).then(function (feed1) {
                            feed = feed1;
                            StateService.FeedManager().Feed().navigateToFeedDetails(feed.id);
                        });
                    });
                    break;

                case "KYLO_CATEGORIES":
                    CategoriesService.getCategoryBySystemName(this.cleanValue(result.categorySystemName)).then(function (category) {
                        StateService.Categories().navigateToCategoryDetails(category.id);
                    });
                    break;

                case "KYLO_UNKNOWN":
                    break;

                default:
                    break;
            }
        };

        this.renderHtml = function (htmlCode) {
            return $sce.trustAsHtml(htmlCode);
        };
    };

    angular.module(moduleName).controller('SearchController',
        ["$scope", "$sce", "$http", "$mdToast", "$mdDialog", "$transition$", "SearchService", "Utils", "CategoriesService", "StateService", "FeedService","PaginationDataService", controller]);
});



