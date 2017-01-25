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
                stepIndex: '@'
            },
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/visual-query/visual-query-store.html',
            controller: "VisualQueryStoreController",
            link: function ($scope, element, attrs, controllers) {

            }

        };
    }

    var controller =  function($scope,$log, $http,$mdToast,RestUrlService, VisualQueryService,  HiveService, TableDataFunctions) {

        this.model = VisualQueryService.model;
        this.isValid = true;
        $scope.$on('$destroy',function(){

        });

        this.save = function(){

        }

    };


    angular.module(MODULE_FEED_MGR).controller('VisualQueryStoreController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigVisualQueryStore', directive);

})();
