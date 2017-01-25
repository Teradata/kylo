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
                defaultSchemaName: '@',
                defaultTableName: '@'
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/tables/preview.html',
            controller: "PreviewController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller =  function($scope,$http,$stateParams, FeedService, RestUrlService, HiveService, Utils,BroadcastService) {

        var self = this;

        this.model = {};
        this.loading = false;
        this.limitOptions = [10, 50, 100, 500, 1000];
        this.limit = this.limitOptions[1];
        this.executingQuery = true;

        //noinspection JSUnusedGlobalSymbols
        this.onLimitChange = function(item) {
            self.query();
        };

        this.gridOptions = $scope.gridOptions = {
            columnDefs: [],
            data: null,
            enableColumnResizing: true,
            enableGridMenu: true
        };


        this.query = function() {
            self.executingQuery = true;
            if (self.defaultSchemaName != null && self.defaultTableName != null) {
                if (self.rowsPerPage == null) {
                    self.rowsPerPage = 50;
                }
                self.sql = 'SELECT * FROM ' + self.defaultSchemaName + "." + self.defaultTableName + " LIMIT "+self.limit;
            }

            return HiveService.queryResult(self.sql).then(function(tableData) {
                self.loading = true;
                var result = self.queryResults = HiveService.transformQueryResultsToUiGridModel(tableData);.0
                self.gridOptions.columnDefs = result.columns;
                self.gridOptions.data = result.rows;
                self.executingQuery = false;
            });
        };

        self.query();
    };


    angular.module(MODULE_FEED_MGR).controller('PreviewController', controller);
    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigPreview', directive);

})();
