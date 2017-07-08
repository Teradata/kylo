define(['angular', "search/module-name"], function (angular, moduleName) {

    var controller = function ($scope, $sce, $http, $mdToast, $mdDialog, SearchService, Utils, CategoriesService, StateService, FeedService) {

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

        /**
         * Pagination data
         * @type {{rowsPerPage: number, currentPage: number, rowsPerPageOptions: [*]}}
         */
        this.paginationData = {rowsPerPage: 10, currentPage: 1, rowsPerPageOptions: ['5', '10', '20', '50', '100']};

        var supportedDisplays = ['tweets', 'feed', 'table-metadata', 'hive-query', 'index-pattern'];

        this.categoryForIndex = function (indexName) {
            var category = CategoriesService.findCategoryByName(indexName);
            if (category != null) {
                return category;
            }
            return null;
        };

        $scope.$watch(function () {
            return SearchService.searchQuery
        }, function (newVal) {
            self.search();
        });

        this.search = function () {
            var search = true;
            //changing the paginationData.currentPAge will trigger a search.
            //only search if they page was not already 1

            if (self.paginationData.currentPage > 1) {
                search = false;
            }
            self.paginationData.currentPage = 1;
            if (search) {
                self._search();
            }
        };
        this._search = function () {
            self.searchError = null;
            self.searching = true;
            var limit = self.paginationData.rowsPerPage;
            var start = (limit * self.paginationData.currentPage) - limit; //self.query.page(self.selectedTab));

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
            self.paginationData.currentPage = page;
            self._search();
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
            return $sce.trustAsHtml(Utils.maskProfanity(htmlCode));
        };

        this.isGenericSearchHitDisplay = function (searchHit) {
            return !_.contains(supportedDisplays, searchHit.type);
        }
    };

    angular.module(moduleName).controller('SearchController',
        ["$scope", "$sce", "$http", "$mdToast", "$mdDialog", "SearchService", "Utils", "CategoriesService", "StateService", "FeedService", controller]);
});



