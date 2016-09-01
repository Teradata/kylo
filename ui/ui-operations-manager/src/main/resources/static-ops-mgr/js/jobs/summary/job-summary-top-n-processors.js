(function () {

    var directive = function () {
        return {
            restrict: "EA",
            scope: {},
            bindToController: {
                panelTitle: "@",
                refreshIntervalTime: "@"
            },
            controllerAs: 'vm',
            templateUrl: 'js/jobs/summary/job-summary-top-n-processors.html',
            controller: "JobSummaryTopNProcessorsController",
            link: function ($scope, element, attrs) {
                $scope.$on('$destroy', function () {

                });
            } //DOM manipulation\}
        }

    };

    var controller = function ($scope, $element, $http, $interval, $timeout) {
        var self = this;
        this.dataLoaded = false;

        this.chartApi = {};

        self.chartOptions = {
            chart: {
                type: 'multiBarHorizontalChart',
                height: 400,
                margin: {
                    top: 5, //otherwise top of numeric value is cut off
                    right: 0,
                    bottom: 25, //otherwise bottom labels are not visible
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
                    showMaxMin: false
                },
                yAxis: {
                    axisLabel: 'Values',
                    tickFormat: function (d) {
                        return d3.format(',.0f')(d);
                    }
                },
                valueFormat: function (d) {
                    return parseInt(d);
                    // return d3.format(',.0f')(d);
                }
            }
        };

        function getRandomInt(min, max) {
            return Math.floor(Math.random() * (max - min + 1)) + min;
        }

        self.chartData = [];

        self.timeframeOptions = [];
        self.timeFrame = 'DAY';

        function loadTimeFrameOption() {
            ProvenanceEventStatsService.getTimeFrameOptions().then(function (opts) {
                console.log(opts);
                var labelValueArr = _.map(opts.data, function (opt) {
                    return {'label': opt.displayName, value: opt.name};
                });
                self.timeFrameOptions = labelValueArr;
            })
        }

        self.onTimeFrameChange = function () {
            buildChartData();
        }

        loadTimeFrameOption();

        function buildChartData() {
            var values = [];

            $q.when(ProvenanceEventStatsService.getFeedProcessorDuration(feedName, self.timeFrame)).then(function (processorStats) {
                _.each(processorStats, function (p) {
                    values.push({label: p.processorName, value: (p.duration / p.totalCount)});
                });
            });

            var data = [{key: "Processor", "color": "#1f77b4", values: values}];
            console.log(' DATA ', data);
            self.chartData = data
        };
        buildChartData();

        //fetch status
        $timeout(function () {
            self.dataLoaded = true;
        }, 4000);
    };

    angular.module(MODULE_OPERATIONS).controller('JobSummaryTopNProcessorsController', controller);

    angular.module(MODULE_OPERATIONS)
        .directive('tbaJobSummaryTopNProcessorsChart', directive);

}());