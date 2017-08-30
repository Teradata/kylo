/**
 * Controller for the Main App index.html
 */
define(['angular','app'], function (angular) {

    var controller = function ($scope, $http, $location, $timeout, $window, $mdSidenav, $mdMedia, $mdBottomSheet, $log, $q, $element, $rootScope, $transitions, $mdDialog, StateService,
                               SearchService, SideNavService, AccessControlService) {
        var self = this;
        /**
         * Function to toggle the left nav
         * @type {toggleSideNavList}
         */
        self.toggleSideNavList = toggleSideNavList;

        /**
         * The current ui-router state
         * @type {null}
         */
        self.currentState = null;
        /**
         * Menu at the top navigation
         * @type {Array}
         */
        self.topNavMenu = [];

        /**
         * Flag to indicated if the left side nav is open or not
         * @type {boolean}
         */
        self.sideNavOpen = SideNavService.isLockOpen;

        /**
         * Service to open/close the left nav
         */
        self.sideNavService = SideNavService;

        /**
         * The Query string for the Global Search
         * @type {string}
         */
        this.searchQuery = '';

        /**
         * Media object to help size the panels on the screen when shrinking/growing the window
         */
        $scope.$mdMedia = $mdMedia;

        /**
         * Time to wait before initializing the loading dialog
         * @type {number}
         */
        var LOADING_DIALOG_WAIT_TIME = 100;

        /**
         * Indicates that global searches are allowed.
         * @type {boolean}
         */
        self.allowSearch = false;

        /**
         * Check if the Side Nav is hidden or not
         * @returns {*|boolean}
         */
        this.isSideNavHidden = function () {
            return ($mdMedia('gt-md') && SideNavService.isLockOpen)
        };

        function toggleSideNavList() {
            $q.when(true).then(function () {
                $mdSidenav('left').toggle();
            });
        }

        function closeSideNavList() {
            $mdSidenav('left').close();
        }

        /**
         * Search for something
         */
        this.search = function () {
            SearchService.searchQuery = this.searchQuery;
            if (self.currentState.name != 'search') {
                StateService.Search().navigateToSearch(true);
            }
        };

        /**
         * Detect if a user presses Enter while focused in the Search box
         * @param $event
         */
        this.onSearchKeypress = function ($event) {
            if ($event.which === 13) {
                self.search();
            }
        };

        var loading = false;

        function showLoadingDialog() {
            loading = true;

            $mdDialog.show({
                templateUrl: 'js/main/loading-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true
            });
        }

        /**
         * Show a loading dialog if the load takes longer than xx ms
         */
        var loadingTimeout = $timeout(function () {
            showLoadingDialog();

        }, LOADING_DIALOG_WAIT_TIME);

        /**
         * Set the ui-router states to the $rootScope for easy access
         */
        $rootScope.previousState;
        $rootScope.currentState;

        $transitions.onSuccess({}, function (transition) {
            self.currentState = transition.to();
            if (self.currentState.name != 'search') {
                self.searchQuery = '';
            }
            else {
                self.searchQuery = SearchService.searchQuery;
            }
            $rootScope.previousState = transition.from().name;
            $rootScope.currentState = transition.to().name;

            //hide the loading dialog
            if (!AccessControlService.isFutureState(self.currentState.name)) {
                if (loadingTimeout != null) {
                    $timeout.cancel(loadingTimeout);
                    loadingTimeout = null;
                }
                if (loading) {
                    loading = false;
                    $mdDialog.hide();
                }
            }

        });

        // Fetch the allowed actions
        AccessControlService.getUserAllowedActions()
            .then(function (actionSet) {
                self.allowSearch = AccessControlService.hasAction(AccessControlService.GLOBAL_SEARCH_ACCESS, actionSet.actions);
            });
    };

    return angular.module("kylo").controller('IndexController', ["$scope", "$http", "$location", "$timeout", "$window", "$mdSidenav", "$mdMedia", "$mdBottomSheet", "$log", "$q", "$element",
                                                                 "$rootScope", "$transitions", "$mdDialog", "StateService", "SearchService", "SideNavService", "AccessControlService",
                                                                 controller]);

});
