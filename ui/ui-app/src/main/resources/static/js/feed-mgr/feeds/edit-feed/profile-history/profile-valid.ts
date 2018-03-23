import * as angular from 'angular';
import * as _ from "underscore";
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


export class FeedProfileValidResultsController implements ng.IComponentController{
// define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {

    model:any = this.FeedService.editFeedModel;
    loading:boolean = false;
    limitOptions:Array<Number> = [10, 50, 100, 500, 1000];
    limit:Number = this.limitOptions[2];
    headers:any;
    rows:any;
    processingdttm:any;
    queryResults:any =null;
    
    //noinspection JSUnusedGlobalSymbols
    onLimitChange() {
        this.getProfileValidation().then(this.setupTable);
    };


    getProfileValidation(){
        this.loading = true;

        var successFn = (response:any) => {
            var result = this.queryResults = this.HiveService.transformResultsToUiGridModel(response);
            this.headers = result.columns;
            this.rows = result.rows;
            this.loading = false;

            this.BroadcastService.notify('PROFILE_TAB_DATA_LOADED','valid');
        };
        var errorFn = (err:any) => {
            this.loading = false;
        };
        var promise = this.$http.get(this.RestUrlService.FEED_PROFILE_VALID_RESULTS_URL(this.model.id),
            { params:
                {
                    'processingdttm': this.processingdttm,
                    'limit': this.limit
                }
            });
        promise.then(successFn, errorFn);
        return promise;
    }

    setupTable() {
        this.FattableService.setupTable({
            tableContainerId: "validProfile",
            headers: this.headers,
            rows: this.rows
        });
    }

    

    constructor(private $scope:any,private $http:any, private FeedService:any, private RestUrlService:any, private HiveService:any
        , private Utils:any,private BroadcastService:any,private FattableService:any){
            this.getProfileValidation().then(this.setupTable);
    }
    
}

angular.module(moduleName).controller('FeedProfileValidResultsController', ["$scope","$http","FeedService","RestUrlService","HiveService","Utils","BroadcastService","FattableService",FeedProfileValidResultsController]);
angular.module(moduleName).directive('thinkbigFeedProfileValid', directive);
