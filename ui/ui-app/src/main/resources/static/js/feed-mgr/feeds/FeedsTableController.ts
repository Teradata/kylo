import * as angular from 'angular';
import * as _ from 'underscore';
import 'pascalprecht.translate';
const moduleName = require('./module-name');
export default class FeedsTableController implements ng.IComponentController {

    allowExport:boolean = false;
    feedData:any = []
    loading: boolean = false;
    loaded:boolean = false;
    cardTitle = "";
    
    //Pagination DAta
    pageName:string = "feeds";
    paginationData:any = this.PaginationDataService.paginationData(this.pageName);
    paginationId: string = 'feeds';
    currentPage: any = this.PaginationDataService.currentPage(this.pageName) || 1;
    viewType: any = this.PaginationDataService.viewType(this.pageName);
    sortOptions: any = this.loadSortOptions();
    filter:  any = null;
    



    constructor(
        private $scope: angular.IScope,
        private $http: any,
        private AccessControlService: any,
        private RestUrlService:any,
        private PaginationDataService: any,
        private TableOptionsService: any,
        private AddButtonService: any,
        private FeedService: any,
        private StateService: any,
        public $filter: any,
        private EntityAccessControlService: any,
    ){

        // Register Add button
        AccessControlService.getUserAllowedActions()
        .then((actionSet:any) => {
            if (AccessControlService.hasAction(AccessControlService.FEEDS_EDIT, actionSet.actions)) {
                AddButtonService.registerAddButton("feeds", () => {
                    FeedService.resetFeed();
                    StateService.FeedManager().Feed().navigateToDefineFeed()
                });
            }
        });
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);

        this.filter = PaginationDataService.filter(this.pageName);
        this.cardTitle = $filter('translate')('views.main.feeds-title');
        $scope.$watch(() => {
            return this.viewType;
        }, (newVal) => {
            this.onViewTypeChange(newVal);
        })

        $scope.$watch(() => {
            return this.filter;
        },  (newVal, oldValue) => {
            if (newVal != oldValue || (!this.loaded && !this.loading)) {
                PaginationDataService.filter(this.pageName, newVal)
                this.getFeeds();
            }
        })


        // Fetch the allowed actions
        AccessControlService.getUserAllowedActions()
        .then((actionSet:any) => {
            this.allowExport = AccessControlService.hasAction(AccessControlService.FEEDS_EXPORT, actionSet.actions);
        });

        //rebind this controller to the onOrderChange function
        //https://github.com/daniel-nagy/md-data-table/issues/616
        this.onOrderChange = this.onOrderChange.bind(this);

        this.selectedTableOption = this.selectedTableOption.bind(this);

        this.onDataTablePaginationChange = this.onDataTablePaginationChange.bind(this)

    }

    onViewTypeChange(viewType:any) {
        this.PaginationDataService.viewType(this.pageName, this.viewType);
    }

    onOrderChange(order:any) {
        this.TableOptionsService.setSortOption(this.pageName, order);
        this.getFeeds();
    };

    onPaginate(page:any,limit:number){
        this.PaginationDataService.currentPage(this.pageName, null, page);
        this.currentPage = page;
        //only trigger the reload if the initial page has been loaded.
        //md-data-table will call this function when the page initially loads and we dont want to have it run the query again.\
        if (this.loaded) {
            this.getFeeds();
        }
    }

    onPaginationChange(page:any, limit:number) {
        if(this.viewType == 'list') {
            this.onPaginate(page,limit);
        }

    };

    onDataTablePaginationChange(page:any, limit:number) {
        if(this.viewType == 'table') {
            this.onPaginate(page,limit);
        }

    };



    /**
     * Called when a user Clicks on a table Option
     * @param option
     */
    selectedTableOption(option:any) {
        var sortString = this.TableOptionsService.toSortString(option);
        var savedSort = this.PaginationDataService.sort(this.pageName, sortString);
        var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
        this.TableOptionsService.setSortOption(this.pageName, sortString);
        this.getFeeds();
    }

    /**
     * Build the possible Sorting Options
     * @returns {*[]}
     */
    loadSortOptions() {
        var options = {'Feed': 'feedName', 'State': 'state', 'Category': 'category.name', 'Last Modified': 'updateDate'};
        var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'updateDate', 'desc');
        this.TableOptionsService.initializeSortOption(this.pageName);
        return sortOptions;
    }

    feedDetails = ($event:any, feed:any)=> {
        if(feed !== undefined) {
            this.StateService.FeedManager().Feed().navigateToFeedDetails(feed.id);
        }
    }

    getFeeds() {
        this.loading = true;

        var successFn = (response:any)=> {
            this.loading = false;
            if (response.data) {
                this.feedData = this.populateFeeds(response.data.data);
                this.PaginationDataService.setTotal(this.pageName,response.data.recordsFiltered);
                this.loaded = true;
            } else {
                this.feedData = [];
            }
        }
        
        var errorFn = (err:any)=> {
            this.loading = false;
            this.loaded = true;
        }

        var limit = this.PaginationDataService.rowsPerPage(this.pageName);
        var start = limit == 'All' ? 0 : (limit * this.currentPage) - limit;
        var sort = this.paginationData.sort;
        var filter = this.paginationData.filter;
        var params = {start: start, limit: limit, sort: sort, filter: filter};
        
        var promise = this.$http.get(this.RestUrlService.GET_FEEDS_URL, {params: params});
        promise.then(successFn, errorFn);
        return promise;
    }

    populateFeeds(feeds:any) {
        var entityAccessControlled = this.AccessControlService.isEntityAccessControlled();
        var simpleFeedData:any = [];
        
        angular.forEach(feeds, (feed)=> {
            if (feed.state == 'ENABLED') {
                feed.stateIcon = 'check_circle'
            } else {
                feed.stateIcon = 'block'
            }
            simpleFeedData.push({
                templateId: feed.templateId,
                templateName: feed.templateName,
                exportUrl: this.RestUrlService.ADMIN_EXPORT_FEED_URL + "/" + feed.id,
                id: feed.id,
                active: feed.active,
                state: feed.state,
                stateIcon: feed.stateIcon,
                feedName: feed.feedName,
                category: {name: feed.categoryName, icon: feed.categoryIcon, iconColor: feed.categoryIconColor},
                updateDate: feed.updateDate,
                allowEditDetails: !entityAccessControlled || this.FeedService.hasEntityAccess(this.EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS, feed),
                allowExport: !entityAccessControlled || this.FeedService.hasEntityAccess(this.EntityAccessControlService.ENTITY_ACCESS.FEED.EXPORT, feed)
            })
        });
        
        return simpleFeedData;
    }



}





    angular.module(moduleName)
        .controller('FeedsTableController',
            ["$scope","$http","AccessControlService","RestUrlService","PaginationDataService",
            "TableOptionsService","AddButtonService","FeedService","StateService", '$filter', "EntityAccessControlService", 
            FeedsTableController]);


