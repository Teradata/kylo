(function () {

    var directive = function () {
        return {
            restrict: "EA",
            scope: {},
            bindToController: {
                panelTitle: "@",
                refreshIntervalTime: "@",
                feedName: '@'
            },
            controllerAs: 'vm',
            templateUrl: 'js/feeds/feed-stats/feed-stats-top-n-processors.html',
            controller: "FeedStatsTopNProcessorsController",
            link: function ($scope, element, attrs) {
                $scope.$on('$destroy', function () {

                });
            } //DOM manipulation\}
        }

    };

    var controller = function ($scope, $element, $http, $interval, $timeout, $q, ProvenanceEventStatsService) {
        var self = this;
        this.dataLoaded = false;

        this.chartApi = {};

        self.chartOptions = {
            chart: {
                type: 'multiBarHorizontalChart',
                height: 400,
                margin: {
                    top: 5, //otherwise top of numeric value is cut off
                    right: 50,
                    bottom: 50, //otherwise bottom labels are not visible
                    left: 100
                },
                duration: 500,
                x: function (d) {
                    return d.label;
                },
                y: function (d) {
                    return d.value;
                },
                showControls: false,
                showValues: true,
                xAxis: {
                    axisLabel: 'Processor',
                    showMaxMin: false
                },
                yAxis: {
                    axisLabel: 'Time (sec) ',
                    tickFormat: function (d) {
                        return d3.format(',.2f')(d);
                    }
                },
                valueFormat: function (d) {
                    return d3.format(',.2f')(d);
                }
            }
        };

        self.chartData = [];

        self.timeframeOptions = [];
        self.timeFrame = 'WEEK';
        self.lastRefreshTime = null;

        function loadTimeFrameOption() {
            ProvenanceEventStatsService.getTimeFrameOptions().then(function (opts) {
                console.log(opts);
                self.timeFrameOptions = opts.data;
            })
        }

        self.onTimeFrameChange = function () {
            clearRefreshInterval();
            buildChartData();
            setRefreshInterval();
        }

        loadTimeFrameOption();

        self.jobsStarted = 0;
        self.jobsFinished = 0;
        self.avgJobDuration = 0;

        function buildChartData() {
            var values = [];

            $q.when(ProvenanceEventStatsService.getFeedProcessorDuration(self.feedName, self.timeFrame)).then(function (processorStats) {
                var jobsStarted = 0;
                var jobsFinished = 0;
                var jobDuration = 0;
                _.each(processorStats.data, function (p) {
                    values.push({label: p.processorName, value: (p.duration / p.totalCount) / 1000});
                    jobsStarted += p.jobsStarted;
                    jobsFinished += p.jobsFinished;
                    jobDuration += p.jobDuration;
                });
                self.dataLoaded = true;
                self.lastRefreshTime = new Date();
                var data = [{key: "Processor", "color": "#1f77b4", values: values}];
                console.log('processorStats', processorStats.data, ' DATA ', data);
                self.chartData = data
                self.jobsStarted = jobsStarted;
                self.jobsFinished = jobsFinished;
                self.avgJobDuration = ((jobDuration / jobsFinished) / 1000).toFixed(2)
            });

        };
        buildChartData();

        setRefreshInterval();

        function clearRefreshInterval() {
            if (self.refreshInterval != null) {
                $interval.cancel(self.refreshInterval);
                self.refreshInterval = null;
            }
        }

        function setRefreshInterval() {
            clearRefreshInterval();
            if (self.refreshIntervalTime) {
                self.refreshInterval = $interval(buildChartData, self.refreshIntervalTime);

            }
        }

        $scope.$on('$destroy', function () {
            clearRefreshInterval();
        });

    };

    angular.module(MODULE_OPERATIONS).controller('FeedStatsTopNProcessorsController', controller);

    angular.module(MODULE_OPERATIONS)
        .directive('tbaFeedStatsTopNProcessorsChart', directive);

}());