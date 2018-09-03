import {Component, Injector, Input, OnInit} from "@angular/core";
import 'd3';
import 'nvd3';
import {OnChanges, SimpleChanges} from '@angular/core/src/metadata/lifecycle_hooks';
import {HttpClient} from '@angular/common/http';

declare let d3: any;


@Component({
    selector: "profile-valid",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/profile/container/valid/profile-valid.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/profile/container/valid/profile-valid.component.html"
})
export class ProfileValidComponent implements OnInit {

    @Input()
    feedId: string;

    @Input()
    processingdttm: string;

    loading: boolean = false;
    limitOptions: Array<Number> = [10, 50, 100, 500, 1000];
    limit: Number = this.limitOptions[2];
    headers: any;
    rows: any;
    queryResults: any = null;
    private feedService: any;
    private restUrlService: any;
    private hiveService: any;
    private fattableService: any;

    constructor(private $$angularInjector: Injector, private http: HttpClient) {

        this.feedService = this.$$angularInjector.get("FeedService");
        this.restUrlService = this.$$angularInjector.get("RestUrlService");
        this.hiveService = this.$$angularInjector.get("HiveService");
        this.fattableService = this.$$angularInjector.get("FattableService");

    }

    ngOnInit(): void {
        this.init();
    }

    onLimitChange() {
        this.init();
    }

    private getProfileValidation() {
        console.log("getProfileValidation");
        this.loading = true;

        const successFn = (response: any) => {
            console.log("got result");
            const result = this.queryResults = this.hiveService.transformResultsToUiGridModel({data: response});
            this.headers = result.columns;
            this.rows = result.rows;
            this.loading = false;
        };
        const errorFn = (err: any) => {
            console.error('error', err);
            this.loading = false;
        };

        //todo , 'limit': this.limit
        const promise = this.http.get(this.restUrlService.FEED_PROFILE_VALID_RESULTS_URL(this.feedId), {params: {'processingdttm': this.processingdttm}}).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    }

    private setupTable() {
        console.log('setup table');
        this.fattableService.setupTable({
            tableContainerId: "validProfile",
            headers: this.headers,
            rows: this.rows
        });
    }

    private init() {
        this.getProfileValidation().then(this.setupTable.bind(this));
    }

}
