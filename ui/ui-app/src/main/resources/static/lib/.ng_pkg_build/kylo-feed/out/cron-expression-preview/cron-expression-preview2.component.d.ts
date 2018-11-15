import { Injector, OnInit } from "@angular/core";
import { HttpClient } from '@angular/common/http';
import { AsyncValidatorFn } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
export declare class CronExpressionPreviewComponent implements OnInit {
    private $$angularInjector;
    private http;
    private translateService;
    private control;
    nextDates: any;
    private restUrlService;
    private labelNotAvailable;
    constructor($$angularInjector: Injector, http: HttpClient, translateService: TranslateService);
    ngOnInit(): void;
    cronExpressionValidator(): AsyncValidatorFn;
}
