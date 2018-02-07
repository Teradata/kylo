import * as angular from 'angular';
import "app";

export class controller implements ng.IComponentController{
        /**
     * Time to wait before initializing the loading dialog
     * @type {number}
     */
    LOADING_DIALOG_WAIT_TIME:any = 100;
    constructor(
        private $scope:any,
        private $http:any,
        private $location: any,
        private $timeout: any,
        private $window: any,
        private $mdSidenav: any,
        private $mdMedia: any,
        private $mdBottomSheet: any,
        private $log: any,
        private $q: any,
        private $element: any,
        private $rootScope: any,
        private $transitions: any,
        private $mdDialog:any,
        private StateService:any,  
        private SearchService: any,
        private SideNavService: any,
        private AccessControlService:any)
        { 
         this.LOADING_DIALOG_WAIT_TIME= 100;
    
         /**
          * Media object to help size the panels on the screen when shrinking/growing the window
          */
          this.$scope.$mdMedia= this.$mdMedia;
          /**
          * Set the ui-router states to the $rootScope for easy access
          */
            this.$rootScope.previousState;
            this.$rootScope.currentState;
  

            this.$transitions.onSuccess({}, (transition: any)=> {
            this.currentState = transition.to();
            if (this.currentState.name != 'search') {
                this.searchQuery = '';
            }
            else {
                this.searchQuery = this.SearchService.searchQuery;
            }
            this.$rootScope.previousState = transition.from().name;
            this.$rootScope.currentState = transition.to().name;

            //hide the loading dialog
            if (!this.AccessControlService.isFutureState(this.currentState.name)) {
                if (this.loadingTimeout != null) {
                    this.$timeout.cancel(this.loadingTimeout);
                    this.loadingTimeout = null;
                }
                if (this.loading) {
                    this.loading = false;
                    this.$mdDialog.hide();
                }
            }

        });
         // Fetch the allowed actions
            this.AccessControlService.getUserAllowedActions()
                .then((actionSet: any)=> {
                    this.allowSearch = this.AccessControlService
                                            .hasAction(this.AccessControlService.GLOBAL_SEARCH_ACCESS, 
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

angular.module('kylo').controller('IndexController', ["$scope", "$http", "$location", "$timeout", "$window", "$mdSidenav", "$mdMedia", "$mdBottomSheet", "$log", "$q", "$element",
                                                                 "$rootScope", "$transitions", "$mdDialog", "StateService", "SearchService", "SideNavService", "AccessControlService",
                                                                 controller]);
