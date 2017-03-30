/**
 * Controller for the Main App index.html
 */
define(['angular'], function (angular) {

        var controller = function ($scope, $http, $location, $window, $mdSidenav, $mdMedia, $mdBottomSheet, $log, $q, $element, $rootScope, $transitions,StateService, ElasticSearchService,
                                   SideNavService) {
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


            /*
            self.alertsCount = AlertsService.alerts.length;

            $scope.$watchCollection(function() {
                return AlertsService.alerts;
            }, function(newVal) {
                self.alertsCount = newVal.length;
            })
            */

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
                ElasticSearchService.searchQuery = this.searchQuery;
                if (self.currentState.name != 'search') {
                    StateService.Search().navigateToSearch();
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


            /**
             * Set the ui-router states to the $rootScope for easy access
             */
            $rootScope.previousState;
            $rootScope.currentState;

            $transitions.onSuccess({},function(transition){
                self.currentState = transition.to();
                if (self.currentState.name != 'search') {
                    self.searchQuery = '';
                }
                $rootScope.previousState = transition.from().name;
                $rootScope.currentState = transition.to().name;


            });

        };

     return   angular.module("kylo").controller('IndexController', controller);


});