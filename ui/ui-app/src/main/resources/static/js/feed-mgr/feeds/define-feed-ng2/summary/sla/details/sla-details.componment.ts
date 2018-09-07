import {Component, Injector, Input, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {FEED_DEFINITION_SECTION_STATE_NAME} from '../../../../../model/feed/feed-constants';
import * as _ from 'underscore';
import {Sla} from '../sla.componment';
import {Feed} from '../../../../../model/feed/feed.model';
import {Observable} from 'rxjs/Observable';
import {FeedLoadingService} from '../../../services/feed-loading-service';

@Component({
    selector: "sla-details",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/sla/details/sla-details.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/sla/details/sla-details.component.html"
})
export class SlaDetailsComponent implements OnInit {

    @Input() stateParams:any;

    private slaId: string;
    private slaService: any;
    private isEditing: boolean;
    private isCreating: boolean;
    private accessControlService: any;
    private policyInputFormService: any;
    private loading: boolean;
    private allowEdit: boolean;
    private sla: Sla;
    private feedModel: Feed;

        constructor(private $$angularInjector: Injector, private state: StateService, private feedLoadingService: FeedLoadingService) {
        this.slaService = $$angularInjector.get("SlaService");
        this.accessControlService = $$angularInjector.get("AccessControlService");
        this.policyInputFormService = $$angularInjector.get("PolicyInputFormService");
    }

    ngOnInit() {
        console.log("ngOnInit");
        this.slaId = this.stateParams ? this.stateParams.slaId : undefined;
        if (this.slaId) {
            this.isEditing = true;
            this.isCreating = false;
            this.loadSla(this.slaId);
        } else {
            this.isEditing = false;
            this.isCreating = true;
        }
        let feedId = this.stateParams ? this.stateParams.feedId : undefined;
        console.log('feedId = ' + feedId);
        this.loadFeed(feedId);
    }

    private loadFeed(feedId:string):void {
        this.feedLoadingService.loadFeed(feedId).subscribe((feedModel:Feed) => {
            console.log('loaded feed', feedModel);
            this.feedModel = feedModel;
        },(error:any) =>{
            console.log('error loading feed for id ' + feedId);
        });
    }


    loadSla(slaId: string) {
        console.log("loading sla for id " + slaId);
        this.slaService.getSlaForEditForm(slaId).then((response: any) => {
            console.log('got sla', response.data);
            this.sla = response.data;
            this.applyEditPermissionsToSLA(this.sla);
            _.each(this.sla.rules, (rule: any) => {
                rule.editable = this.sla.canEdit;
                rule.mode = 'EDIT';
                rule.groups = this.policyInputFormService.groupProperties(rule);
                this.policyInputFormService.updatePropertyIndex(rule);
            });

            _.each(this.sla.actionConfigurations, (rule: any) => {
                rule.editable = this.sla.canEdit;
                rule.mode = 'EDIT';
                rule.groups = this.policyInputFormService.groupProperties(rule);
                this.policyInputFormService.updatePropertyIndex(rule);
                //validate the rules
                this.slaService.validateSlaActionRule(rule);

            });
            this.loading = false;

        }, (err: any) => {
            const msg = err.data.message || 'Error loading the SLA';
            this.loading = false;
            console.error(msg);
            //todo toast
            // this.$mdDialog.show(
            //     this.$mdDialog.alert()
            //         .clickOutsideToClose(true)
            //         .title("Error loading the SLA")
            //         .textContent(msg)
            //         .ariaLabel("Access denied to edit the SLA")
            //         .ok("OK")
            // );

        });

    }

    private applyEditPermissionsToSLA(sla: any) {
        const entityAccessControlled = this.accessControlService.isEntityAccessControlled();
        this.accessControlService.getUserAllowedActions().then((response: any) => {
            if (entityAccessControlled) {
                sla.editable = sla.canEdit;
                this.allowEdit = sla.canEdit;
            }
            else {
                const allowFeedEdit = this.accessControlService.hasAction(this.accessControlService.FEEDS_EDIT, response.actions);
                const allowSlaEdit = this.accessControlService.hasAction(this.accessControlService.SLA_EDIT, response.actions);
                this.allowEdit = allowFeedEdit && allowSlaEdit;
                sla.editable = this.allowEdit;
            }
        });
    }


    onSaveSla():void {
        console.log('on save sla');
        //todo
        this.state.go(FEED_DEFINITION_SECTION_STATE_NAME+".sla");
    }

    onCancelSaveSla(): void {
        this.state.go(FEED_DEFINITION_SECTION_STATE_NAME+".sla");
    }
}

