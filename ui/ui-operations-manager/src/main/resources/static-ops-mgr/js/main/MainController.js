/**
 * Controller for the Main App index.html
 */
(function() {

    var controller = function($scope, $mdSidenav, $mdBottomSheet, $log, $q, $element, $http, AlertsService, ServicesStatusData, FeedData, ConfigurationService, AccessControlService) {

        var self = this;
        self.toggleSideNavList = toggleSideNavList;
        self.menu = [];
        self.selectedMenuItem = null;
        self.selectMenuItem = selectMenuItem;
        self.alertsCount = AlertsService.alerts.length;

        $scope.$watchCollection(function() {
            return AlertsService.alerts;
        }, function(newVal) {
            self.alertsCount = newVal.length;
        })

        function buildSideNavMenu() {
            var menu = [];
            menu.push({sref: "home", icon: "home", text: "Overview", defaultActive: true});
            menu.push({sref: "service-health", icon: "vector_triangle", text: "Services", defaultActive: false});
            menu.push({sref: "jobs", icon: "settings", text: "Jobs", defaultActive: false});
            menu.push({sref: "alerts", icon: "notifications", text: "Alerts", defaultActive: false});
            menu.push({sref: "scheduler", icon: "today", text: "Scheduler", defaultActive: false});
            menu.push({sref: "charts", icon: "insert_chart", text: "Charts", defaultActive: false});
            self.selectedMenuItem = menu[0];
            self.menu = menu;

        }

        function toggleSideNavList() {
            // var pending = $mdBottomSheet.hide() || $q.when(true);

            $q.when(true).then(function() {
                $mdSidenav('left').toggle();
            });
        }

        this.gotoFeedManager = function() {
            if (self.feedMgrUrl == undefined) {
                $http.get(ConfigurationService.MODULE_URLS).then(function(response) {
                    self.feedMgrUrl = response.data.feedMgr;
                    window.location.href = window.location.origin + self.feedMgrUrl;
                });
            }
            else {
                window.location.href = window.location.origin + self.feedMgrUrl;
            }
        }

        function selectMenuItem($event, menuItem) {
            self.selectedMenuItem = menuItem;
            self.toggleSideNavList();
        }

        // Fetch list of allowed actions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    if (AccessControlService.hasAction(AccessControlService.OPERATIONS_MANAGER_ACCESS, actionSet.actions)) {
                        buildSideNavMenu(actionSet.actions);
                    }
                });

        ServicesStatusData.fetchServiceStatus();
        FeedData.fetchFeedHealth();
    };

    angular.module(MODULE_OPERATIONS).controller('MainController', controller);

}());
