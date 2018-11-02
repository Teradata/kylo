import {Injector} from "@angular/core";
import {LoadingMode, LoadingType, TdLoadingService} from "@covalent/core/loading";
import {RejectType, Transition, TransitionService} from "@uirouter/core";
import * as angular from 'angular';

import "app";
import {StateService} from  "../services/StateService";
import {AccessControlService} from "../services/AccessControlService";
import AccessConstants from "../constants/AccessConstants";
import SearchService from "../services/SearchService";
import {SideNavService} from "../services/SideNavService";
import '../../assets/images/kylo-logo-orange-200.png';

export interface IMyScope extends ng.IScope {
    $mdMedia?: any;
}

/**
 * Identifier for state loader mask of TdLoadingService
 */
const STATE_LOADER = "stateLoader";

export class IndexController implements angular.IComponentController {

    /**
     * Time to wait before initializing the loading dialog
     * @type {number}
     */
    LOADING_DIALOG_WAIT_TIME:number = 100;

    /**
     * Covalent loading service
     */
    loadingService: TdLoadingService;

    /**
     * Timeout for state loader
     */
    stateLoaderTimeout: number;

    template = require("../common/ui-router-breadcrumbs/uiBreadcrumbs.tpl.html");

    static readonly $inject = ["$scope", "$http", "$location", "$timeout", "$window", "$mdSidenav", "$mdMedia",
                                "$mdBottomSheet", "$log", "$q", "$element","$rootScope", "$transitions", "$$angularInjector",
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
        private $$angularInjector: Injector,
        private $mdDialog:angular.material.IDialogService,
        private StateService:StateService,
        private SearchService: SearchService,
        private SideNavService: SideNavService,
        private accessControlService:AccessControlService)
        {
        // this.LOADING_DIALOG_WAIT_TIME= 100;
         /**
          * Media object to help size the panels on the screen when shrinking/growing the window
          */
          $scope.$mdMedia= $mdMedia;
          /**
          * Set the ui-router states to the $rootScope for easy access
          */
            this.$rootScope.previousState;
            this.$rootScope.currentState;


            // to focus on input element after it appears
            $scope.$watch(function() {
                return document.querySelector('#search-bar:not(.ng-hide)');
            }, function(){
                document.getElementById('search-input').focus();
            });

            // Create state loading bar
            this.loadingService = $$angularInjector.get(TdLoadingService);
            this.loadingService.create({
                name: STATE_LOADER,
                mode: LoadingMode.Indeterminate,
                type: LoadingType.Linear,
                color: "accent"
            });

            // Listen for state transitions
            this.$transitions.onCreate({}, this.onTransitionStart.bind(this));
            this.$transitions.onSuccess({}, this.onTransitionSuccess.bind(this));
            this.$transitions.onError({}, this.onTransitionError.bind(this));

         // Fetch the allowed actions
            accessControlService.getUserAllowedActions()
                .then((actionSet: any)=> {
                    this.allowSearch = accessControlService
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
        searchQuery: any = null;

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


        showPreSearchBar() : boolean {
            return this.searchQuery == null;
        };

        initiateSearch() : void {
            this.searchQuery = '';
        };

        showSearchBar() : boolean {
            return this.searchQuery != null
        };

        endSearch() : void {
            return this.searchQuery = null;
        };

        /**
         * Search for something
         */
        search = () =>{
            if (this.searchQuery != null && this.searchQuery.length > 0) {
                this.SearchService.searchQuery = this.searchQuery;
                if (this.currentState.name != 'search') {
                    this.StateService.Search().navigateToSearch(true);
                }
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
                templateUrl: './loading-dialog.html',
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

        getStateClassName(){
            if(this.currentState && this.currentState.name){
                return this.currentState.name.replace(/\./g,'-')
            }
            else {
                return '';
            }
        }

    /**
     * Called when transitioning to a new state.
     */
    onTransitionStart(transition: Transition) {
        if (this.stateLoaderTimeout == null) {
            this.stateLoaderTimeout = setTimeout(() => this.loadingService.register(STATE_LOADER), 10);
        }
    }

    /**
     * Called when the transition was successful.
     */
    onTransitionSuccess(transition: Transition) {
        // Clear state loading bar. Ignore parent states as child states will load next.
        if (!transition.to().name.endsWith(".**")) {
            clearTimeout(this.stateLoaderTimeout);
            this.stateLoaderTimeout = null;
            this.loadingService.resolveAll(STATE_LOADER);
        }

        // Clear search query on "search" state
        this.currentState = transition.to();
        if (this.currentState.name != 'search') {
            this.searchQuery = null;
        }
        else {
            this.searchQuery = this.SearchService.searchQuery;
        }
        this.$rootScope.previousState = transition.from().name;
        this.$rootScope.currentState = transition.to().name;

        //hide the loading dialog
        if (!this.accessControlService.isFutureState(this.currentState.name)) {
            if (this.loadingTimeout != null) {
                this.$timeout.cancel(this.loadingTimeout);
                this.loadingTimeout = null;
            }
            if (this.loading) {
                this.loading = false;
                this.$mdDialog.hide();
            }
        }
    }

    /**
     * Called when the transition failed.
     */
    onTransitionError(transition: Transition) {
        // Clear state loading bar. Ignore parent states (type is SUPERSEDED) as child states will load next.
        if (transition.error().type !== RejectType.SUPERSEDED) {
            clearTimeout(this.stateLoaderTimeout);
            this.stateLoaderTimeout = null;
            this.loadingService.resolveAll(STATE_LOADER);
        }
    }
}

  angular.module('kylo').component("indexController", {
        controller: IndexController,
        controllerAs: "mc",
        template: require("./index.component.html")
    });
