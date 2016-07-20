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
    };

    var controller =  function($scope, $http, $sce,$stateParams, PaginationDataService, FeedService, RestUrlService, HiveService, Utils, BroadcastService) {

        var self = this;
        self.data = [];
        self.loading = true;
        self.processingDate = new Date(HiveService.getUTCTime(self.processingdttm));
        self.model = FeedService.editFeedModel;
        self.selectedRow = [];

        self.selectRow = function(event, row) {
            self.selectedRow = row;
            self.summaryApi.update();
        };

        self.summaryApi = {};
        self.summaryOptions = {
            chart: {
                type: 'discreteBarChart',
                showYAxis: false,
                showXAxis: true,
                color: function (d, i) {
                    return '#1f77b4'; //see if can be done via css
                },
                margin : {
                    top: 0,
                    right: 0,
                    bottom: 20,
                    left: 0
                },
                x: function(d){return d.label;},
                y: function(d){return d.value + (1e-10);},
                showValues: true,
                valueFormat: function(d){
                    return d3.format(',.0f')(d);
                },
                duration: 500
            }
        };

        self.summaryData = function() {
            var filtered = _.filter(self.data.rows, function(row){ return row.columnname == self.selectedRow.columnname; });

            function findStat(rows, metrictype) {
                var row = _.find(rows, function(row){ return row.metrictype == metrictype; });
                return _.isUndefined(row) || _.isUndefined(row.metricvalue) ? 0 : Number(row.metricvalue);
            }
            var total = findStat(filtered, 'TOTAL_COUNT');
            var nulls = findStat(filtered, 'NULL_COUNT');
            var empty = findStat(filtered, 'EMPTY_COUNT');
            var unique = findStat(filtered, 'UNIQUE_COUNT');
            var invalid = findStat(filtered, 'INVALID_COUNT');

            return [{
                key: "Summary",
                values: [
                    {
                        "label": "Total",
                        "value": total
                    },
                    {
                        "label": "Valid",
                        "value": total - invalid
                    },
                    {
                        "label": "Invalid",
                        "value": invalid
                    },
                    {
                        "label": "Unique",
                        "value": unique
                    },
                    {
                        "label": "Missing",
                        "value": nulls + empty
                    }
                ]
            }];
        };

        function getProfileStats(){
            self.loading = true;
            var successFn = function (response) {
                var transformFn = function(row,columns,displayColumns){
                    var _index = _.indexOf(displayColumns,'metrictype');
                    var metricType = row[columns[_index]];
                    if(metricType == 'TOP_N_VALUES') {
                        _index = _.indexOf(displayColumns,'metricvalue');
                        var val = row[columns[_index]];
                        if(val) {
                            var newVal = '';
                            angular.forEach(val.split('^B'),function(row) {
                                var itemArr = row.split('^A');
                                if(itemArr != undefined && itemArr.length ==3) {
                                    newVal += itemArr[0] + "." + itemArr[1] + " (" + itemArr[2] + ") \n";
                                }
                            });
                            row[columns[_index]] = newVal;
                        }
                    }

                };
                self.data = HiveService.transformResults2(response, ['processing_dttm'], transformFn);
                if (self.data && self.data.rows && self.data.rows.length > 0) {
                    self.selectedRow = self.data.rows[1];
                }
                console.log(self.data);
                self.loading = false;
                BroadcastService.notify('PROFILE_TAB_DATA_LOADED','profile-stats');
            };
            var errorFn = function (err) {
                self.loading = false;
            };
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
