import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from "underscore";
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";
import TabService from "../services/TabService";

export class AlertsTableController implements ng.IComponentController{
    pageName: any;
    loading: any;
    showProgress: any;
    paginationData: any;
    viewType: any;
    tabNames: any;
    tabs: any;
    tabMetadata: any;
    sortOptions: any;
    additionalMenuOptions: any;
    selectedAdditionalMenuOptionVar: any;
    filterAlertType: any;
    alertTypes: any;
    filterAlertState: any;
    alertStates: any;
    showCleared: any;
    filter: any;
    query: any;
    activeAlertRequests: any;
    newestTime: any;
    oldestTime: any;
    ALL_ALERT_TYPES_FILTER: any;
    direction: any;
    PAGE_DIRECTION: any;
    refreshing: any;
    deferred: any;
    promise: any;

constructor(private $scope: any,
            private $http: any,
            private $q: any,
            private TableOptionsService: any,
            private PaginationDataService: any, 
            private StateService: any,
            private TabService: any,
            private OpsManagerRestUrlService: any){
         this.pageName = angular.isDefined(this.pageName) ? this.pageName : 'alerts';
        //Page State
        this.loading = true;
        this.showProgress = true;
        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
        this.viewType = PaginationDataService.viewType(this.pageName);
        //Setup the Tabs
        this.tabNames = ['All', 'INFO', 'WARNING', 'MINOR', 'MAJOR', 'CRITICAL', 'FATAL'];
        this.tabs = TabService.registerTabs(this.pageName, this.tabNames, this.paginationData.activeTab);
        this.tabMetadata = TabService.metadata(this.pageName);
        this.sortOptions = this.loadSortOptions();
        this.PAGE_DIRECTION = {forward: 'f', backward: 'b', none: 'n'};
        this.additionalMenuOptions = this.loadAdditionalMenuOptions();
        this.selectedAdditionalMenuOptionVar = this.selectedAdditionalMenuOption;

        this.ALL_ALERT_TYPES_FILTER = {label:"ALL",type:""};
        this.filterAlertType = this.ALL_ALERT_TYPES_FILTER;

        this.alertTypes = [this.ALL_ALERT_TYPES_FILTER];

        var UNHANDLED_FILTER = {label:'UNHANDLED'};
        this.filterAlertState = UNHANDLED_FILTER;

        this.alertStates = [{label:'ALL'},{label:'HANDLED'},UNHANDLED_FILTER]
        this.initAlertTypes();
        this.showCleared = false;
        /**
         * The filter supplied in the page
         * @type {string}
         */
        this.filter =  angular.isDefined(this.query) ? this.query : '';

        $scope.$watch(() =>{
            return this.filter;
        },  (newVal: any, oldVal: any) =>{
            if (newVal != oldVal) {
                return this.loadAlerts(true).promise;
            }

        });
         /**
         * Array holding onto the active alert promises
         * @type {Array}
         */
        this.activeAlertRequests = [];

        /**
         * The time of the newest alert from the last server response.
         * @type {number|null}
         */
        this.newestTime = null;

        /**
         * The time of the oldest alert from the last server response.
         * @type {number|null}
         */


          $scope.$watch( ()=> {
            return this.viewType;
        },  (newVal: any)=> {
            this.onViewTypeChange(newVal);
        });

        /**
         * This will be called the first time the page loads and then whenever the filter changes.
         *
         */
        $scope.$watch( ()=> {
            return this.filter;
        },  (newVal: any)=> {
            return this.loadAlerts().promise;
        });

        this.oldestTime = null;
        $scope.$on('$destroy',  ()=> {
        });

} // end of constructor


        initAlertTypes=()=> {
            this.$http.get(this.OpsManagerRestUrlService.ALERT_TYPES).then((response: any)=> {
                this.alertTypes = this.alertTypes.concat(response.data);
            });
        }

        onFilterAlertTypeChange = (alertType: any)=>{
            this.loadAlerts(true);
        }

        onFilterAlertStateChange = (alertState: any)=>{
             this.loadAlerts(true);
        }

       

        paginationId =  (tab: any)=> {
            return this.PaginationDataService.paginationId(this.pageName, tab.title);
        };
        currentPage =  (tab: any) =>{
            return this.PaginationDataService.currentPage(this.pageName, tab.title);
        };

      
        onViewTypeChange =  (viewType: any)=> {
            this.PaginationDataService.viewType(this.pageName, this.viewType);
        };

        //Tab Functions

        onTabSelected = (tab: any) =>{
            this.newestTime = null;
            this.oldestTime = null;
            this.PaginationDataService.currentPage(this.pageName, tab.title, 1);
            this.TabService.selectedTab(this.pageName, tab);
            return this.loadAlerts().promise;
        };

        onOrderChange =  (order: any)=> {
            this.PaginationDataService.sort(this.pageName, order);
            this.TableOptionsService.setSortOption(this.pageName, order);
            return this.loadAlerts().promise;
            //return this.deferred.promise;
        };

        onPaginationChange =  (page: any, limit: any) =>{
            var activeTab = this.TabService.getActiveTab(this.pageName);
            var prevPage = this.PaginationDataService.currentPage(this.pageName, activeTab.title);
           
            // Current page number is only used for comparison in determining the direction, i.e. the value is not relevant.
            if (prevPage > page) {
                this.direction = this.PAGE_DIRECTION.backward;
            } else if (prevPage < page) {
            	this.direction = this.PAGE_DIRECTION.forward;
            } else {
            	this.direction = this.PAGE_DIRECTION.none;
            }

            this.PaginationDataService.currentPage(this.pageName, activeTab.title, page);
            return  this.loadAlerts(this.direction).promise;
        };


        //Sort Functions
        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        loadSortOptions=()=> {
            var options = {'Start Time': 'startTime', 'Level': 'level', 'State': 'state'};

            var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'startTime', 'desc');
            var currentOption = this.TableOptionsService.getCurrentSort(this.pageName);
            if (currentOption) {
                this.TableOptionsService.saveSortOption(this.pageName, currentOption)
            }
            return sortOptions;
        }

        /**
         * Loads the additional menu options that appear in the more_vert options
         * @returns {Array}
         */
        loadAdditionalMenuOptions=()=> {
            var options = [];
                options.push(this.TableOptionsService.newOption("Actions", 'actions_header', true, false))
                options.push(this.TableOptionsService.newOption("Show Cleared", 'showCleared', false, false));
            return options;
        }


        selectedAdditionalMenuOption=(item: any)=> {
            if (item.type == 'showCleared') {
                this.showCleared = !this.showCleared;
                if(this.showCleared) {
                    item.label = "Hide Cleared";
                }
                else {
                    item.label = "Show Cleared";
                }
                this.loadAlerts();
            }

        }


        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        selectedTableOption =  (option: any)=> {
            var sortString = this.TableOptionsService.toSortString(option);
            this.PaginationDataService.sort(this.pageName, sortString);
            var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
            this.TableOptionsService.setSortOption(this.pageName, sortString);
            this.loadAlerts();
        };



        //Load Alerts

        loadAlerts=(direction?: any)=> {
            if (direction == undefined) {
                direction = this.PAGE_DIRECTION.none;
            }

            if (!this.refreshing) {
                    //cancel any active requests
                    angular.forEach(this.activeAlertRequests,  (canceler: any, i: any)=> {
                        canceler.resolve();
                    });
                    this.activeAlertRequests = [];

                var activeTab = this.TabService.getActiveTab(this.pageName);

                this.refreshing = true;
                var sortOptions = '';
                var tabTitle = activeTab.title;
                var filters = {tabTitle: tabTitle};
                var limit = this.paginationData.rowsPerPage;
//                var start = start + limit;

                var sort = this.PaginationDataService.sort(this.pageName);
                var canceler = this.$q.defer();

                var successFn = (response: any)=> {
                    if (response.data) {
                        var alertRange = response.data;
                        var total = 0;

                        if (angular.isDefined(alertRange.size)) {
                            if (direction === this.PAGE_DIRECTION.forward || direction === this.PAGE_DIRECTION.none) {
                                total = (this.PaginationDataService.currentPage(this.pageName, activeTab.title) - 1) * this.PaginationDataService.rowsPerPage(this.pageName) + alertRange.size + 1;
                            } else {
                                total = this.PaginationDataService.currentPage(this.pageName, activeTab.title) * this.PaginationDataService.rowsPerPage(this.pageName) + 1;
                            }
                        } else {
                            total = (this.PaginationDataService.currentPage(this.pageName, activeTab.title) - 1) * this.PaginationDataService.rowsPerPage(this.pageName) + 1;
                        }

                        this.newestTime = angular.isDefined(alertRange.newestTime) ? alertRange.newestTime : 0;
                        this.oldestTime = angular.isDefined(alertRange.oldestTime) ? alertRange.oldestTime : 0;

                        //transform the data for UI
                        this.transformAlertData(tabTitle, angular.isDefined(alertRange.alerts) ? alertRange.alerts : []);
                        this.TabService.setTotal(this.pageName, tabTitle, total);

                        if (this.loading) {
                            this.loading = false;
                        }
                    }

                    this.finishedRequest(canceler);

                };
                var errorFn =  (err: any)=> {
                    this.finishedRequest(canceler);
                };

                this.activeAlertRequests.push(canceler);
                this.deferred = canceler;
                this.promise = this.deferred.promise;

                var filter = this.filter;

                var params: any = {limit: limit};

                // Get the next oldest or next newest alerts depending on paging direction.
                if (direction == this.PAGE_DIRECTION.forward) {
                	if (this.oldestTime !== null) {
                		// Filter alerts to those created before the oldest alert of the previous results
                		params.before = this.oldestTime;
                	}
                } else if (direction == this.PAGE_DIRECTION.backward) {
                	if (this.newestTime !== null) {
                		// Filter alerts to those created after the newest alert of the previous results
                		params.after = this.newestTime;
                	}
                } else {
                    if (this.newestTime !== null && this.newestTime !== 0) {
                        // Filter alerts to the current results
                        params.before = this.newestTime + 1;
                    }
                }
                params.cleared = this.showCleared;

                if (tabTitle != 'All') {
                	params.level=tabTitle;
                }
                if(filter != '') {
                    params.filter = filter;
                }

                if(this.filterAlertType.label != 'ALL'){
                    if(params.filter == undefined){
                        params.filter = this.filterAlertType.type;
                    }
                    else {
                        params.filter+=','+this.filterAlertType.type;
                    }
                }


                if(this.filterAlertState.label != 'ALL'){
                    params.state = this.filterAlertState.label;
                }


                this.$http.get(this.OpsManagerRestUrlService.ALERTS_URL, {timeout: canceler.promise, params: params}).then(successFn, errorFn);
            }
            this.showProgress = true;

            return this.deferred;

        }

        /**
         * Called when the Server finishes.
         * @param canceler
         */
        finishedRequest=(canceler: any)=> {
            var index = _.indexOf(this.activeAlertRequests, canceler);
            if (index >= 0) {
                this.activeAlertRequests.splice(index, 1);
            }
            canceler.resolve();
            canceler = null;
            this.refreshing = false;
            this.showProgress = false;
        }

        /**
         * Transform the array of alerts for the selected Tab coming from the server to the UI model
         * @param tabTitle
         * @param alerts
         */
        transformAlertData=(tabTitle: any, alerts: any)=> {
            //first clear out the arrays

            this.TabService.clearTabs(this.pageName);
            angular.forEach(alerts,  (alert: any, i: any)=>{
                var transformedAlert = this.transformAlert(alert);
                this.TabService.addContent(this.pageName, tabTitle, transformedAlert);
            });

        }

        /**
         * Transform the alert coming from the server to a UI model
         * @param alert
         * @returns {*}
         */
        transformAlert=(alert: any) =>{
            alert.typeDisplay = alert.type;
            if(alert.typeDisplay.indexOf("http://kylo.io/alert/alert/") == 0){
                alert.typeDisplay = alert.typeDisplay.substring("http://kylo.io/alert/alert/".length);
                alert.typeDisplay= alert.typeDisplay.split("/").join(" ");
            }
            else if(alert.typeDisplay.indexOf("http://kylo.io/alert/") == 0){
                alert.typeDisplay = alert.typeDisplay.substring("http://kylo.io/alert/".length);
                alert.typeDisplay = alert.typeDisplay.split("/").join(" ");
            }
            return alert;
        }

        /**
         * Navigate to the alert details page
         * @param event
         * @param alert
         */
        alertDetails =  (event: any, alert: any)=> {
            this.StateService.OpsManager().Alert().navigateToAlertDetails(alert.id);
        };

}


export class AlertsController implements ng.IComponentController{
    query: any;
    constructor(private $transition$: any){
        this.query = $transition$.params().query;
    }
}
angular.module(moduleName).controller("AlertsController",["$transition$",AlertsController]);
angular.module(moduleName).controller("AlertsTableController",
    ["$scope","$http","$q","TableOptionsService","PaginationDataService","StateService",
    "TabService","OpsManagerRestUrlService",AlertsTableController]);
angular.module(moduleName).directive("tbaAlertsTable", [
        ()=> {
           return {
            restrict: "E",
            bindToController: {
                cardTitle: "@",
                pageName: '@',
                query:"=",
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/ops-mgr/alerts/alerts-table-template.html',
            controller: "AlertsTableController",
            link: function ($scope: any, element: any, attrs: any, controller: any) {

            }
           }
        }
    ]);