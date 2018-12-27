import * as _ from 'underscore';
import {SearchService} from "../../services/SearchService";
import {CategoriesService} from "../../feed-mgr/services/CategoriesService";
import {StateService as StateServices} from "../../services/StateService";
import {FeedService} from "../../feed-mgr/services/FeedService";
import {DefaultPaginationDataService} from "../../services/PaginationDataService";
import { DatasourcesService } from "../../feed-mgr/services/DatasourcesService";
import { Component, Inject, ViewContainerRef, OnInit, OnDestroy } from '@angular/core';
import { StateService } from '@uirouter/core';
import { TdDialogService } from '@covalent/core/dialogs';
import { SLIDE_TOGGLE_INPUT_CONTROL_VALUE_ACCESSOR } from '@covalent/dynamic-forms';
import { Subscription } from 'rxjs/Subscription';

@Component({
    templateUrl: './search.html',
})
export class SearchComponent implements OnInit, OnDestroy {
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
    pageSize: number = 10;

    subscription: Subscription;

    categoryForIndex(indexName: any) {
        var category = this.CategoriesService.findCategoryByName(indexName);
        if (category != null) {
            return category;
        }
        return null;
    };

    constructor(private searchService: SearchService,
                private transition: StateService,
                private CategoriesService: CategoriesService,
                private StateService: StateService,
                private FeedService: FeedService,
                private PaginationDataService: DefaultPaginationDataService,
                private DatasourcesService: DatasourcesService,
                private _dialogService: TdDialogService,
                private _viewContainerRef: ViewContainerRef,
                private stateServices: StateServices) {}

    ngOnInit() {

        this.search(true);

        this.subscription = this.searchService.searchQuerySubject.subscribe((searchQuery : string) => {
            if(searchQuery != this.searchService.getSearchQuery() || this.searchResult == null || this.resetPaging == true) {
                var resetCurrentPage = ((searchQuery != this.searchService.getSearchQuery()) || (this.resetPaging == true));
            }
                this.search(resetCurrentPage);
        });
    }

    ngOnDestroy() {
        if(this.subscription){
            this.subscription.unsubscribe();
        }
    }

    search (resetCurrentPage: any) {
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

        this.searchService.search(limit, start)
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
        this.currentPage = page.page;
        this.paginationData.rowsPerPage = page.pageSize;
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
                        this.stateServices.FeedManager().Feed().navigateToFeedDetails(feed.id);
                });
                break;

            case "KYLO_SCHEMA":
                categorySystemName = result.databaseName;
                feedSystemName = result.tableName;
                this.FeedService.getFeedByName(categorySystemName + "." + feedSystemName)
                    .then((feed: any) => {
                        this.stateServices.FeedManager().Feed().navigateToFeedDetails(feed.id);
                });
                break;

            case "KYLO_FEEDS":
                this.stateServices.FeedManager().Feed().navigateToFeedDetails(result.feedId);
                break;

            case "KYLO_CATEGORIES":
                this.CategoriesService.getCategoryBySystemName(this.cleanValue(result.categorySystemName))
                                      .then((category: any)=> {
                                        this.stateServices.Categories().navigateToCategoryDetails(category.id);
                });
                break;

            case "KYLO_UNKNOWN":
                break;

            default:
                break;
        }
    };

    renderHtml(htmlCode: any): any {
        return htmlCode;
    };

}

