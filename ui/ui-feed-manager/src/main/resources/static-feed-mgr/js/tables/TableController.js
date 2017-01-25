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

    var controller = function($scope,$stateParams,$http,RestUrlService,StateService){

        var self = this;
        this.tableSchema =null;

        self.selectedTabIndex = 0;
        self.hql = '';

        var init = function(){
            var schema = $stateParams.schema;
            self.schema = schema;
            self.tableName = $stateParams.tableName;
            getTable(self.schema,self.tableName);
        }


        $scope.$watch(function(){
            return self.selectedTabIndex;
        },function(newVal){
        })



        function getTable(schema,table){
            var successFn = function (response) {
                self.tableSchema = response.data;

            }
            var errorFn = function (err) {
                self.loading = false;

            }
            var promise = $http.get(RestUrlService.HIVE_SERVICE_URL+"/schemas/"+schema+"/tables/"+table);
            promise.then(successFn, errorFn);
            return promise;
        }
        init();
    };

    angular.module(MODULE_FEED_MGR).controller('TableController',controller);



}());

