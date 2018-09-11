import {Component, Injector, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {FEED_DEFINITION_SECTION_STATE_NAME} from '../../../../../model/feed/feed-constants';
import {Sla} from '../sla.componment';

@Component({
    selector: "sla-list",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/sla/list/sla-list.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/sla/list/sla-list.component.html"
})
export class SlaListComponent implements OnInit {

    @Input() stateParams:any;

    private feedId: string;
    private slaService: any;
    private accessControlService: any;
    private loading: boolean;
    private serviceLevelAgreements: Sla[];
    private allowCreate: boolean;

    constructor(private $$angularInjector: Injector, private state: StateService) {
        this.slaService = $$angularInjector.get("SlaService");
        this.accessControlService = $$angularInjector.get("AccessControlService");

        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (this.accessControlService.hasAction(this.accessControlService.SLA_EDIT, actionSet.actions)) {
                    this.allowCreate = true;
                }
            });
    }

    ngOnInit() {
        this.feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.loadFeedSlas(this.feedId);
    }

    loadFeedSlas(feedId: string) {
        this.slaService.getFeedSlas(feedId).then((response: any) => {
            if (response.data && response.data != undefined && response.data.length > 0) {
                this.serviceLevelAgreements = response.data;
            }
            this.loading = false;
        });
    }

    editExistingSla(sla: Sla): void {
        this.state.go(FEED_DEFINITION_SECTION_STATE_NAME+".sla.edit", {slaId: sla.id});
    }

    createNewSla(): void {
        this.state.go(FEED_DEFINITION_SECTION_STATE_NAME+".sla.new");
    }
}

