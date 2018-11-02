import * as angular from 'angular';
import {moduleName} from "../module-name";
import * as _ from 'underscore';
import {Transition} from "@uirouter/core";
import SearchService from "../../services/SearchService";
import {Utils} from "../../services/Utils";
import {CategoriesService} from "../../feed-mgr/services/CategoriesService";
import {StateService} from "../../services/StateService";
import {FeedService} from "../../feed-mgr/services/FeedService";
import {DefaultPaginationDataService} from "../../services/PaginationDataService";
import {DatasourcesService} from "../../feed-mgr/services/DatasourcesService";
import "../module";
import "../module-require";

export class SearchController implements ng.IComponentController{
$transition$: Transition;
/**
 * The result object of what is returned from the search query
 * @type {null}
 */
searchResult: any = null;
/**
 * flag to indicate we are querying/searching
 * @type {boolean}
 */
searching: boolean = false;
resetPaging = this.$transition$.params().bcExclude_globalSearchResetPaging || false;
//Page Name
pageName: string = "search";
/**
 * Pagination data
 * @type {{rowsPerPage: number, currentPage: number, rowsPerPageOptions: [*]}}
 */
paginationData: any =  this.PaginationDataService.paginationData(this.pageName, this.pageName,10);
hiveDatasource: any = this.DatasourcesService.getHiveDatasource();
currentPage: any = this.PaginationDataService.currentPage(this.pageName) || 1;
categoryForIndex = (indexName: any)=> {
    var category = this.CategoriesService.findCategoryByName(indexName);
    if (category != null) {
        return category;
    }
    return null;
};

static readonly $inject = ["$scope","$sce","$http","$mdToast","$mdDialog","SearchService","Utils","CategoriesService","StateService", "FeedService","PaginationDataService","DatasourcesService"];
constructor(private $scope: angular.IScope, 
            private $sce: angular.ISCEService,
            private $http: angular.IHttpService,
            private $mdToast: angular.material.IToastService,
            private $mdDialog: angular.material.IDialogService, 
            //private $transition$: any,
            private SearchService: SearchService,
            private Utils: Utils,
            private CategoriesService: CategoriesService,
            private StateService: StateService,
            private FeedService: FeedService,
            private PaginationDataService: DefaultPaginationDataService,
            private DatasourcesService: DatasourcesService){
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
         }

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
        let feedSystemName: any;
        let categorySystemName: any;

        switch (result.type) {
            case "KYLO_DATA":
                categorySystemName = result.schemaName;
                feedSystemName = result.tableName;
                this.FeedService.getFeedByName(categorySystemName + "." + feedSystemName)
                    .then((feed: any) => {
                        this.StateService.FeedManager().Feed().navigateToFeedDetails(feed.id);
                    });
                break;

            case "KYLO_SCHEMA":
                categorySystemName = result.databaseName;
                feedSystemName = result.tableName;
                this.FeedService.getFeedByName(categorySystemName + "." + feedSystemName)
                    .then((feed: any) => {
                        this.StateService.FeedManager().Feed().navigateToFeedDetails(feed.id);
                    });
                break;

            case "KYLO_FEEDS":
                this.StateService.FeedManager().Feed().navigateToFeedDetails(result.feedId);
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

const module = angular.module(moduleName)
  .component("searchController", { 
        bindings: {
            $transition$: '<'
        },
        controller: SearchController,
        controllerAs: "vm",
        templateUrl: "./search.html"
    });
export default module;
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