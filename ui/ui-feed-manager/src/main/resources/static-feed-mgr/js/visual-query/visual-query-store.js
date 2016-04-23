
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
