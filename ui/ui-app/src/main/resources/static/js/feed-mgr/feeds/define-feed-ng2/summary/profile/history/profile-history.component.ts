import {Component, Injector, Input, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import * as _ from 'underscore';
import * as angular from 'angular';
import {StateService} from "@uirouter/angular";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_SUMMARY_STATE_NAME} from '../../../../../model/feed/feed-constants';
import {KyloIcons} from "../../../../../../kylo-utils/kylo-icons";

@Component({
    selector: 'profile-history',
    styleUrls: ['js/feed-mgr/feeds/define-feed-ng2/summary/profile/history/profile-history.component.scss'],
    templateUrl: 'js/feed-mgr/feeds/define-feed-ng2/summary/profile/history/profile-history.component.html',
})
export class ProfileHistoryComponent implements OnInit {

    @Input() stateParams:any;

    private feedId: string;
    private processingdttm: string;
    private restUrlService: any;
    private hiveService: any;
    private utils: any;
    private showSummary: boolean = true;
    private profileSummary: Array<any> = [];
    private loading: boolean = false;
    private showNoResults: boolean = false;

    public kyloIcons:KyloIcons = KyloIcons;

    constructor(private $$angularInjector: Injector, private http: HttpClient, private state: StateService) {
        this.hiveService = $$angularInjector.get("HiveService");
        this.utils = $$angularInjector.get("Utils");
        this.restUrlService = $$angularInjector.get("RestUrlService");
    }


    public ngOnInit(): void {
        this.feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.processingdttm = this.stateParams ? this.stateParams.processingdttm : undefined;
        this.getProfileHistory();
    }

    private getProfileHistory() {
        this.loading = true;
        this.showNoResults = false;
        const successFn = (response: any) => {
            if (response.length == 0) {
                this.showNoResults = true;
            }
            const dataMap: any = {};
            let dataArr: any = [];
            const columns: any = this.hiveService.getColumnNamesForQueryResult(response);
            if (columns != null) {
                //get the keys into the map for the different columns
                const dateColumn: any = _.find(columns, (column) => {
                    return this.utils.strEndsWith(column, 'processing_dttm');
                });

                const metricTypeColumn: any = _.find(columns, (column) => {
                    return this.utils.strEndsWith(column, 'metrictype');
                });

                const metricValueColumn: any = _.find(columns, (column) => {
                    return this.utils.strEndsWith(column, 'metricvalue');
                });

                //group on date column
                angular.forEach(response, (row: any) => {
                    const date = row[dateColumn];
                    if (dataMap[date] == undefined) {
                        const timeInMillis = this.hiveService.getUTCTime(date);
                        const obj = {'PROCESSING_DTTM': date, 'DATE_TIME': timeInMillis, 'DATE': new Date(timeInMillis)};
                        dataMap[date] = obj;
                        dataArr.push(obj);
                    }
                    const newRow = dataMap[date];
                    const metricType = row[metricTypeColumn];
                    let value = row[metricValueColumn];
                    if (value && metricType == 'MIN_TIMESTAMP' || metricType == 'MAX_TIMESTAMP') {
                        //first check to see if it is millis
                        if (!isNaN(value)) {
                            //tmp was passed as which is not declared anywhere. was returning 'Invalid Date'// replaced by '' here
                            value = ''; //this.$filter('date')(new Date(''), "yyyy-MM-dd");
                        }
                        else {
                            value = value.substr(0, 10); //string the time off the string
                        }

                    }

                    newRow[metricType] = value;
                });

                //sort it desc

                // dataArr = _.sortBy(dataArr,dateColumn).reverse();
                dataArr = _.sortBy(dataArr, 'DATE_TIME').reverse();

                this.profileSummary = dataArr;


            }
            this.loading = false;

        };
        const errorFn = (err: any) => {
            console.log('ERROR ', err);
            this.loading = false;
        };

        this.http.get(this.restUrlService.FEED_PROFILE_SUMMARY_URL(this.feedId)).toPromise().then(successFn, errorFn);
    };

    onValidCountClick(row: any) {
        this.goToResults(row, 'valid');
    }

    onInvalidCountClick(row: any) {
        this.goToResults(row, 'invalid');
    }

    public viewProfileResults(row: any) {
        this.goToResults(row, 'stats');
    };

    goToResults(row: any, type: string) {
        this.state.go(FEED_DEFINITION_SUMMARY_STATE_NAME+".profile.results", {processingdttm: row['PROCESSING_DTTM'], t: type});
    }


}
