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

    var controller = function($scope,$sce,$http,$mdToast,$mdDialog,ElasticSearchService, Utils,CategoriesService){

        var self = this;
        this.searchResult = null;

        this.paginationData = {rowsPerPage:10,
        currentPage:1,
            rowsPerPageOptions:['5','10','20','50','100']}

        var supportedDisplays = ['tweets','feed','table-metadata','hive-query','index-pattern'];


        this.categoryForIndex = function(indexName) {
            var category = CategoriesService.findCategoryByName(indexName);
            if(category != null){
                return category;
            }
            return null;
        }

        $scope.$watch(function() {
            return ElasticSearchService.searchQuery
        },function(newVal){
            self.search();
        })

       this.search = function(){
           var search = true;
           //changing the paginationData.currentPAge will trigger a search.
           //only search if they page was not already 1

           if(self.paginationData.currentPage > 1){
               search = false;
           }
         self.paginationData.currentPage = 1;
           if(search) {
               self._search();
           }
       }
        this._search = function(){
            var limit = self.paginationData.rowsPerPage;
            var start = (limit * self.paginationData.currentPage)-limit; //self.query.page(self.selectedTab));

            ElasticSearchService.search(ElasticSearchService.searchQuery,limit,start).then(function(result){
                self.searchResult = result;
            });
        }



        this.onPaginationChange = function (page, limit) {
            self.paginationData.currentPage = page;
            self._search();
        };

        this.onSearchResultItemClick = function($event,result){
            self.showSearchHitDialog(result);
        }

        this.renderHtml = function (htmlCode) {
            return $sce.trustAsHtml(Utils.maskProfanity(htmlCode));
        };

        this.isGenericSearchHitDisplay = function(searchHit){
            return !_.contains(supportedDisplays,searchHit.type);
        }


        this.showSearchHitDialog = function(searchHit) {
            $mdDialog.show({
                controller: SearchHitDialog,
                templateUrl: 'js/elastic-search/search-hit-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose:true,
                fullscreen: true,
                locals : {
                    searchHit:searchHit
                }
            })
                .then(function(msg) {


                }, function() {

                });
        };




    };

    angular.module(MODULE_FEED_MGR).controller('ElasticSearchController',controller);



}());



function SearchHitDialog($scope, $mdDialog, $mdToast, $http, StateService, searchHit ){
    $scope.searchHit = searchHit;


    $scope.hide = function($event) {
        $mdDialog.hide();
    };

    $scope.cancel = function($event) {
        $mdDialog.cancel();
    };


};
