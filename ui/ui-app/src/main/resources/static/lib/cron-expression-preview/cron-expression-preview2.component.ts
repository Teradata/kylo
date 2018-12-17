import {Component, Injector, Input, OnInit} from "@angular/core";
import {HttpClient} from '@angular/common/http';
import {AbstractControl, AsyncValidatorFn, FormControl, ValidationErrors} from '@angular/forms';
import {catchError, map} from 'rxjs/operators';
import {TranslateService} from '@ngx-translate/core';
import {Observable} from 'rxjs/Observable';
import {of} from 'rxjs/observable/of';

declare const CodeMirror: any;

@Component({
    selector: "cron-expression-preview2",
    styleUrls: ["./cron-expression-preview2.component.scss"],
    templateUrl: "./cron-expression-preview2.component.html"
})
export class CronExpressionPreviewComponent implements OnInit {

    @Input()
    private control: FormControl;
    nextDates: any;
    private restUrlService: any;
    private labelNotAvailable: string;

    constructor(private $$angularInjector: Injector, private http: HttpClient, private translateService: TranslateService) {
        this.restUrlService = $$angularInjector.get("RestUrlService");
        this.labelNotAvailable = this.translateService.instant('views.cron-expression-preview.PreviewNotAvailable');
    }

    ngOnInit() {
        this.control.setAsyncValidators(this.cronExpressionValidator());
        this.control.updateValueAndValidity(); //updateValueAndValidity so that we get preview of the initial default value
    }

    cronExpressionValidator(): AsyncValidatorFn {

        return (control: AbstractControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> => {
            return this.http.get(this.restUrlService.PREVIEW_CRON_EXPRESSION_URL, {params: {cronExpression: control.value}}).pipe(
                map(response => {
                    this.nextDates = response;
                    return null;
                }),
                catchError(() => {
                    this.nextDates = [this.labelNotAvailable];
                    return of(true);
                })
            );
        };
    }
}
