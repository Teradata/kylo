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

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                processingdttm:'=',
                rowsPerPage:'='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/profile-history/profile-valid-results.html',
            controller: "FeedProfileValidResultsController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller =  function($scope,$http,$stateParams, FeedService, RestUrlService, HiveService, Utils,BroadcastService) {

        var self = this;

        this.model = FeedService.editFeedModel;
        this.loading = false;
        this.limitOptions = [10, 50, 100, 500, 1000];
        this.limit = this.limitOptions[2];

        //noinspection JSUnusedGlobalSymbols
        this.onLimitChange = function() {
            getProfileValidation();
        };

        $scope.gridOptions = {
            columnDefs: [],
            data: null,
            enableColumnResizing: true,
            enableGridMenu: true
        };

        function getProfileValidation(){
            self.loading = true;

            var successFn = function (response) {
                var result = self.queryResults = HiveService.transformResultsToUiGridModel(response);

                $scope.gridOptions.columnDefs = result.columns;
                $scope.gridOptions.data = result.rows;

                self.loading = false;

                BroadcastService.notify('PROFILE_TAB_DATA_LOADED','valid');
            };
            var errorFn = function (err) {
                self.loading = false;
            };
            var promise = $http.get(RestUrlService.FEED_PROFILE_VALID_RESULTS_URL(self.model.id),
                { params:
                    {
                        'processingdttm': self.processingdttm,
                        'limit': self.limit
                    }
                });
            promise.then(successFn, errorFn);
            return promise;
        }

        getProfileValidation();
    };


    angular.module(MODULE_FEED_MGR).controller('FeedProfileValidResultsController', controller);
    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedProfileValid', directive);

})();
