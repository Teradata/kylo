import {Component, Injector, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import {HttpClient} from '@angular/common/http';

declare const CodeMirror: any;

@Component({
    selector: "cron-expression-preview2",
    styleUrls:  ["js/feed-mgr/shared/cron-expression-preview/cron-expression-preview2.component.css"],
    templateUrl: "js/feed-mgr/shared/cron-expression-preview/cron-expression-preview2.component.html"
})
export class CronExpressionPreviewComponent implements OnInit, OnChanges {

    @Input()
    private expression: string;
    nextDates: any[];
    private restUrlService: any;
    private invalid = false;

    constructor(private $$angularInjector: Injector, private http: HttpClient) {
        this.restUrlService = $$angularInjector.get("RestUrlService");
    }

    ngOnInit() {
        this.getNextDates();
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.getNextDates();
    }

    getNextDates() {
        this.invalid = false;
        this.http.get(this.restUrlService.PREVIEW_CRON_EXPRESSION_URL, {params: {cronExpression: this.expression}}).toPromise()
            .then((response: any) => {
                this.nextDates = response;
            }, (error: any) => {
                this.invalid = true;
            });
    }
}