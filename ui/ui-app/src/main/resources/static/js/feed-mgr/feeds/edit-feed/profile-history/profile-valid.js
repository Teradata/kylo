define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {


    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                processingdttm:'=',
                rowsPerPage:'='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-valid-results.html',
            controller: "FeedProfileValidResultsController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller =  function($scope,$http, FeedService, RestUrlService, HiveService, Utils,BroadcastService,FattableService) {

        var self = this;

        this.model = FeedService.editFeedModel;
        this.loading = false;
        this.limitOptions = [10, 50, 100, 500, 1000];
        this.limit = this.limitOptions[2];

        //noinspection JSUnusedGlobalSymbols
        this.onLimitChange = function() {
            getProfileValidation().then(setupTable);
        };

        function getProfileValidation(){
            self.loading = true;

            var successFn = function (response) {
                var result = self.queryResults = HiveService.transformResultsToUiGridModel(response);
                self.headers = result.columns;
                self.rows = result.rows;
                self.loading = false;

                BroadcastService.notify('PROFILE_TAB_DATA_LOADED','valid');
            };
            var errorFn = function (err) {
                self.loading = false;
            };
            var promise = $http.get(RestUrlService.FEED_PROFILE_VALID_RESULTS_URL(self.model.id),
                { params:
                    {
                        'processingdttm': self.processingdttm,
                        'limit': self.limit
                    }
                });
            promise.then(successFn, errorFn);
            return promise;
        }

        function setupTable() {
            FattableService.setupTable({
                tableContainerId: "validProfile",
                headers: self.headers,
                rows: self.rows
            });
        }

        getProfileValidation().then(setupTable);
    };


    angular.module(moduleName).controller('FeedProfileValidResultsController', ["$scope","$http","FeedService","RestUrlService","HiveService","Utils","BroadcastService","FattableService",controller]);
    angular.module(moduleName).directive('thinkbigFeedProfileValid', directive);

});
