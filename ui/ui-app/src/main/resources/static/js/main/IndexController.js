define(["require", "exports", "@covalent/core/loading", "@uirouter/core", "angular", "app"], function (require, exports, loading_1, core_1, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Identifier for state loader mask of TdLoadingService
     */
    var STATE_LOADER = "stateLoader";
    var IndexController = /** @class */ (function () {
        function IndexController($scope, $http, $location, $timeout, $window, $mdSidenav, $mdMedia, $mdBottomSheet, $log, $q, $element, $rootScope, $transitions, $$angularInjector, $mdDialog, StateService, SearchService, SideNavService, AccessControlService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$location = $location;
            this.$timeout = $timeout;
            this.$window = $window;
            this.$mdSidenav = $mdSidenav;
            this.$mdMedia = $mdMedia;
            this.$mdBottomSheet = $mdBottomSheet;
            this.$log = $log;
            this.$q = $q;
            this.$element = $element;
            this.$rootScope = $rootScope;
            this.$transitions = $transitions;
            this.$$angularInjector = $$angularInjector;
            this.$mdDialog = $mdDialog;
            this.StateService = StateService;
            this.SearchService = SearchService;
            this.SideNavService = SideNavService;
            this.AccessControlService = AccessControlService;
            /**
             * Time to wait before initializing the loading dialog
             * @type {number}
             */
            this.LOADING_DIALOG_WAIT_TIME = 100;
            /**
                 * Function to toggle the left nav
                 * @type {toggleSideNavList}
                 */
            //   toggleSideNavList: any = this.toggleSideNavList();
            /**
             * The current ui-router state
             * @type {null}
             */
            this.currentState = null;
            /**
             * Menu at the top navigation
             * @type {Array}
             */
            this.topNavMenu = [];
            /**
             * Flag to indicated if the left side nav is open or not
             * @type {boolean}
             */
            this.sideNavOpen = this.SideNavService.isLockOpen;
            /**
             * Service to open/close the left nav
             */
            this.sideNavService = this.SideNavService;
            /**
             * The Query string for the Global Search
             * @type {string}
             */
            this.searchQuery = null;
            /**
             * Indicates that global searches are allowed.
             * @type {boolean}
             */
            this.allowSearch = false;
            /**
             * Check if the Side Nav is hidden or not
             * @returns {*|boolean}
             */
            this.isSideNavHidden = function () {
                return (_this.$mdMedia('gt-md') && _this.SideNavService.isLockOpen);
            };
            this.toggleSideNavList = function () {
                _this.$q.when(true).then(function () {
                    _this.$mdSidenav('left').toggle();
                });
            };
            /**
             * Search for something
             */
            this.search = function () {
                if (_this.searchQuery != null && _this.searchQuery.length > 0) {
                    _this.SearchService.searchQuery = _this.searchQuery;
                    if (_this.currentState.name != 'search') {
                        _this.StateService.Search().navigateToSearch(true);
                    }
                }
            };
            /**
             * Detect if a user presses Enter while focused in the Search box
             * @param $event
             */
            this.onSearchKeypress = function ($event) {
                if ($event.which === 13) {
                    _this.search();
                }
            };
            this.loading = false;
            /**
      * Show a loading dialog if the load takes longer than xx ms
      */
            this.loadingTimeout = this.$timeout(function () {
                _this.showLoadingDialog();
            }, this.LOADING_DIALOG_WAIT_TIME);
            this.LOADING_DIALOG_WAIT_TIME = 100;
            /**
             * Media object to help size the panels on the screen when shrinking/growing the window
             */
            this.$scope.$mdMedia = this.$mdMedia;
            /**
            * Set the ui-router states to the $rootScope for easy access
            */
            this.$rootScope.previousState;
            this.$rootScope.currentState;
            // to focus on input element after it appears
            $scope.$watch(function () {
                return document.querySelector('#search-bar:not(.ng-hide)');
            }, function () {
                document.getElementById('search-input').focus();
            });
            // Create state loading bar
            this.loadingService = $$angularInjector.get(loading_1.TdLoadingService);
            this.loadingService.create({
                name: STATE_LOADER,
                mode: loading_1.LoadingMode.Indeterminate,
                type: loading_1.LoadingType.Linear,
                color: "accent"
            });
            // Listen for state transitions
            this.$transitions.onCreate({}, this.onTransitionStart.bind(this));
            this.$transitions.onSuccess({}, this.onTransitionSuccess.bind(this));
            this.$transitions.onError({}, this.onTransitionError.bind(this));
            // Fetch the allowed actions
            this.AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.allowSearch = _this.AccessControlService
                    .hasAction(_this.AccessControlService.GLOBAL_SEARCH_ACCESS, actionSet.actions);
            });
        }
        IndexController.prototype.closeSideNavList = function () {
            this.$mdSidenav('left').close();
        };
        IndexController.prototype.showPreSearchBar = function () {
            return this.searchQuery == null;
        };
        ;
        IndexController.prototype.initiateSearch = function () {
            this.searchQuery = '';
        };
        ;
        IndexController.prototype.showSearchBar = function () {
            return this.searchQuery != null;
        };
        ;
        IndexController.prototype.endSearch = function () {
            return this.searchQuery = null;
        };
        ;
        IndexController.prototype.showLoadingDialog = function () {
            this.loading = true;
            this.$mdDialog.show({
                templateUrl: 'js/main/loading-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true
            });
        };
        /**
         * Called when transitioning to a new state.
         */
        IndexController.prototype.onTransitionStart = function (transition) {
            var _this = this;
            if (this.stateLoaderTimeout == null) {
                this.stateLoaderTimeout = setTimeout(function () { return _this.loadingService.register(STATE_LOADER); }, 250);
            }
        };
        /**
         * Called when the transition was successful.
         */
        IndexController.prototype.onTransitionSuccess = function (transition) {
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
        };
        /**
         * Called when the transition failed.
         */
        IndexController.prototype.onTransitionError = function (transition) {
            // Clear state loading bar. Ignore parent states (type is SUPERSEDED) as child states will load next.
            if (transition.error().type !== core_1.RejectType.SUPERSEDED) {
                clearTimeout(this.stateLoaderTimeout);
                this.stateLoaderTimeout = null;
                this.loadingService.resolveAll(STATE_LOADER);
            }
        };
        return IndexController;
    }());
    exports.IndexController = IndexController;
    angular.module('kylo').controller('IndexController', ["$scope", "$http", "$location", "$timeout", "$window", "$mdSidenav", "$mdMedia", "$mdBottomSheet", "$log", "$q", "$element",
        "$rootScope", "$transitions", "$$angularInjector", "$mdDialog", "StateService", "SearchService", "SideNavService",
        "AccessControlService", IndexController]);
});
//# sourceMappingURL=IndexController.js.map