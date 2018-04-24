import * as angular from 'angular';
import "app";
import StateService from  "../services/StateService";
import AccessControlService from "../services/AccessControlService";
import AccessConstants from "../constants/AccessConstants";
import SearchService from "../services/SearchService";
import SideNavService from "../services/SideNavService";
import {TransitionService} from "@uirouter/core";
export interface IMyScope extends ng.IScope {
  $mdMedia?: any;
}
export class controller implements ng.IComponentController{
    /**
     * Time to wait before initializing the loading dialog
     * @type {number}
     */
    LOADING_DIALOG_WAIT_TIME:number = 100;
    static readonly $inject = ["$scope", "$http", "$location", "$timeout", "$window", "$mdSidenav", "$mdMedia", 
                                "$mdBottomSheet", "$log", "$q", "$element","$rootScope", "$transitions", 
                                "$mdDialog", "StateService", "SearchService", "SideNavService", 
                                "AccessControlService"];
    constructor(
        private $scope:IMyScope,
        private $http:angular.IHttpService,
        private $location: angular.ILocationService,
        private $timeout: angular.ITimeoutService,
        private $window: angular.IWindowService,
        private $mdSidenav: angular.material.ISidenavService,
        private $mdMedia: angular.material.IMedia,
        private $mdBottomSheet: angular.material.IBottomSheetService,
        private $log: angular.ILogService,
        private $q: angular.IQService,
        private $element: JQuery,
        private $rootScope: any, //angular.IRootScopeService,
        private $transitions: TransitionService,
        private $mdDialog:angular.material.IDialogService,
        private StateService:StateService,  
        private SearchService: SearchService,
        private SideNavService: SideNavService,
        private AccessControlService:AccessControlService)
        { 
        // this.LOADING_DIALOG_WAIT_TIME= 100;
         /**
          * Media object to help size the panels on the screen when shrinking/growing the window
          */
          $scope.$mdMedia= $mdMedia;
          /**
          * Set the ui-router states to the $rootScope for easy access
          */
            $rootScope.previousState;
            $rootScope.currentState;
            $transitions.onSuccess({}, (transition: any)=> {
            this.currentState = transition.to();
            if (this.currentState.name != 'search') {
                this.searchQuery = '';
            }
            else {
                this.searchQuery = SearchService.searchQuery;
            }
            $rootScope.previousState = transition.from().name;
            $rootScope.currentState = transition.to().name;

            //hide the loading dialog
            if (!AccessControlService.isFutureState(this.currentState.name)) {
                if (this.loadingTimeout != null) {
                    $timeout.cancel(this.loadingTimeout);
                    this.loadingTimeout = null;
                }
                if (this.loading) {
                    this.loading = false;
                    this.$mdDialog.hide();
                }
            }
        });
         // Fetch the allowed actions
            AccessControlService.getUserAllowedActions()
                .then((actionSet: any)=> {
                    this.allowSearch = AccessControlService
                                            .hasAction(AccessConstants.GLOBAL_SEARCH_ACCESS, 
                                                        actionSet.actions);
                });

    }


    /**
         * Function to toggle the left nav
         * @type {toggleSideNavList}
         */
     //   toggleSideNavList: any = this.toggleSideNavList();

        /**
         * The current ui-router state
         * @type {null}
         */
        currentState: any = null;
        /**
         * Menu at the top navigation
         * @type {Array}
         */
        topNavMenu: any = [];

        /**
         * Flag to indicated if the left side nav is open or not
         * @type {boolean}
         */
        sideNavOpen: any = this.SideNavService.isLockOpen;

        /**
         * Service to open/close the left nav
         */
        sideNavService: any = this.SideNavService;

        /**
         * The Query string for the Global Search
         * @type {string}
         */
        searchQuery: any = '';

        /**
         * Indicates that global searches are allowed.
         * @type {boolean}
         */
        allowSearch: any = false;

        /**
         * Check if the Side Nav is hidden or not
         * @returns {*|boolean}
         */
        isSideNavHidden = () =>{
            return (this.$mdMedia('gt-md') && this.SideNavService.isLockOpen);
        };

        toggleSideNavList=()=>{
            this.$q.when(true).then(()=> {
                this.$mdSidenav('left').toggle();
            });
        };

       closeSideNavList() {
            this.$mdSidenav('left').close();
        }

        /**
         * Search for something
         */
        search = () =>{
            this.SearchService.searchQuery = this.searchQuery;
            if (this.currentState.name != 'search') {
                this.StateService.Search().navigateToSearch(true);
            }
        };

        /**
         * Detect if a user presses Enter while focused in the Search box
         * @param $event
         */
        onSearchKeypress = ($event:any)=> {
            if ($event.which === 13) {
                this.search();
            }
        };

        loading: any = false;

        showLoadingDialog() {
            this.loading = true;

            this.$mdDialog.show({
                templateUrl: 'js/main/loading-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true
            });
        }

        /**
         * Show a loading dialog if the load takes longer than xx ms
         */
        loadingTimeout: any = this.$timeout(()=> {
            this.showLoadingDialog();

        }, this.LOADING_DIALOG_WAIT_TIME);
}

 /* angular.module('kylo').component("indexController", { 
        controller: controller,
        controllerAs: "mc",
        templateUrl: "index.html"
    });*/
    angular.module('kylo').controller('IndexController',controller);
//.controller('IndexController', ["$scope", "$http", "$location", "$timeout", "$window", "$mdSidenav", "$mdMedia", "$mdBottomSheet", "$log", "$q", "$element",
                                                             //    "$rootScope", "$transitions", "$mdDialog", "StateService", "SearchService", "SideNavService", "AccessControlService",
                                                               // controller]);