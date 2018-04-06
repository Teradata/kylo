import * as angular from 'angular';

const moduleName = require('feed-mgr/feeds/edit-feed/module-name');

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
        link: function ($scope:any, element:any, attrs:any, controller:any) {

        }

    };
}


export class FeedProfileValidResultsController implements ng.IComponentController {
// define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {

    model: any = this.FeedService.editFeedModel;
    loading: boolean = false;
    limitOptions: Array<Number> = [10, 50, 100, 500, 1000];
    limit: Number = this.limitOptions[2];
    headers: any;
    rows: any;
    processingdttm: any;
    queryResults: any = null;

    constructor(private $scope: any, private $http: any, private FeedService: any, private RestUrlService: any, private HiveService: any
        , private Utils: any, private BroadcastService: any, private FattableService: any) {
        const self = this;

        //noinspection JSUnusedGlobalSymbols
        function onLimitChange() {
            getProfileValidation().then(setupTable);
        }

        function getProfileValidation() {
            self.loading = true;

            const successFn = (response: any) => {
                const result = self.queryResults = HiveService.transformResultsToUiGridModel(response);
                self.headers = result.columns;
                self.rows = result.rows;
                self.loading = false;

                BroadcastService.notify('PROFILE_TAB_DATA_LOADED', 'valid');
            };
            const errorFn = (err: any) => {
                self.loading = false;
            };
            const promise = $http.get(RestUrlService.FEED_PROFILE_VALID_RESULTS_URL(self.model.id),
                {
                    params:
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
    }

}

angular.module(moduleName).controller('FeedProfileValidResultsController', ["$scope","$http","FeedService","RestUrlService","HiveService","Utils","BroadcastService","FattableService",FeedProfileValidResultsController]);
angular.module(moduleName).directive('thinkbigFeedProfileValid', directive);
