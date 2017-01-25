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
 * Service to display 'about kylo' popup page, and get kylo version
 */
angular.module(COMMON_APP_MODULE_NAME).service('AboutKyloService', function ($mdDialog) {
    var self = this;

    self.showAboutDialog = function() {
        $mdDialog.show({
            controller: 'AboutKyloDialogController',
            templateUrl: 'js/shared/about-kylo/about.html',
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            escapeToClose:true,
            fullscreen: false,
            locals : {

            }
        }).then(function(msg) {
            //callback (success)
        }, function() {
            //callback (failure)
        });
    }
});

(function () {
    var controller = function ($scope, $mdDialog, $http){

        var self = this;

        $http({
            method: "GET",
            url : "/proxy/v1/about/version"
        }).then(function callSuccess(response) {
            $scope.version = response.data;
        }, function callFailure(response) {
            $scope.version = "Not Available"
        });

        $scope.hide = function() {
            $mdDialog.hide();
        };

        $scope.cancel = function() {
            $mdDialog.cancel();
        };


    };
    angular.module(COMMON_APP_MODULE_NAME).controller('AboutKyloDialogController',controller);

}());
