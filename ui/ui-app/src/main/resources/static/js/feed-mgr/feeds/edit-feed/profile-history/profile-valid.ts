import * as angular from 'angular';

const moduleName = require('feed-mgr/feeds/edit-feed/module-name');

const directive = function () {
    return {
        restrict: "EA",
        bindToController: {
            processingdttm: '=',
            rowsPerPage: '='
        },
        controllerAs: 'vm',
        scope: {},
        templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-valid-results.html',
        controller: "FeedProfileValidResultsController",
        link: function ($scope: any, element: any, attrs: any, controller: any) {

        }
    };
};


export class FeedProfileValidResultsController implements ng.IComponentController {

    model: any = this.FeedService.editFeedModel;
    loading: boolean = false;
    limitOptions: Array<Number> = [10, 50, 100, 500, 1000];
    limit: Number = this.limitOptions[2];
    headers: any;
    rows: any;
    processingdttm: any;
    queryResults: any = null;

    constructor(private $http: any, private FeedService: any, private RestUrlService: any, private HiveService: any,
                private BroadcastService: any, private FattableService: any) {
    }

    //noinspection JSUnusedGlobalSymbols
    private onLimitChange() {
        this.init();
    }

    private getProfileValidation() {
        this.loading = true;

        const successFn = (response: any) => {
            const result = this.queryResults = this.HiveService.transformResultsToUiGridModel(response);
            this.headers = result.columns;
            this.rows = result.rows;
            this.loading = false;

            this.BroadcastService.notify('PROFILE_TAB_DATA_LOADED', 'valid');
        };
        const errorFn = (err: any) => {
            this.loading = false;
        };
        const promise = this.$http.get(this.RestUrlService.FEED_PROFILE_VALID_RESULTS_URL(this.model.id),
            {
                params:
                    {
                        'processingdttm': this.processingdttm,
                        'limit': this.limit
                    }
            });
        promise.then(successFn, errorFn);
        return promise;
    }

    private setupTable() {
        this.FattableService.setupTable({
            tableContainerId: "validProfile",
            headers: this.headers,
            rows: this.rows
        });
    }

    $onInit() {
        this.init();
    }

    private init() {
        this.getProfileValidation().then(this.setupTable.bind(this));
    }
}

angular.module(moduleName).controller('FeedProfileValidResultsController',
    ["$http", "FeedService", "RestUrlService", "HiveService", "BroadcastService", "FattableService", FeedProfileValidResultsController]);
angular.module(moduleName).directive('thinkbigFeedProfileValid', directive);
