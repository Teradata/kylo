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

    var directive = function ($http,RestUrlService) {
        return {
            restrict: "EA",
            scope: {
                cronExpression: '='
            },
            templateUrl: 'js/shared/cron-expression-preview/cron-expression-preview.html',
            link: function ($scope, element, attrs) {

                $scope.nextDates = [];

               function getNextDates() {
                    $http.get(RestUrlService.PREVIEW_CRON_EXPRESSION_URL,{params:{cronExpression:$scope.cronExpression}}).then(function (response) {
                        $scope.nextDates = response.data;
                    });
                }

                $scope.$watch('cronExpression',function(newVal) {
                    if(newVal != null && newVal != ''){
                        getNextDates();
                    }
                    else {
                        $scope.nextDates = [];
                    }
                });
                getNextDates();
            }

        };
    }




    angular.module(MODULE_FEED_MGR)
        .directive('cronExpressionPreview', ['$http','RestUrlService',directive]);

})();
