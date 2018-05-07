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
        templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-stats.html',
        controller: "FeedProfileStatsController"
    };
};

export class FeedProfileStatsController implements ng.IComponentController {

    data: Array<any> = [];
    loading: boolean = true;
    processingdttm: any;
    processingDate: any = null;
    model: any = this.FeedService.editFeedModel;
    hideColumns: any = ["processing_dttm"];

    constructor(private $http: any, private FeedService: any, private RestUrlService: any, private HiveService: any, private BroadcastService: any) {
    }

    private getProfileStats() {
        this.loading = true;
        const successFn = (response: any) => {
            this.data = response.data;
            this.loading = false;
            this.BroadcastService.notify('PROFILE_TAB_DATA_LOADED', 'profile-stats');
        };
        const errorFn = (err: any) => {
            this.loading = false;
        };
        const promise = this.$http.get(this.RestUrlService.FEED_PROFILE_STATS_URL(this.model.id), {params: {'processingdttm': this.processingdttm}});
        promise.then(successFn, errorFn);
        return promise;
    };


    $onInit() {
        this.processingDate = new Date(this.HiveService.getUTCTime(this.processingdttm));
        this.getProfileStats();
    }
}

angular.module(moduleName).controller('FeedProfileStatsController',
    ["$http", "FeedService", "RestUrlService", "HiveService", "BroadcastService", FeedProfileStatsController]);
angular.module(moduleName).directive('thinkbigFeedProfileStats', directive);
