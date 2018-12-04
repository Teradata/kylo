import {Component, Injector, Input, OnInit} from '@angular/core';
import {MatTabChangeEvent} from '@angular/material/tabs';
import {KyloIcons} from "../../../../../../kylo-utils/kylo-icons";

@Component({
    selector: 'profile-container',
    styleUrls: ['./profile-container.component.scss'],
    templateUrl: './profile-container.component.html',
})
export class ProfileContainerComponent implements OnInit {

    @Input() stateParams:any;

    feedId: string;
    processingdttm: string;
    private type: string;
    private hiveService: any;
    timeInMillis: number | Date;

    private tabs = ['stats', 'valid', 'invalid'];
    selected = 0;

    public kyloIcons_Links_profile = KyloIcons.Links.profile;

    public reference: any; //just to make timeAgo:reference, which appears in template, compile with AOT

    constructor(private $$angularInjector: Injector) {
        this.hiveService = $$angularInjector.get("HiveService");
    }

    public ngOnInit(): void {
        this.feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.processingdttm = this.stateParams ? this.stateParams.processingdttm : undefined;
        this.timeInMillis = this.hiveService.getUTCTime(this.processingdttm);
        this.type = this.stateParams ? this.stateParams.t : this.tabs[0];
        this.selected = this.tabs.indexOf(this.type);
    }

    onSelectedTabChange(event: MatTabChangeEvent) {
        this.selected = event.index;
    }
}
