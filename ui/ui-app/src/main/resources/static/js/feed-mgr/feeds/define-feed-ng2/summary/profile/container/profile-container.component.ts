import {Component, Injector, Input, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProfileMetric} from './stats/profile-stats.component';

@Component({
    selector: 'profile-container',
    styleUrls: ['js/feed-mgr/feeds/define-feed-ng2/summary/profile/container/profile-container.component.css'],
    templateUrl: 'js/feed-mgr/feeds/define-feed-ng2/summary/profile/container/profile-container.component.html',
})
export class ProfileContainerComponent implements OnInit {

    @Input() stateParams:any;

    private feedId: string;
    private processingdttm: string;
    private restUrlService: any;
    private profileData: any[];
    private type: string;
    private loading = false;
    private hiveService: any;
    private timeInMillis: number | Date;

    constructor(private http: HttpClient, private $$angularInjector: Injector) {
        // this.feed = data.feed;
        // console.log('feed', this.feed);
        // this.profileRow = data.profileRow;
        // this.currentTab = data.currentTab;
        // this.processingdttm = this.profileRow['PROCESSING_DTTM'];
        // this.processingdate = this.profileRow['DATE'];
        this.restUrlService = $$angularInjector.get("RestUrlService");
        this.hiveService = $$angularInjector.get("HiveService");
    }

    public ngOnInit(): void {
        console.log('ngOnInit');
        this.feedId = this.stateParams ? this.stateParams.feedId : undefined;
        console.log('feedId = ' + this.feedId);
        this.processingdttm = this.stateParams ? this.stateParams.processingdttm : undefined;
        this.timeInMillis = this.hiveService.getUTCTime(this.processingdttm);
        console.log('processingdttm = ' + this.processingdttm);
        this.type = this.stateParams ? this.stateParams.t : undefined;
        console.log('type = ' + this.type);
        this.getProfileStats();
    }

    private getProfileStats() {
        // this.loading = true;
        const successFn = (response: any) => {
            // this.loading = false;
            this.profileData = response;
            console.log('got profile data', response);
        };
        const errorFn = (err: any) => {
            this.loading = false;
        };
        const promise = this.http.get(this.restUrlService.FEED_PROFILE_STATS_URL(this.feedId), {params: {'processingdttm': this.processingdttm}}).toPromise();
        promise.then(successFn, errorFn);
    };

}
