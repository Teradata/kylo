define(['angular','ops-mgr/overview/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            scope: true,
            bindToController: {
                panelTitle: "@",
                refreshIntervalTime: "@"
            },
            controllerAs: 'vm',
            templateUrl: 'js/ops-mgr/overview/data-confidence-indicator/data-confidence-indicator-template.html',
            controller: "DataConfidenceIndicatorController",
            link: function ($scope, element, attrs) {
                $scope.$on('$destroy', function () {

                });
            } //DOM manipulation\}
        }

    };

    var controller = function ($scope, $element, $http, $interval, OpsManagerJobService) {
        var self = this;
        this.refreshInterval = null;
        this.refreshing = false;
        this.dataLoaded = false;

        this.dataMap = {'Healthy':{count:0, color:'#5cb85c'},'Unhealthy':{count:0,color:'#a94442'}};

        this.chartApi = {};

        this.chartOptions = {
            chart: {
                type: 'pieChart',
                x: function(d){return d.key;},
                y: function(d){return d.value;},
                showLabels: false,
                duration: 100,
                height:150,
                labelThreshold: 0.01,
                labelSunbeamLayout: false,
                "margin":{"top":10,"right":10,"bottom":10,"left":10},
                donut:true,
                donutRatio:0.65,
                showLegend:false,
                refreshDataOnly: false,
                valueFormat: function(d){
                    return parseInt(d);
                },
                color:function(d){
                    if(d.key == 'Healthy'){
                        return '#009933';
                    }
                    else if( d.key== 'Unhealthy'){
                        return '#FF0000';
                    }
                },
                dispatch: {

                }
            }
        };
        this.chartData = [];

        this.updateChart = function(){
            angular.forEach(self.chartData,function(row,i){
                row.value = self.dataMap[row.key].count;
            });
            var title = (self.dataMap.Healthy.count+self.dataMap.Unhealthy.count)+" Total";
            self.chartOptions.chart.title=title
            self.dataLoaded = true;
            if(self.chartApi.update) {
                self.chartApi.update();
            }
        }


        function initializePieChart() {
            self.chartData.push({key: "Healthy", value: 0})
            self.chartData.push({key: "Unhealthy", value: 0})
        }


        this.getDataConfidenceSummary = function () {
            if (self.refreshing == false) {
                self.refreshing = true;
                var successFn = function (response) {
                    var data = response.data;
                    if (self.dataLoaded == false) {
                        self.dataLoaded = true;
                    }
                    self.dataMap.Healthy.count = data.successCount;
                    self.dataMap.Unhealthy.count = data.failedCount;
                    self.updateChart();
                }
                var errorFn = function (err) {
                    self.chartOptions.chart.title = "N/A";
                    self.dataMap.Healthy.count = 0;
                    self.dataMap.Unhealthy.count = 0;
                }
                var finallyFn = function () {
                    self.refreshing = false;
                }
                $http.get(OpsManagerJobService.DATA_CONFIDENCE_URL).then(successFn).catch(errorFn).finally(finallyFn);
            }
        }


        this.clearRefreshInterval = function () {
            if (self.refreshInterval != null) {
                $interval.cancel(self.refreshInterval);
                self.refreshInterval = null;
            }
        }

        this.setRefreshInterval = function () {
            self.clearRefreshInterval();
            if (self.refreshIntervalTime) {
                self.refreshInterval = $interval(self.getDataConfidenceSummary, self.refreshIntervalTime);

            }
        }

        this.init = function () {
            initializePieChart();
            self.getDataConfidenceSummary();
          self.setRefreshInterval();
        }

        this.init();

        $scope.$on('$destroy', function () {
            self.clearRefreshInterval();
        });
    };

    angular.module(moduleName).controller('DataConfidenceIndicatorController',["$scope","$element","$http","$interval","OpsManagerJobService", controller]);


    angular.module(moduleName)
        .directive('tbaDataConfidenceIndicator', directive);

});
