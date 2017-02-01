/*-
 * #%L
 * thinkbig-ui-common
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/*
 Module for popup menu
 */
(function() {

    var directive = function($http, $mdDialog, $window, AboutKyloService) {
        return {
            restrict: "E",
            scope: {
                selectedOption: "&?",
                openedMenu: "&?",
                menuIcon: "@?"
            },
            templateUrl: 'js/shared/kylo-options/kylo-options.html',
            link: function($scope) {

                //default the icon to be more_vert
                if (!angular.isDefined($scope.menuIcon)) {
                    $scope.menuIcon = 'more_vert';
                }

                // Get user name
                $scope.username = "User";
                $http.get("/proxy/v1/about/me").then(function(response) {
                    $scope.username = response.data.systemName;
                });

                $scope.openMenu = function($mdOpenMenu, ev) {
                    //callback
                    if ($scope.openedMenu) {
                        $scope.openedMenu();
                    }
                    $mdOpenMenu(ev);
                };

                $scope.aboutKylo = function() {
                    AboutKyloService.showAboutDialog();
                    if ($scope.selectedOption) {
                        $scope.selectedOption()('aboutKylo');
                    }
                };

                /**
                 * Redirects the user to the logout page.
                 */
                $scope.logout = function() {
                    $window.location.href = "/logout";
                }
            }
        }
    };

    angular.module(COMMON_APP_MODULE_NAME).directive('kyloOptions', ['$http', '$mdDialog', '$window', 'AboutKyloService', directive]);
}());
