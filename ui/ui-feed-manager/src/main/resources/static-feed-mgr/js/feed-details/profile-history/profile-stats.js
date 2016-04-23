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
            templateUrl: 'js/feed-details/profile-history/profile-stats.html',
            controller: "FeedProfileStatsController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller =  function($scope,$http, $sce,$stateParams, FeedService, RestUrlService, HiveService, Utils, BroadcastService) {

        var self = this;

        this.model = FeedService.editFeedModel;
        this.data = [];
        this.loading = false;


        this.processingDate = new Date(HiveService.getUTCTime(self.processingdttm));

        function getProfileStats(){
            self.loading = true;
            var successFn = function (response) {
                var transformFn = function(row,columns,displayColumns){
                    var _index = _.indexOf(displayColumns,'metrictype');
                    var metricType = row[columns[_index]];
                    if(metricType == 'TOP_N_VALUES')
                    {
                        _index = _.indexOf(displayColumns,'metricvalue');
                        var val = row[columns[_index]];
                        if(val) {
                            var newVal = '';
                            angular.forEach(val.split('^B'),function(row) {
                                var itemArr = row.split('^A');
                                if(itemArr != undefined && itemArr.length ==3) {
                                    var item = itemArr[0] + "." + itemArr[1] + " (" + itemArr[2] + ") \n";
                                    newVal += item;
                                }
                            });
                        row[columns[_index]] = newVal;
                        }
                    }
                    else {
                        if( row[columns[_index]].indexOf('script') >=0){
                            console.log('****** FOUND IT!!!!******',row)
                        }
                    }

                };
                var data = HiveService.transformResults2(response,['processing_dttm'],transformFn);
                self.data = data;
                self.loading = false;
                BroadcastService.notify('PROFILE_TAB_DATA_LOADED','profile-stats');
            }
            var errorFn = function (err) {
                self.loading = false;
            }
            var promise = $http.get(RestUrlService.FEED_PROFILE_STATS_URL(self.model.id),{params:{'processingdttm':self.processingdttm}});
            promise.then(successFn, errorFn);
            return promise;
        }

        getProfileStats();
    };


    angular.module(MODULE_FEED_MGR).controller('FeedProfileStatsController', controller);
    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedProfileStats', directive);


})();
