import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../../module-name";;

const SECONDS_PER_YEAR: number = 31536000;
const SECONDS_PER_MONTH: number = 2592000;
const SECONDS_PER_WEEK: number = 604800;
const SECONDS_PER_DAY: number = 86400;
const SECONDS_PER_HOUR: number = 3600;
const SECONDS_PER_MINUTE: number = 60;

class controller {
    property: any;
    timePeriods: any;
    //convert back and forth to seconds


    static readonly $inject = ["$scope, $element"];
    constructor(private $scope: IScope, private element: angular.IAugmentedJQuery) {

        element.addClass('nifi-property-input layout-padding-top-bottom')
        if ($scope.propertyDisabled == undefined) {
            $scope.propertyDisabled = false;
        }
        if ($scope.timePeriods == undefined) {
            $scope.timePeriods = ["", "hours", "days", "weeks", "months", "years"]
        }

        $scope.units = null;
        $scope.timePeriod = null;
        if ($scope.property && $scope.property.value) {
            var seconds = $scope.property.value.substring(0, $scope.property.value.indexOf("sec"));
            this.populateUnits(seconds);
        }
        $scope.$watch('units', (newVal: any) => {
            this.setPropertyValue();
        });

        $scope.$watch('timePeriod', (newVal: any) => {
            this.setPropertyValue();
        });


    }

    setPropertyValue() {
        var seconds = this.getSeconds();
        if (this.$scope.units != "") {
            this.$scope.property.value = seconds + " seconds";
        }
        else {
            this.$scope.property.value = "";
        }
    }
    /**
    * convert the seconds to the readable units
    * @param seconds
    */
    populateUnits(seconds: any) {
        var numyears = Math.floor(seconds / SECONDS_PER_YEAR);
        var nummonths = Math.floor((seconds % SECONDS_PER_YEAR) / SECONDS_PER_MONTH);
        var numweeks = Math.floor(((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) / SECONDS_PER_WEEK);
        var numdays = Math.floor((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) / SECONDS_PER_DAY);
        var numhours = Math.floor(((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) / SECONDS_PER_HOUR);
        var numminutes = Math.floor((((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
        var numseconds = ((((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) % SECONDS_PER_HOUR) % SECONDS_PER_MINUTE);

        if (numyears > 0) {
            this.$scope.timePeriod = "years";
            this.$scope.units = numyears;
        } else if (nummonths > 0) {
            this.$scope.timePeriod = "months";
            this.$scope.units = nummonths;
        } else if (numweeks > 0) {
            this.$scope.timePeriod = "weeks";
            this.$scope.units = numweeks;
        }
        else if (numdays > 0) {
            this.$scope.timePeriod = "days";
            this.$scope.units = numdays;
        }
        else if (numhours > 0) {
            this.$scope.timePeriod = "hours";
            this.$scope.units = numhours;
        }
        else if (numminutes > 0) {
            this.$scope.timePeriod = "minutes";
            this.$scope.units = numminutes;
        } else if (numseconds > 0) {
            this.$scope.timePeriod = "seconds";
            this.$scope.units = numseconds;
        }

    }

    /**
     * convert the units to seconds
     * @returns {*}
     */
    getSeconds() {
        var seconds = null;
        var timePeriod = this.$scope.timePeriod;
        var units = this.$scope.units;

        if (timePeriod == 'years') {
            seconds = units * SECONDS_PER_YEAR;
        }
        else if (timePeriod == 'months') {
            seconds = units * SECONDS_PER_MONTH;
        }
        else if (timePeriod == 'weeks') {
            seconds = units * SECONDS_PER_WEEK;
        }
        else if (timePeriod == 'days') {
            seconds = units * SECONDS_PER_DAY;
        }
        else if (timePeriod == 'hours') {
            seconds = units * SECONDS_PER_HOUR;
        }
        else if (timePeriod == 'minutes') {
            seconds = units * SECONDS_PER_MINUTE;
        }
        else {
            seconds = units;
        }
        return seconds;
    }
}



angular.module(moduleName)
    .component('nifiPropertyTimeUnitInput', {
        controller: controller,
        controllerAs: "vm",
        templateUrl: './nifi-property-timeunit-input.html',
        bindings: {
            property: '=',
            timePeriods: '=?'
        },
    });
