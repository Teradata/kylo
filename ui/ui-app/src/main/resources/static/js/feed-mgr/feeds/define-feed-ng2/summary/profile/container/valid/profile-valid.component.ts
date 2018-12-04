import {AfterViewInit, Component, ElementRef, Injector, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from "@angular/core";
import 'd3';
import 'nvd3';
import {HttpClient} from '@angular/common/http';
import * as $ from "jquery";
import {ConnectorComponent} from '../../../../../../catalog/connector/connector.component';
import {LoadingMode, LoadingType, TdLoadingService} from '@covalent/core/loading';
import {MatSelectChange} from '@angular/material/select';

declare let d3: any;


@Component({
    selector: "profile-valid",
    styleUrls: ["./profile-valid.component.scss"],
    templateUrl: "./profile-valid.component.html"
})
export class ProfileValidComponent implements OnInit, AfterViewInit, OnChanges  {

    private static topOfPageLoader: string = "ProfileValidComponent.topOfPageLoader";

    @Input()
    feedId: string;

    @Input()
    processingdttm: string;

    @Input()
    offsetHeight: number;

    @Input()
    private active: boolean;
    private activated: boolean = false;

    loading: boolean = false;
    limitOptions: Array<string> = ['10', '50', '100', '500', '1000'];
    limit: string = this.limitOptions[2];
    headers: any;
    rows: any;
    queryResults: any = null;
    private feedService: any;
    private restUrlService: any;
    private hiveService: any;
    private fattableService: any;

    private tableId = 'validProfile';

    constructor(private $$angularInjector: Injector, private http: HttpClient, private loadingService: TdLoadingService, private hostElement: ElementRef) {

        this.feedService = this.$$angularInjector.get("FeedService");
        this.restUrlService = this.$$angularInjector.get("RestUrlService");
        this.hiveService = this.$$angularInjector.get("HiveService");
        this.fattableService = this.$$angularInjector.get("FattableService");

        this.loadingService.create({
            name: ProfileValidComponent.topOfPageLoader,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: 'accent',
        });
    }


    ngOnDestroy() {
        this.fattableService.destroy(this.tableId);
    }

    ngAfterViewInit(): void {
        this.setTableHeight();
    }

    ngOnInit(): void {

    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.active.currentValue && !this.activated) {
            //lazy loading, i.e. loading only when tab is opened for the first time
            this.activated = true;
            this.init();
        }
    }

    onLimitChange(event: MatSelectChange): void {
        this.limit = event.value;
        this.init();
    }

    private getProfileValidation() {
        this.loadingService.register(ProfileValidComponent.topOfPageLoader);

        this.loading = true;

        const successFn = (response: any) => {
            const result = this.queryResults = this.hiveService.transformResultsToUiGridModel({data: response});
            this.headers = result.columns;
            this.rows = result.rows;
            this.loadingService.resolve(ProfileValidComponent.topOfPageLoader);
            this.loading = false;
        };
        const errorFn = (err: any) => {
            this.loadingService.resolve(ProfileValidComponent.topOfPageLoader);
            this.loading = false;
        };

        const promise = this.http.get(this.restUrlService.FEED_PROFILE_VALID_RESULTS_URL(this.feedId), {params: {'processingdttm': this.processingdttm, 'limit': this.limit}}).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    }

    private setupTable() {
        if (this.rows && this.rows.length > 0) {
            this.fattableService.setupTable({
                tableContainerId: this.tableId,
                headers: this.headers,
                rows: this.rows
            });
        }
    }

    private init() {
        this.getProfileValidation().then(this.setupTable.bind(this));
    }

    private setTableHeight() {
        const windowHeight = $(window).height();
        const tableHeight = windowHeight - this.offsetHeight;
        const table = this.hostElement.nativeElement.querySelector('#' + this.tableId);
        table.style = 'height: ' + tableHeight + 'px';
    }

}
