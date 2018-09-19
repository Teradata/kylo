import {TranslateService} from "@ngx-translate/core";
import {Injectable} from "@angular/core";


/**
 * ng2 date time service
 */
@Injectable()
export class DateTimeService {
    
    
    constructor(private _translatService:TranslateService) {}


    private padLeft(nr: any, n: any, str: any): any{
        return Array(n-String(nr).length+1).join(str||'0')+nr;
    }

    formatMillis(ms: number) : any{
        let days = Math.floor(ms / (24 * 60 * 60 * 1000));
        var daysms = ms % (24 * 60 * 60 * 1000);
        let hours = Math.floor((daysms) / (60 * 60 * 1000));
        var hoursms = ms % (60 * 60 * 1000);
        let minutes = Math.floor((hoursms) / (60 * 1000));
        var minutesms = ms % (60 * 1000);
        var seconds = Math.floor((minutesms) / (1000));



        var secondsStr = '';
        var minutesStr = '';
        var hoursStr = '';
        var daysStr = '';

        var millisStr = '';
        var str = seconds + ' ' + this._translatService.instant('views.Utils.sec');
        secondsStr = str;
        var truncateFormatStr = str;
        var truncatedTimeFormat = this.padLeft(minutes,2,'0')+":"+this.padLeft(seconds,2,'0');
        var timeFormat = this.padLeft(hours,2,'0')+":"+this.padLeft(minutes,2,'0')+":"+this.padLeft(seconds,2,'0');

        if(seconds == 0 && minutes ==0){
            var roundedMs = Math.ceil((minutesms/1000) * 100)/100;
            millisStr = roundedMs + ' ' + this._translatService.instant('views.Utils.sec');
        }
        if (hours > 0 || (hours == 0 && minutes > 0)) {
            minutesStr = minutes + ' ' + this._translatService.instant('views.Utils.min');
            str = minutesStr + ' '+str;
            truncateFormatStr = minutesStr;
        }
        if (days > 0 || days == 0 && hours > 0) {
            hoursStr = hours + ' ' + this._translatService.instant('views.Utils.hrs');
            str = hoursStr + ' '+ str;
            truncateFormatStr = hoursStr;
            truncatedTimeFormat = this.padLeft(hours,2,'0')+':'+truncatedTimeFormat;
        }
        if (days > 0) {
            daysStr = days + ' ' + this._translatService.instant('views.Utils.days');
            str = daysStr + ' '+str;
            truncateFormatStr = daysStr ;
            truncatedTimeFormat = this.padLeft(days,2,'0')+":"+truncatedTimeFormat;
            timeFormat = this.padLeft(days,2,'0')+":"+timeFormat;
        }


        return {
            str:str,
            truncatedStr:truncateFormatStr,
            timeFormat:timeFormat,
            truncatedTimeFormat:truncatedTimeFormat,
            millisStr: millisStr != '' ? millisStr : str,
            truncatedMillisStr : millisStr != '' ? millisStr : truncatedTimeFormat,
            millisOnly: millisStr != ''
        }

    }

    formatMillisAsText(ms: number,truncate?: boolean, showMillis?: boolean){
        let format = this.formatMillis(ms);
        if(truncate){
            return showMillis ? format.truncatedMillisStr : format.truncatedStr;
        }
        else {
            return showMillis ? format.millisStr : format.str;
        }
    }

    formatMillisAsTime(ms: number,truncate: boolean, showMillis: boolean){
        var format = this.formatMillis(ms);
        if(truncate){
            return showMillis && format.millisOnly ? format.millisStr : format.truncatedTimeFormat;
        }
        else {
            return showMillis && format.millisOnly ? format.millisStr : format.timeFormat;
        }
    }
    
    
}
   

