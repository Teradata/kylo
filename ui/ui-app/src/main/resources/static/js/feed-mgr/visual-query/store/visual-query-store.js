define(['angular',"feed-mgr/visual-query/module-name"], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/feed-mgr/visual-query/store/visual-query-store.html',
            controller: "VisualQueryStoreController",
            link: function ($scope, element, attrs, controllers) {

            }

        };
    }

    var controller =  function($scope,$log, $http,$mdToast,RestUrlService, VisualQueryService) {

        this.model = VisualQueryService.model;
        this.isValid = true;
        $scope.$on('$destroy',function(){

        });

        this.save = function(){

        }

    };


    angular.module(moduleName).controller('VisualQueryStoreController', ["$scope","$log","$http","$mdToast","RestUrlService","VisualQueryService",controller]);

    angular.module(moduleName)
        .directive('thinkbigVisualQueryStore', directive);

});
