import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name:'verboseTimeUnit'})
export class VerboseTimeUnitPipe implements PipeTransform {
    transform(seconds: any): string {
        //convert back and forth to seconds
        var SECONDS_PER_YEAR = 31536000;
        var SECONDS_PER_MONTH = 2592000;
        var SECONDS_PER_WEEK = 604800;
        var SECONDS_PER_DAY = 86400;
        var SECONDS_PER_HOUR = 3600;
        var SECONDS_PER_MINUTE = 60;
        var timePeriod = '';
        var units = +'';
        seconds = seconds.substring(0, seconds.indexOf("sec"));
        if (!isNaN(seconds)) {
            seconds = parseInt(seconds);
        }

        var numyears = Math.floor(seconds / SECONDS_PER_YEAR);
        var nummonths = Math.floor((seconds % SECONDS_PER_YEAR) / SECONDS_PER_MONTH);
        var numweeks = Math.floor(((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) / SECONDS_PER_WEEK);
        var numdays = Math.floor((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) / SECONDS_PER_DAY);
        var numhours = Math.floor(((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) / SECONDS_PER_HOUR);
        var numminutes = Math.floor((((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
        var numseconds = ((((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) % SECONDS_PER_HOUR) % SECONDS_PER_MINUTE);

        if (numyears > 0) {
            timePeriod = "years";
            units = numyears;
        } else if (nummonths > 0) {
            timePeriod = "months";
            units = nummonths;
        } else if (numweeks > 0) {
            timePeriod = "weeks";
            units = numweeks;
        }
        else if (numdays > 0) {
            timePeriod = "days";
            units = numdays;
        }
        else if (numhours > 0) {
            timePeriod = "hours";
            units = numhours;
        }
        else if (numminutes > 0) {
            timePeriod = "minutes";
            units = numminutes;
        } else if (numseconds > 0) {
            timePeriod = "seconds";
            units = numseconds;
        }
        return units + " " + timePeriod;
    }
}