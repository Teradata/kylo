import * as angular from "angular";
import { Directive, Input, ElementRef, SimpleChanges, Inject } from '@angular/core';
import "pascalprecht.translate";
import { TranslateService } from "@ngx-translate/core";
import {DateTimeService} from "../utils/date-time.service";

@Directive({
    selector: '[kylo-timer]'
})
export class KyloTimerDirective {

    @Input() startTime: any;
    @Input() truncatedFormat: boolean;
    @Input() addAgoSuffix: any;

    refreshTime: number;
    time: number;
    previousDisplayStr: string;
    interval: any;

    constructor(private elRef: ElementRef,
                private _dateTimeService:DateTimeService) {}

    clearInterval(): void {
        clearInterval(this.interval);
        this.interval = null;
    }

    ngOnDestroy(): void {
        this.clearInterval();
    }

    update() {
        this.time += this.refreshTime;
        //format it
        this.format();
    }

    format(): void {
        var ms = this.time;
        var displayStr = this._dateTimeService.formatMillisAsText(ms,this.truncatedFormat,false);
        if(this.addAgoSuffix) {
            displayStr += " ago";
        }

        if (this.previousDisplayStr == '' || this.previousDisplayStr != displayStr) {
            $(this.elRef.nativeElement).html(displayStr);
            $(this.elRef.nativeElement).attr('title', displayStr);
        }
        this.previousDisplayStr = displayStr;

    }

    ngOnChanges(changes: SimpleChanges): void {
        if (!changes.startTime.firstChange) {
            this.time = changes.startTime.currentValue;
            this.format();
        }
    }

    ngOnInit(): void {
        this.truncatedFormat = angular.isDefined(this.truncatedFormat) ? this.truncatedFormat : false;
        this.addAgoSuffix = angular.isDefined(this.addAgoSuffix) ? this.addAgoSuffix : false;

        this.time = this.startTime;
        this.previousDisplayStr = '';

        this.format();

        if (this.refreshTime == undefined) {
            this.refreshTime = 1000;
        }

        this.interval = setInterval(this.update.bind(this), this.refreshTime);

    }

}

