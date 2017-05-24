define(['angular', "search/module-name"], function (angular, moduleName) {

    var controller = function($scope,$sce,$http,$mdToast,$mdDialog,ElasticSearchService, Utils,CategoriesService){

        var self = this;
        /**
         * The result object of what is retured from the search query
         * @type {null}
         */
        this.searchResult = null;
        /**
         * flag to indicate we are querying/searching
         * @type {boolean}
         */
        this.searching = false;

        /**
         * Pagination data
         * @type {{rowsPerPage: number, currentPage: number, rowsPerPageOptions: [*]}}
         */
        this.paginationData = {rowsPerPage:10, currentPage:1, rowsPerPageOptions:['5','10','20','50','100']}

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
            self.searchError = null;
            self.searching = true;
            var limit = self.paginationData.rowsPerPage;
            var start = (limit * self.paginationData.currentPage)-limit; //self.query.page(self.selectedTab));

            ElasticSearchService.search(ElasticSearchService.searchQuery,limit,start).then(function(result){
                self.searchResult = result;
                self.searching = false;
            },function(response){
                self.searching = false;
               var err = response.data.message;
               var detailedMessage = response.data.developerMessage;
               if(detailedMessage) {
                   err += "<br/>" + detailedMessage;
               }
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title('Error Searching')
                        .htmlContent('There was an error while searching.<br/>'+err)
                        .ariaLabel('Error Searching')
                        .ok('Got it!')
                );
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
                controller: "SearchHitDialog",
                templateUrl: 'js/search/elastic-search/search-hit-dialog.html',
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


    function SearchHitDialog($scope, $mdDialog, searchHit ){
        $scope.searchHit = searchHit;


        $scope.hide = function($event) {
            $mdDialog.hide();
        };

        $scope.cancel = function($event) {
            $mdDialog.cancel();
        };


    };

    angular.module(moduleName).controller('ElasticSearchController',["$scope","$sce","$http","$mdToast","$mdDialog","ElasticSearchService","Utils","CategoriesService",controller]);


    angular.module(moduleName).controller('SearchHitDialog',["$scope","$mdDialog","searchHit",SearchHitDialog]);

});



