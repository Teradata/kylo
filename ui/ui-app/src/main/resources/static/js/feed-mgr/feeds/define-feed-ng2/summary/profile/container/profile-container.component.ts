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
    private type: string;
    private hiveService: any;
    private timeInMillis: number | Date;

    private tabs = ['stats', 'valid', 'invalid'];
    private selected = new FormControl(0);

    constructor(private $$angularInjector: Injector) {
        this.hiveService = $$angularInjector.get("HiveService");
    }

    public ngOnInit(): void {
        this.feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.processingdttm = this.stateParams ? this.stateParams.processingdttm : undefined;
        this.timeInMillis = this.hiveService.getUTCTime(this.processingdttm);
        this.type = this.stateParams ? this.stateParams.t : this.tabs[0];
        this.selected.setValue(this.tabs.indexOf(this.type));
    }
}
