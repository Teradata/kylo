import * as angular from "angular";
import {moduleName} from "../module-name";
import "pascalprecht.translate";

angular.module(moduleName).directive("kyloTimer",
  ['$interval','$filter', () => {
     return {
            restrict: "EA",
            scope: {
                startTime: "=",
                refreshTime: "@",
                truncatedFormat: '=?',
                addAgoSuffix: '=?'
            },
            link: ($scope: any, element: any, attrs: any)=> {

                $scope.truncatedFormat = angular.isDefined($scope.truncatedFormat) ? $scope.truncatedFormat : false;
                $scope.addAgoSuffix = angular.isDefined($scope.addAgoSuffix) ? $scope.addAgoSuffix : false;

                $scope.time = $scope.startTime;
                $scope.previousDisplayStr = '';
                $scope.$watch('startTime', (newVal: any, oldVal: any)=> {
                    $scope.time = $scope.startTime;
                    this.format();
                })
                this.format();
                var seconds = 0;
                var minutes = 0;
                var hours = 0;
                var days = 0;
                var months = 0;
                var years = 0;
                if ($scope.refreshTime == undefined) {
                    $scope.refreshTime = 1000;
                }
                function update() {
                    $scope.time += $scope.refreshTime;
                    //format it
                    this.format();
                }

                function format() {
                    var ms = $scope.time;
                    var displayStr = this.DateTimeUtils(this.$filter('translate')).formatMillisAsText(ms,$scope.truncatedFormat,false);
                    if($scope.addAgoSuffix) {
                        displayStr += " ago";
                    }

                    if ($scope.previousDisplayStr == '' || $scope.previousDisplayStr != displayStr) {
                        element.html(displayStr);
                        element.attr('title', displayStr);
                    }
                    $scope.previousDisplayStr = displayStr;

                }

                var interval = this.$interval(update, $scope.refreshTime);

                var clearInterval = ()=> {
                    this.$interval.cancel(interval);
                    interval = null;
                }
                $scope.$on('$destroy', ()=> {
                    clearInterval()
                });
            }

        }
  }
  ]);