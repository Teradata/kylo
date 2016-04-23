(function () {

    var controller = function($scope,$http,$mdToast,$mdDialog,AddButtonService,CategoriesService,StateService){

        var self = this;
        this.searchQuery = '';

        this.categories = CategoriesService.categories;

        $scope.$watchCollection(function(){
            return CategoriesService.categories
        },function(newVal) {
            self.categories = newVal;
        })

        this.newCategory = function() {
            self.showDialog(null);
        }

        this.editCategory = function(category) {
            StateService.navigateToCategoryDetails(category.id);
        }

        AddButtonService.registerAddButton('categories',function() {
            StateService.navigateToCategoryDetails(null);
        });



    };

    angular.module(MODULE_FEED_MGR).controller('CategoriesController',controller);



}());

