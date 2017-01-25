/*-
 * #%L
 * thinkbig-ui-operations-manager
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
(function () {

    var directive = function () {
        return {
            restrict: "EA",
            scope: true,
            controllerAs: 'loadingController',
            bindToController: {
                loadingTitle: "@"
            },
            templateUrl: 'js/shared/loading-directive/loading-template.html',
            controller: "LoadingIndicatorController",
            link: function ($scope, element, attrs) {
                $scope.$on('$destroy', function () {
                });
            }
        }
    };

        var controller = function ($scope, $element) {
            var self = this;
        };
        angular.module(MODULE_OPERATIONS).controller('LoadingIndicatorController', controller);

        angular.module(MODULE_OPERATIONS)
            .directive('tbaLoadingIndicator', directive);



    }());
