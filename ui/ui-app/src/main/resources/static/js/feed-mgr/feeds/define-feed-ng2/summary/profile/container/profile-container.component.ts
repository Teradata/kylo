import {Component, Injector, Input, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {FormControl} from '@angular/forms';

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

    private tabs = ['stats', 'valid', 'invalid'];
    private selected = new FormControl(0);

    constructor(private http: HttpClient, private $$angularInjector: Injector) {
        this.restUrlService = $$angularInjector.get("RestUrlService");
        this.hiveService = $$angularInjector.get("HiveService");
    }

    public ngOnInit(): void {
        this.feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.processingdttm = this.stateParams ? this.stateParams.processingdttm : undefined;
        this.timeInMillis = this.hiveService.getUTCTime(this.processingdttm);
        this.type = this.stateParams ? this.stateParams.t : this.tabs[0];
        this.selected.setValue(this.tabs.indexOf(this.type));

        this.getProfileStats();
    }

    private getProfileStats() {
        // this.loading = true;
        const successFn = (response: any) => {
            // this.loading = false;
            this.profileData = response;
        };
        const errorFn = (err: any) => {
            this.loading = false;
        };
        const promise = this.http.get(this.restUrlService.FEED_PROFILE_STATS_URL(this.feedId), {params: {'processingdttm': this.processingdttm}}).toPromise();
        promise.then(successFn, errorFn);
    };

}
