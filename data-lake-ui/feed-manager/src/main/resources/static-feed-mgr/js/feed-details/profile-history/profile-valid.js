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
        this.data = [];
        this.loading = false;

        function getProfileValidation(){
            self.loading = true;
            var successFn = function (response) {
               var data = HiveService.transformResults(response);
                self.loading = false;
                //add a row to fix counts
                self.data = data;
                BroadcastService.notify('PROFILE_TAB_DATA_LOADED','valid');
            }
            var errorFn = function (err) {
                self.loading = false;
            }
            var promise = $http.get(RestUrlService.FEED_PROFILE_VALID_RESULTS_URL(self.model.id),{params:{'processingdttm':self.processingdttm}});
            promise.then(successFn, errorFn);
            return promise;
        }

        getProfileValidation();
    };


    angular.module(MODULE_FEED_MGR).controller('FeedProfileValidResultsController', controller);
    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedProfileValid', directive);

})();
