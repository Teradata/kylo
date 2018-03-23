import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/edit-feed/module-name');

var directive = function () {
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

export class FeedProfileStatsController implements ng.IComponentController{

    data:Array<any> = [];
    loading:boolean = true;
    processingdttm:any;
    processingDate:any = null;
    model:any = this.FeedService.editFeedModel;
    hideColumns:any = ["processing_dttm"];

    getProfileStats() {
        this.loading = true;
        var successFn = (response:any) => {
            this.data = response.data;
            this.loading = false;
            this.BroadcastService.notify('PROFILE_TAB_DATA_LOADED', 'profile-stats');
        };
        var errorFn = (err:any) => {
            this.loading = false;
        };
        var promise = this.$http.get(this.RestUrlService.FEED_PROFILE_STATS_URL(this.model.id), {params: {'processingdttm': this.processingdttm}});
        promise.then(successFn, errorFn);
        return promise;
    };


    constructor(private $scope:any, private $http:any, private $sce:any, private PaginationDataService:any, private FeedService:any, private RestUrlService:any
        , private HiveService:any, private Utils:any, private BroadcastService:any) {

            var self = this;
            this.processingDate = new Date(this.HiveService.getUTCTime(this.processingdttm));
            this.getProfileStats();

    }


}
angular.module(moduleName).controller('FeedProfileStatsController', ["$scope", "$http", "$sce", "PaginationDataService", "FeedService", "RestUrlService", "HiveService", "Utils",
                                                                     "BroadcastService", FeedProfileStatsController]);
angular.module(moduleName).directive('thinkbigFeedProfileStats', directive);
