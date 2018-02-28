import * as angular from "angular";
import {moduleName} from "../module-name";


   angular.module(moduleName).directive('kyloOptions', 
   ['$http', '$mdDialog', '$window', 'AboutKyloService', 
   ($http, $mdDialog, $window, AboutKyloService)=>
   {
  return {
                restrict: "E",
                scope: {
                    selectedOption: "&?",
                    openedMenu: "&?",
                    menuIcon: "@?"
                },
                templateUrl: 'js/common/kylo-options/kylo-options.html',
                link: function ($scope: any) {

                    //default the icon to be more_vert
                    if (!angular.isDefined($scope.menuIcon)) {
                        $scope.menuIcon = 'more_vert';
                    }

                    // Get user name
                    $scope.username = "User";
                    $http.get("/proxy/v1/about/me").then(function (response: any) {
                        $scope.username = response.data.systemName;
                    });

                    $scope.openMenu = function ($mdOpenMenu: any, ev: any) {
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
   }]);
