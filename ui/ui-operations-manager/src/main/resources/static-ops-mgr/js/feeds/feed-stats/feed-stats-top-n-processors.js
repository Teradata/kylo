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

        this.statusPieChartApi = {};

        self.processorDurationChartOptions = {
            chart: {
                type: 'multiBarHorizontalChart',
                height: 400,
                margin: {
                    top: 5, //otherwise top of numeric value is cut off
                    right: 50,
                    bottom: 50, //otherwise bottom labels are not visible
                    left: 150
                },
                duration: 500,
                x: function (d) {
                    return d.label.length > 100 ? d.label.substr(0, 100) + "..." : d.label;
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

        self.processorDurationChartData = [];

        this.statusPieChartApi = {};

        this.statusPieChartOptions = {
            chart: {
                type: 'pieChart',
                x: function (d) {
                    return d.key;
                },
                y: function (d) {
                    return d.value;
                },
                showLabels: false,
                duration: 100,
                "height": 150,
                labelThreshold: 0.01,
                labelSunbeamLayout: false,
                "margin": {"top": 10, "right": 10, "bottom": 10, "left": 10},
                donut: false,
                // donutRatio: 0.65,
                showLegend: false,
                valueFormat: function (d) {
                    return parseInt(d);
                },
                color: function (d) {
                    if (d.key == 'Successful Jobs') {
                        return '#009933';
                    }
                    else if (d.key == 'Failed Jobs') {
                        return '#FF0000';
                    }
                    else if (d.key == 'Running Jobs') {
                        return '#FF9901';
                    }
                },
                dispatch: {
                    renderEnd: function () {

                    }
                }
            }
        };











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
        self.jobsFailed = 0;
        self.avgJobDuration = 0;

        //stats for pie chart
        self.jobsRunning = 0;
        self.jobsSuccess = 0;

        function buildChartData() {
            var values = [];
            var processorNameCount = {};
            var processorIdNameMap = {};





            $q.when(ProvenanceEventStatsService.getFeedProcessorDuration(self.feedName, self.timeFrame)).then(function (processorStats) {
                var jobsStarted = 0;
                var jobsFinished = 0;
                var jobDuration = 0;
                var jobsFailed = 0;
                var jobsSuccess = 0;
                var jobsRunning = 0;
                _.each(processorStats.data, function (p) {

                    if (processorIdNameMap[p.processorId] == undefined) {
                        if (processorNameCount[p.processorName] == undefined) {
                            processorNameCount[p.processorName] = 0;
                            processorIdNameMap[p.processorId] = p.processorName;
                        }
                        else {
                            processorNameCount[p.processorName] = processorNameCount[p.processorName] + 1;
                            processorIdNameMap[p.processorId] = p.processorName + " - " + processorNameCount[p.processorName];
                        }
                    }
                    var processorName = processorIdNameMap[p.processorId];

                    values.push({label: processorName, value: (p.duration / p.totalCount) / 1000});
                    jobsStarted += p.jobsStarted;
                    jobsFinished += p.jobsFinished;
                    jobDuration += p.jobDuration;
                    jobsFailed += p.jobsFailed;
                    jobsSuccess = (jobsFinished - jobsFailed);
                    jobsRunning = (jobsStarted - jobsFinished) < 0 ? 0 : (jobsStarted - jobsFinished);
                });
                self.dataLoaded = true;
                self.lastRefreshTime = new Date();
                var data = [{key: "Processor", "color": "#1f77b4", values: values}];
                self.processorDurationChartData = data
                self.jobsStarted = jobsStarted;
                self.jobsFinished = jobsFinished;
                self.jobsFailed = jobsFailed;
                self.jobsRunning = jobsRunning;
                self.jobsSuccess = jobsSuccess
                self.avgJobDuration = ((jobDuration / jobsFinished) / 1000).toFixed(2)

                self.statusPieChartData = [];
                self.statusPieChartData.push({key: "Successful Jobs", value: self.jobsSuccess})
                self.statusPieChartData.push({key: "Failed Jobs", value: self.jobsFailed})
                self.statusPieChartData.push({key: "Running Jobs", value: self.jobsRunning})

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