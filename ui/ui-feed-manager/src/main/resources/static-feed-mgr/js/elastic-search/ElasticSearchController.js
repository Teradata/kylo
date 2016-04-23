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