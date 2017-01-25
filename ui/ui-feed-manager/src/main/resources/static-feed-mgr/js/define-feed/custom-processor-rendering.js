/*-
 * #%L
 * thinkbig-ui-feed-manager
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

    var directive = function ($compile, $templateRequest) {
        return {
            restrict: "EA",
            scope: {
                mode: '@',
                processor: '=',
                templateUrl: '@',
                theForm: '='
            },
            template: '<div ng-include="getContentUrl()"></div>',
            link: function ($scope, element, attrs) {
                $scope.getContentUrl = function () {
                    return $scope.templateUrl;
                }
            }
            /*
             compile:function(element,attrs) {
             return {
             pre: function preLink($scope, iElement, iAttrs, controller) {


             },
             post: function postLink($scope, $element, iAttrs, controller) {
             console.log('POST COMPILE!!!! ', $scope.templateUrl)
             $templateRequest($scope.templateUrl).then(function(html){
             // Convert the html to an actual DOM node
             var template = angular.element(html);
             // Append it to the directive element
             $element.append(template);
             // And let Angular $compile it
             $compile(template)($scope);
             });

             }
             }

             }
             */

        };
    }

    angular.module(MODULE_FEED_MGR)
        .directive('customProcessorRendering', ['$compile', '$templateRequest', directive]);

})();
