define(['angular','common/module-name'], function (angular,moduleName) {

        var directive = function ($http, $mdDialog, $window, AboutKyloService) {
            return {
                restrict: "E",
                scope: {
                    selectedOption: "&?",
                    openedMenu: "&?",
                    menuIcon: "@?"
                },
                templateUrl: 'js/common/kylo-options/kylo-options.html',
                link: function ($scope) {

                    //default the icon to be more_vert
                    if (!angular.isDefined($scope.menuIcon)) {
                        $scope.menuIcon = 'more_vert';
                    }

                    // Get user name
                    $scope.username = "User";
                    $http.get("/proxy/v1/about/me").then(function (response) {
                        $scope.username = response.data.systemName;
                    });

                    $scope.openMenu = function ($mdOpenMenu, ev) {
                        //callback
                        if ($scope.openedMenu) {
                            $scope.openedMenu();
                        }
                        $mdOpenMenu(ev);
                    };

                    $scope.aboutKylo = function () {
                        AboutKyloService.showAboutDialog();
                        if ($scope.selectedOption) {
                            $scope.selectedOption()('aboutKylo');
                        }
                    };

                    /**
                     * Redirects the user to the logout page.
                     */
                    $scope.logout = function () {
                        $window.location.href = "/logout";
                    }
                }
            }
        };

        angular.module(moduleName).directive('kyloOptions', ['$http', '$mdDialog', '$window', 'AboutKyloService', directive]);
});