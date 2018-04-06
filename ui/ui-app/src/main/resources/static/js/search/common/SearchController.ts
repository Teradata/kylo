import * as angular from 'angular';
import {moduleName} from "../module-name";
//const moduleName = require('../module-name');
import * as _ from 'underscore';

export class controller implements ng.IComponentController{
        /**
         * The result object of what is returned from the search query
         * @type {null}
         */
        searchResult: any = null;

        /**
         * flag to indicate we are querying/searching
         * @type {boolean}
         */
        searching: any = false;

        resetPaging = this.$transition$.params().bcExclude_globalSearchResetPaging || false;

        //Pagination Data
        pageName: any = "search";
        /**
         * Pagination data
         * @type {{rowsPerPage: number, currentPage: number, rowsPerPageOptions: [*]}}
         */
        paginationData: any =  this.PaginationDataService.paginationData(this.pageName, this.pageName,10);
        //this.getPaginatedData();

    /*    getPaginatedData () {
            this.PaginationDataService.paginationData(this.pageName, this.pageName,10);
            this.PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
        }*/
     //  this.PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
     
        hiveDatasource: any = this.DatasourcesService.getHiveDatasource();
      
        currentPage: any = this.PaginationDataService.currentPage(this.pageName) || 1;

          categoryForIndex = (indexName: any)=> {
            var category = this.CategoriesService.findCategoryByName(indexName);
            if (category != null) {
                return category;
            }
            return null;
        };

constructor(private $scope: any, 
            private $sce: any,
            private $http: any,
            private $mdToast: any,
            private $mdDialog: any, 
            private $transition$: any,
            private SearchService: any,
            private Utils: any,
            private CategoriesService: any,
            private StateService: any,
            private FeedService: any,
            private PaginationDataService: any,
            private DatasourcesService: any){
        
                this.$scope.$watch(() => {
                    return this.SearchService.searchQuery;
                }, (newVal: any,oldVal: any) => {
                        if(newVal != oldVal || this.searchResult == null || this.resetPaging == true) {
                        var resetCurrentPage = ((newVal != oldVal) || (this.resetPaging == true));
                        this.search(resetCurrentPage);
                    }
                });
       
           this.PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
       }
        search = (resetCurrentPage: any)=>{
            if(resetCurrentPage == undefined || resetCurrentPage == true) {
                this.PaginationDataService.currentPage(this.pageName, null, 1);
                this.currentPage = 1;
            }
                this._search();
         };

            _search = function () {
            this.searchError = null;
            this.searching = true;
            var limit = this.paginationData.rowsPerPage;
            var start = (limit * this.currentPage) - limit; //self.query.page(self.selectedTab));

            this.SearchService.search(this.SearchService.searchQuery, limit, start)
                              .then((result: any)=> {
                                    this.searchResult = result;
                                    this.searching = false;
                                }, (response: any) =>{
                                    this.searching = false;
                                    var err = response.data.message;
                                    var detailedMessage = response.data.developerMessage;
                                    if (detailedMessage) {
                                        err += "<br/>" + detailedMessage;
                                }
                this.$mdDialog.show(
                    this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title('Error Searching')
                        .htmlContent('There was an error while searching.<br/>' + err)
                        .ariaLabel('Error Searching')
                        .ok('Got it!')
                );
            });
        };

  onPaginationChange(page: any, limit: any) {
            var prevPage = this.PaginationDataService.currentPage(this.pageName);
            this.PaginationDataService.currentPage(this.pageName, null, page);
            this.currentPage = page;
            if(prevPage == undefined || prevPage != page) {
                this._search();
            }
        };

  cleanValue(str: any): any {
            return str.replace('[', '').replace(']', '');
        };

    onSearchResultItemClick($event: any, result: any) {
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
                                          .then((category1: any)=> {
                                                category = category1;
                                                this.FeedService.getFeedByName(category.systemName + "." + (result.feedSystemName.replace('[', '').replace(']', '')))
                                                                .then((feed1: any) =>{
                                                                    feed = feed1;
                                                                    this.StateService.FeedManager().Feed().navigateToFeedDetails(feed.id);
                                                                });
                    });
                    break;

                case "KYLO_CATEGORIES":
                    this.CategoriesService.getCategoryBySystemName(this.cleanValue(result.categorySystemName))
                                          .then((category: any)=> {
                                            this.StateService.Categories().navigateToCategoryDetails(category.id);
                    });
                    break;

                case "KYLO_UNKNOWN":
                    break;

                default:
                    break;
            }
        };

    renderHtml(htmlCode: any): any{
            return this.$sce.trustAsHtml(htmlCode);
        };

}

angular.module(moduleName).controller('SearchController',
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
                        controller]);