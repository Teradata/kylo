import * as angular from 'angular';
import * as _ from 'underscore';
import SearchService from "../../services/SearchService";
import CategoriesService from "../../feed-mgr/services/CategoriesService";
import StateServices from "../../services/StateService";
import {FeedService} from "../../feed-mgr/services/FeedService";
import {DefaultPaginationDataService} from "../../services/PaginationDataService";
import { DatasourcesService } from "../../feed-mgr/services/DatasourcesService";
import { Component, Inject, ViewContainerRef, OnInit } from '@angular/core';
import { StateService } from '@uirouter/core';
import { TdDialogService } from '@covalent/core/dialogs';
import { SLIDE_TOGGLE_INPUT_CONTROL_VALUE_ACCESSOR } from '@covalent/dynamic-forms';
import { Subscription } from 'rxjs/Subscription';

@Component({
    templateUrl: 'js/search/common/search.html'
})
export class SearchComponent implements OnInit {
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
    resetPaging = this.transition.params.bcExclude_globalSearchResetPaging || false;
    //Page Name
    pageName: string = "search";
    /**
     * Pagination data
     * @type {{rowsPerPage: number, currentPage: number, rowsPerPageOptions: [*]}}
     */
    paginationData: any =  this.PaginationDataService.paginationData(this.pageName, this.pageName,10);
    hiveDatasource: any = this.DatasourcesService.getHiveDatasource();
    currentPage: any = this.PaginationDataService.currentPage(this.pageName) || 1;
    pageSize: number = 2;

    subscription: Subscription;

    categoryForIndex = (indexName: any)=> {
        var category = this.CategoriesService.findCategoryByName(indexName);
        if (category != null) {
            return category;
        }
        return null;
    };

    constructor(private searchService: SearchService,
                private transition: StateService,
                private CategoriesService: CategoriesService,
                private StateService: StateServices,
                private FeedService: FeedService,
                private PaginationDataService: DefaultPaginationDataService,
                private DatasourcesService: DatasourcesService,
                private _dialogService: TdDialogService,
                private _viewContainerRef: ViewContainerRef,
                @Inject("$injector") private $injector: any) {}

    ngOnInit() {

        this.search(true);
        this.PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);

        this.subscription = this.searchService.searchQuerySubject.subscribe((searchQuery : string) => {
            if(searchQuery != this.searchService.getSearchQuery() || this.searchResult == null || this.resetPaging == true) {
                var resetCurrentPage = ((searchQuery != this.searchService.getSearchQuery()) || (this.resetPaging == true));
            }
                this.search(resetCurrentPage);
        });
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    search = (resetCurrentPage: any)=>{
        if(resetCurrentPage == undefined || resetCurrentPage == true) {
            this.PaginationDataService.currentPage(this.pageName, null, 1);
            this.currentPage = 1;
        }
        this._search();
    }

    _search() {
        this.searching = true;
        var limit = this.paginationData.rowsPerPage;
        var start = (limit * this.currentPage) - limit; //self.query.page(self.selectedTab));

        this.searchService.search(this.searchService.getSearchQuery(), limit, start)
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

            this._dialogService.openAlert({
                message: "There was an error while searching.<br/>'" + err,
                viewContainerRef: this._viewContainerRef,
                width: '300 px',
                title: 'Error Searching',
                closeButton: 'Got it!',
                ariaLabel: "Error Searching",
                closeOnNavigation: true,
                disableClose: false
            });
        });
    }

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
        return this.$injector.get("$sce").trustAsHtml(htmlCode);
    };

}

