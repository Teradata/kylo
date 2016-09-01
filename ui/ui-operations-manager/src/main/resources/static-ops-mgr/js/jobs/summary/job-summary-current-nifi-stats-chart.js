(function () {

    var directive = function () {
        return {
            restrict: "EA",
            scope: true,
            bindToController: {
                panelTitle: "@",
                refreshIntervalTime: "@"
            },
            controllerAs: 'vm',
            templateUrl: 'js/jobs/summary/job-summary-current-nifi-stats-chart.html',
            controller: "JobSummaryCurrentNifiStatsChartController",
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

        this.chartOptions = {
            "chart": {
                "type": "lineChart",
                "height": 450,
                "margin": {
                    "top": 20,
                    "right": 20,
                    "bottom": 40,
                    "left": 55
                },
                x: function (d) {
                    return d.x;
                },
                y: function (d) {
                    return d.y;
                },
                "useInteractiveGuideline": true,
                "dispatch": {},
                "xAxis": {
                    "axisLabel": "Time (ms)"
                },
                "yAxis": {
                    "axisLabel": "#",
                    tickFormat: function (d) {
                        return d3.format('.02f')(d);
                    },
                    "axisLabelDistance": -10
                }
            },
            "title": {
                "enable": true,
                "text": "Total Task Duration (5 min)"
            },
            subtitle: {
                enable: true,
                text: '',
                css: {
                    'text-align': 'center',
                    'margin': '10px 13px 0px 7px'
                }
            },

        };
        self.statusHistory = {};
        self.chartType = null;

        self.chartTypes = [];

        function fetchData() {
            //
            //data pulled from nifi query:
            //http://localhost:8079/nifi-api/controller/process-groups/e9067a58-df67-4ccb-9674-073bdf9e7a3a/status/history
            var statusHistory = {
                "generated": "00:52:54 UTC",
                "details": {"Id": "e9067a58-df67-4ccb-9674-073bdf9e7a3a", "Name": "my_file"},
                "fieldDescriptors": [{
                    "field": "bytesRead",
                    "label": "Bytes Read (5 mins)",
                    "description": "The total number of bytes read from Content Repository by Processors in this Process Group in the past 5 minutes",
                    "formatter": "DATA_SIZE"
                }, {
                    "field": "bytesWritten",
                    "label": "Bytes Written (5 mins)",
                    "description": "The total number of bytes written to Content Repository by Processors in this Process Group in the past 5 minutes",
                    "formatter": "DATA_SIZE"
                }, {
                    "field": "bytesTransferred",
                    "label": "Bytes Transferred (5 mins)",
                    "description": "The total number of bytes read from or written to Content Repository by Processors in this Process Group in the past 5 minutes",
                    "formatter": "DATA_SIZE"
                }, {
                    "field": "inputBytes",
                    "label": "Bytes In (5 mins)",
                    "description": "The cumulative size of all FlowFiles that have entered this Process Group via its Input Ports in the past 5 minutes",
                    "formatter": "DATA_SIZE"
                }, {
                    "field": "inputCount",
                    "label": "FlowFiles In (5 mins)",
                    "description": "The number of FlowFiles that have entered this Process Group via its Input Ports in the past 5 minutes",
                    "formatter": "COUNT"
                }, {
                    "field": "outputBytes",
                    "label": "Bytes Out (5 mins)",
                    "description": "The cumulative size of all FlowFiles that have exited this Process Group via its Output Ports in the past 5 minutes",
                    "formatter": "DATA_SIZE"
                }, {
                    "field": "outputCount",
                    "label": "FlowFiles Out (5 mins)",
                    "description": "The number of FlowFiles that have exited this Process Group via its Output Ports in the past 5 minutes",
                    "formatter": "COUNT"
                }, {"field": "queuedBytes", "label": "Queued Bytes", "description": "The cumulative size of all FlowFiles queued in all Connections of this Process Group", "formatter": "DATA_SIZE"},
                    {"field": "queuedCount", "label": "Queued Count", "description": "The number of FlowFiles queued in all Connections of this Process Group", "formatter": "COUNT"}, {
                        "field": "taskMillis",
                        "label": "Total Task Duration (5 mins)",
                        "description": "The total number of thread-milliseconds that the Processors within this ProcessGroup have used to complete their tasks in the past 5 minutes",
                        "formatter": "DURATION"
                    }],
                "statusSnapshots": [{
                    "timestamp": 1471308424934,
                    "statusMetrics": {
                        "bytesWritten": 1721760,
                        "inputBytes": 0,
                        "outputCount": 0,
                        "bytesTransferred": 3443520,
                        "queuedCount": 697,
                        "bytesRead": 1721760,
                        "queuedBytes": 100005560,
                        "outputBytes": 0,
                        "taskMillis": 41222,
                        "inputCount": 0
                    }
                }, {
                    "timestamp": 1471308484948,
                    "statusMetrics": {
                        "bytesWritten": 3443520,
                        "inputBytes": 0,
                        "outputCount": 0,
                        "bytesTransferred": 6887040,
                        "queuedCount": 706,
                        "bytesRead": 3443520,
                        "queuedBytes": 101296880,
                        "outputBytes": 0,
                        "taskMillis": 101301,
                        "inputCount": 0
                    }
                }, {
                    "timestamp": 1471308544958,
                    "statusMetrics": {
                        "bytesWritten": 5165280,
                        "inputBytes": 0,
                        "outputCount": 0,
                        "bytesTransferred": 10330560,
                        "queuedCount": 715,
                        "bytesRead": 5165280,
                        "queuedBytes": 102588200,
                        "outputBytes": 0,
                        "taskMillis": 161392,
                        "inputCount": 0
                    }
                }, {
                    "timestamp": 1471308604961,
                    "statusMetrics": {
                        "bytesWritten": 6887040,
                        "inputBytes": 0,
                        "outputCount": 0,
                        "bytesTransferred": 13774080,
                        "queuedCount": 724,
                        "bytesRead": 6887040,
                        "queuedBytes": 103879520,
                        "outputBytes": 0,
                        "taskMillis": 221467,
                        "inputCount": 0
                    }
                }, {
                    "timestamp": 1471308664965,
                    "statusMetrics": {
                        "bytesWritten": 8178360,
                        "inputBytes": 0,
                        "outputCount": 0,
                        "bytesTransferred": 16356720,
                        "queuedCount": 730,
                        "bytesRead": 8178360,
                        "queuedBytes": 104740400,
                        "outputBytes": 0,
                        "taskMillis": 281541,
                        "inputCount": 0
                    }
                }, {
                    "timestamp": 1471308724968,
                    "statusMetrics": {
                        "bytesWritten": 6456600,
                        "inputBytes": 0,
                        "outputCount": 0,
                        "bytesTransferred": 12913200,
                        "queuedCount": 727,
                        "bytesRead": 6456600,
                        "queuedBytes": 104309960,
                        "outputBytes": 0,
                        "taskMillis": 300331,
                        "inputCount": 0
                    }
                }]
            };

            if (self.chartTypes.length == 0) {
                var types = [];
                _.each(statusHistory.fieldDescriptors, function (fieldDescriptor) {
                    types.push({label: fieldDescriptor.label, value: fieldDescriptor.field});
                });
                self.chartTypes = types;
            }

            if (self.chartType == null) {
                self.chartType = 'taskMillis';
            }

            self.statusHistory = statusHistory;

            updateChart();
        }

        this.chartData = [];
        var formatStatusHistoryAsChartData = function (key) {
            var fieldDescriptor = getFieldDescriptor(key)
            var xy = [];
            _.each(self.statusHistory.statusSnapshots, function (data) {
                xy.push({x: data.timestamp, y: data.statusMetrics[key]});
            });
            return [{key: fieldDescriptor.label, values: xy}];
        }

        function getFieldDescriptor(key) {
            var fieldDescriptor = _.find(self.statusHistory.fieldDescriptors, function (field) {
                return field.field = key;
            });
            return fieldDescriptor;
        }

        this.onChartTypeChange = function () {
            console.log('change chart type to be ', self.chartType)
            updateChart();
        }

        function updateChart() {
            console.log('CHART TYPE ', self.chartType);
            var fieldDescriptor = getFieldDescriptor(self.chartType)
            var title = fieldDescriptor.label;
            var data = formatStatusHistoryAsChartData(self.chartType)

            console.log('UPDATE CHART ', fieldDescriptor, title, data)
            self.chartOptions.chart.title = title;
            self.chartData = data;
            self.chartOptions.subtitle.text = fieldDescriptor.description;

            if (self.chartApi.updateWithOptions) {
                self.chartApi.updateWithOptions(self.chartOptions);
            }
        }

        fetchData();

        //fetch status
        $timeout(function () {
            self.dataLoaded = true;
        }, 4000);
    };

    angular.module(MODULE_OPERATIONS).controller('JobSummaryCurrentNifiStatsChartController', controller);

    angular.module(MODULE_OPERATIONS)
        .directive('tbaJobSummaryNifiStatsChart', directive);

}());