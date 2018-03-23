define(["require", "exports", "angular", "app"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $http, $location, $timeout, $window, $mdSidenav, $mdMedia, $mdBottomSheet, $log, $q, $element, $rootScope, $transitions, $mdDialog, StateService, SearchService, SideNavService, AccessControlService) {
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
            this.searchQuery = '';
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
                _this.SearchService.searchQuery = _this.searchQuery;
                if (_this.currentState.name != 'search') {
                    _this.StateService.Search().navigateToSearch(true);
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
            this.$transitions.onSuccess({}, function (transition) {
                _this.currentState = transition.to();
                if (_this.currentState.name != 'search') {
                    _this.searchQuery = '';
                }
                else {
                    _this.searchQuery = _this.SearchService.searchQuery;
                }
                _this.$rootScope.previousState = transition.from().name;
                _this.$rootScope.currentState = transition.to().name;
                //hide the loading dialog
                if (!_this.AccessControlService.isFutureState(_this.currentState.name)) {
                    if (_this.loadingTimeout != null) {
                        _this.$timeout.cancel(_this.loadingTimeout);
                        _this.loadingTimeout = null;
                    }
                    if (_this.loading) {
                        _this.loading = false;
                        _this.$mdDialog.hide();
                    }
                }
            });
            // Fetch the allowed actions
            this.AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.allowSearch = _this.AccessControlService
                    .hasAction(_this.AccessControlService.GLOBAL_SEARCH_ACCESS, actionSet.actions);
            });
        }
        controller.prototype.closeSideNavList = function () {
            this.$mdSidenav('left').close();
        };
        controller.prototype.showLoadingDialog = function () {
            this.loading = true;
            this.$mdDialog.show({
                templateUrl: 'js/main/loading-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true
            });
        };
        return controller;
    }());
    exports.controller = controller;
    angular.module('kylo').controller('IndexController', ["$scope", "$http", "$location", "$timeout", "$window", "$mdSidenav", "$mdMedia", "$mdBottomSheet", "$log", "$q", "$element",
        "$rootScope", "$transitions", "$mdDialog", "StateService", "SearchService", "SideNavService", "AccessControlService",
        controller]);
});
//# sourceMappingURL=IndexController.js.map