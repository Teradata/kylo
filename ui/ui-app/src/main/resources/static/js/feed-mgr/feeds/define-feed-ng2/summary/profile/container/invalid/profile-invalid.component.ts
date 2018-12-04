import {AfterViewInit, Component, ElementRef, Injector, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import 'd3';
import 'nvd3';
import {HttpClient} from '@angular/common/http';
import * as angular from 'angular';
import * as _ from 'underscore';
import * as $ from "jquery";
import {ProfileValidComponent} from '../valid/profile-valid.component';
import {LoadingMode, LoadingType, TdLoadingService} from '@covalent/core/loading';
import {MatSelectChange} from '@angular/material/select';

declare let d3: any;


@Component({
    selector: "profile-invalid",
    styleUrls: ["./profile-invalid.component.scss"],
    templateUrl: "./profile-invalid.component.html"
})
export class ProfileInvalidComponent implements OnInit, AfterViewInit, OnChanges  {

    private static topOfPageLoader: string = "ProfileInvalidComponent.topOfPageLoader";

    @Input()
    feedId: string;

    @Input()
    processingdttm: string;

    @Input()
    offsetHeight: number;

    @Input()
    private active: boolean;
    private activated: boolean = false;

    private feedService: any;
    private restUrlService: any;
    private hiveService: any;
    private fattableService: any;

    data: any = [];
    loadingFilterOptions: boolean = false;
    loadingData: boolean = false;
    limitOptions: Array<string> = ['10', '50', '100', '500', '1000'];
    limit: string = this.limitOptions[2];

    headers: any;
    queryResults: any = [];
    rows: any;

    filterOptions: Array<any> = [
        {name: 'None', objectShortClassType: ''},
        {name: 'Type Conversion', objectShortClassType: 'Not convertible to'}
    ];
    filter: any = this.filterOptions[0];

    private tableId = 'invalidProfile';

    constructor(private $$angularInjector: Injector, private http: HttpClient, private loadingService: TdLoadingService, private hostElement: ElementRef) {

        this.feedService = this.$$angularInjector.get("FeedService");
        this.restUrlService = this.$$angularInjector.get("RestUrlService");
        this.hiveService = this.$$angularInjector.get("HiveService");
        this.fattableService = this.$$angularInjector.get("FattableService");

        this.loadingService.create({
            name: ProfileInvalidComponent.topOfPageLoader,
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

    onFilterChange(event: MatSelectChange) {
        this.filter = event.value;
        this.init();
    }

    private init() {
        this.getFilterOptions();
        this.getProfileValidation().then(this.setupTable.bind(this));
    }

    private getFilterOptions() {
        this.loadingFilterOptions = true;
        const filterOptionsOk = (response: any) => {
            this.filterOptions = _.union(this.filterOptions, response.data);
            this.loadingFilterOptions = false;
        };
        const errorFn = (err: any) => {
            this.loadingFilterOptions = false;
        };

        this.http.get(this.restUrlService.AVAILABLE_VALIDATION_POLICIES, {params: {'cache': 'true'}}).toPromise().then(filterOptionsOk, errorFn);
    }

    private setupTable() {
        if (this.rows && this.rows.length > 0) {
            const parameters = {
                tableContainerId: this.tableId,
                headers: this.headers,
                rows: this.rows,
                rowHeight: 45,
                cellText: (row: any, column: any) => {
                    // console.log('cellText');
                    //return the longest text out of cell value and its validation errors
                    const textArray = [];
                    textArray.push(row[column.displayName]);
                    const validationError = row.invalidFieldMap[column.displayName];
                    if (validationError !== undefined) {
                        textArray.push(validationError.rule);
                        textArray.push(validationError.reason);
                    }
                    return textArray.sort(function (a, b) {
                        return b.length - a.length
                    })[0];
                },
                fillCell: (cellDiv: any, data: any) => {
                    // console.log('fillCell');
                    let html = _.escape(data.value);
                    if (data.isInvalid) {
                        html += '<span class="violation hint">' + data.rule + '</span>';
                        html += '<span class="violation hint">' + data.reason + '</span>';
                        cellDiv.className += " warn";
                    }
                    cellDiv.innerHTML = html;
                },
                getCellSync: (i: any, j: any) => {
                    // console.log('getCellSync');
                    const displayName = this.headers[j].displayName;
                    const row = this.rows[i];
                    if (row === undefined) {
                        //occurs when filtering table
                        return undefined;
                    }
                    const invalidFieldMap = row.invalidFieldMap[displayName];
                    const isInvalid = invalidFieldMap !== undefined;
                    const rule = isInvalid ? invalidFieldMap.rule : "";
                    const reason = isInvalid ? invalidFieldMap.reason : "";
                    return {
                        "value": row[displayName],
                        "isInvalid": isInvalid,
                        "rule": rule,
                        "reason": reason
                    };

                }
            };
            this.fattableService.setupTable(parameters);
        }
    }

    private transformFn(row: any, columns: any, displayColumns: any) {
        const invalidFields: Array<any> = [];
        const invalidFieldMap: Object = {};
        row.invalidFields = invalidFields;
        row.invalidFieldMap = invalidFieldMap;
        row.invalidField = (column: any) => {
            return invalidFieldMap[column];
        };
        let _index = _.indexOf(displayColumns, 'dlp_reject_reason');
        let rejectReasons = row[columns[_index]];
        if (rejectReasons != null) {
            rejectReasons = angular.fromJson(rejectReasons);
        }
        if (rejectReasons != null) {
            angular.forEach(rejectReasons, function (rejectReason) {
                if (rejectReason.scope == 'field') {
                    const field = rejectReason.field;
                    const copy = angular.copy(rejectReason);
                    _index = _.indexOf(displayColumns, field);
                    copy.fieldValue = row[columns[_index]];
                    invalidFields.push(copy);
                    invalidFieldMap[columns[_index]] = copy;
                }
            });
        }
    };

    private getProfileValidation() {
        this.loadingService.register(ProfileInvalidComponent.topOfPageLoader);
        this.loadingData = true;

        const successFn = (response: any) => {
            const result = this.queryResults = this.hiveService.transformResultsToUiGridModel({data: response}, [], this.transformFn.bind(this));
            this.headers = result.columns;
            this.headers = _.reject(this.headers, (col: any) => {
                return col.name == 'dlp_reject_reason'
            });
            this.rows = result.rows;

            this.loadingService.resolve(ProfileInvalidComponent.topOfPageLoader);
            this.loadingData = false;
        };

        const errorFn = (err: any) => {
            this.loadingService.resolve(ProfileInvalidComponent.topOfPageLoader);
            this.loadingData = false;
        };

        const promise = this.http.get(
            this.restUrlService.FEED_PROFILE_INVALID_RESULTS_URL(this.feedId),
            {
                params:
                    {
                        'processingdttm': this.processingdttm,
                        'limit': this.limit,
                        'filter': _.isUndefined(this.filter) ? '' : this.filter.objectShortClassType
                    }
            }).toPromise();
        return promise.then(successFn, errorFn);
    }


    private setTableHeight() {
        const windowHeight = $(window).height();
        const tableHeight = windowHeight - this.offsetHeight;
        const table = this.hostElement.nativeElement.querySelector('#' + this.tableId);
        table.style = 'height: ' + tableHeight + 'px';
    }


}
