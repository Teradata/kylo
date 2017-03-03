define(['angular',"feed-mgr/module-name"], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            scope: {
                property:'=',
                timePeriods:'=?'
            },
            templateUrl: 'js/feed-mgr/shared/nifi-property-input/nifi-property-timeunit-input.html',
            link: function ($scope, element, attrs) {
                element.addClass('nifi-property-input layout-padding-top-bottom')
                if($scope.propertyDisabled == undefined){
                    $scope.propertyDisabled = false;
                }
                if($scope.timePeriods == undefined){
                    $scope.timePeriods =["","hours","days","weeks","months","years"]
                }

                $scope.units = null;
                $scope.timePeriod = null;

                //convert back and forth to seconds
                var SECONDS_PER_YEAR = 31536000;
                var SECONDS_PER_MONTH =2592000;
                var SECONDS_PER_WEEK = 604800;
                var SECONDS_PER_DAY = 86400;
                var SECONDS_PER_HOUR = 3600;
                var SECONDS_PER_MINUTE = 60;

                /**
                 * convert the seconds to the readable units
                 * @param seconds
                 */
                function populateUnits(seconds)
                {
                    var numyears = Math.floor(seconds / SECONDS_PER_YEAR);
                    var nummonths = Math.floor((seconds % SECONDS_PER_YEAR) / SECONDS_PER_MONTH);
                    var numweeks = Math.floor(((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) / SECONDS_PER_WEEK);
                    var numdays = Math.floor((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) / SECONDS_PER_DAY);
                    var numhours = Math.floor(((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) / SECONDS_PER_HOUR);
                    var numminutes = Math.floor((((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) % SECONDS_PER_HOUR)/ SECONDS_PER_MINUTE);
                    var numseconds = ((((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) % SECONDS_PER_HOUR) % SECONDS_PER_MINUTE);

                    if(numyears >0){
                        $scope.timePeriod = "years";
                        $scope.units = numyears;
                    } else if(nummonths >0){
                        $scope.timePeriod = "months";
                        $scope.units = nummonths;
                    }else if(numweeks >0){
                        $scope.timePeriod = "weeks";
                        $scope.units = numweeks;
                    }
                    else if(numdays >0){
                        $scope.timePeriod = "days";
                        $scope.units = numdays;
                    }
                    else if(numhours >0){
                        $scope.timePeriod = "hours";
                        $scope.units = numhours;
                    }
                    else if(numminutes >0){
                        $scope.timePeriod = "minutes";
                        $scope.units = numminutes;
                    } else if(numseconds >0){
                        $scope.timePeriod = "seconds";
                        $scope.units = numseconds;
                    }

                }

                /**
                 * convert the units to seconds
                 * @returns {*}
                 */
                function getSeconds(){
                    var seconds = null;
                    var timePeriod = $scope.timePeriod;
                    var units = $scope.units;

                    if(timePeriod== 'years'){
                        seconds =  units * SECONDS_PER_YEAR;
                    }
                    else if(timePeriod == 'months' ){
                        seconds =  units * SECONDS_PER_MONTH;
                    }
                    else if(timePeriod == 'weeks' ){
                        seconds =  units * SECONDS_PER_WEEK;
                    }
                    else if(timePeriod == 'days' ){
                        seconds =  units * SECONDS_PER_DAY;
                    }
                    else if(timePeriod == 'hours' ){
                        seconds =  units * SECONDS_PER_HOUR;
                    }
                    else if(timePeriod == 'minutes' ){
                        seconds =  units * SECONDS_PER_MINUTE;
                    }
                    else {
                        seconds = units;
                    }
                    return seconds;
                }

                if($scope.property && $scope.property.value){
                    var seconds = $scope.property.value.substring(0,$scope.property.value.indexOf("sec"));
                    populateUnits(seconds);
                }

                function setPropertyValue(){
                    var seconds = getSeconds();
                    if($scope.units != "") {
                        $scope.property.value = seconds + " seconds";
                    }
                    else {
                        $scope.property.value = "";
                    }
                }

                $scope.$watch('units',function(newVal){
                    setPropertyValue();
                })

                $scope.$watch('timePeriod',function(newVal){
                    setPropertyValue();
                })


            }

        };
    }




    angular.module(moduleName)
        .directive('nifiPropertyTimeUnitInput', directive);

});
