import {Component, Injector, Input, OnInit, ViewContainerRef} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {FEED_DEFINITION_SECTION_STATE_NAME} from '../../../../../model/feed/feed-constants';
import * as _ from 'underscore';
import {Sla} from '../sla.componment';
import {Feed} from '../../../../../model/feed/feed.model';
import {FeedLoadingService} from '../../../services/feed-loading-service';
import {FormGroup} from '@angular/forms';
import {LoadingMode, LoadingType, TdLoadingService} from '@covalent/core/loading';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TdDialogService} from '@covalent/core/dialogs';

@Component({
    selector: "sla-details",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/summary/sla/details/sla-details.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/summary/sla/details/sla-details.component.html"
})
export class SlaDetailsComponent implements OnInit {

    private static feedLoader: string = "SlaDetailsComponent.feedLoader";
    private static slaLoader: string = "SlaDetailsComponent.slaLoader";
    private static saveLoader: string = "SlaDetailsComponent.saveLoader";
    private static deleteLoader: string = "SlaDetailsComponent.deleteLoader";

    @Input() stateParams:any;

    private slaId: string;
    private slaService: any;
    private isEditing: boolean;
    private isCreating: boolean;
    private accessControlService: any;
    private policyInputFormService: any;
    allowEdit: boolean;
    private sla: Sla;
    private feedModel: Feed;
    slaForm: FormGroup = new FormGroup({});
    private feedId: string;
    private savingSla = false;
    private loadingFeed = false;
    private loadingSla = false;
    private deletingSla = false;

    constructor(private $$angularInjector: Injector, private state: StateService, private feedLoadingService: FeedLoadingService,
                private loadingService: TdLoadingService, private snackBar: MatSnackBar, private dialogService: TdDialogService,
                private viewContainerRef: ViewContainerRef) {
        this.slaService = $$angularInjector.get("SlaService");
        this.accessControlService = $$angularInjector.get("AccessControlService");
        this.policyInputFormService = $$angularInjector.get("PolicyInputFormService");
        this.createLoader(SlaDetailsComponent.feedLoader);
        this.createLoader(SlaDetailsComponent.slaLoader);
        this.createLoader(SlaDetailsComponent.saveLoader);
        this.createLoader(SlaDetailsComponent.deleteLoader);
    }

    private createLoader(name: string) {
        this.loadingService.create({
            name: name,
            mode: LoadingMode.Indeterminate,
            type: LoadingType.Linear,
            color: 'accent',
        });
    }

    ngOnInit() {
        this.slaId = this.stateParams ? this.stateParams.slaId : undefined;
        if (this.slaId) {
            this.isEditing = true;
            this.isCreating = false;
            this.loadSla(this.slaId);
        } else {
            this.isEditing = false;
            this.isCreating = true;
        }
        this.feedId = this.stateParams ? this.stateParams.feedId : undefined;
        this.loadFeed(this.feedId);
    }

    private loadFeed(feedId:string):void {
        this.loadingService.register(SlaDetailsComponent.feedLoader);
        this.loadingFeed = true;

        this.feedLoadingService.loadFeed(feedId).subscribe((feedModel:Feed) => {
            this.feedModel = feedModel;
            this.loadingService.resolve(SlaDetailsComponent.feedLoader);
            this.loadingFeed = false;
        },(error:any) =>{
            this.loadingService.resolve(SlaDetailsComponent.feedLoader);
            this.loadingFeed = false;
            console.log('error loading feed for id ' + feedId);
        });
    }


    loadSla(slaId: string) {
        this.loadingService.register(SlaDetailsComponent.slaLoader);
        this.loadingSla = true;

        this.slaService.getSlaForEditForm(slaId).then((response: any) => {
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
            this.loadingService.resolve(SlaDetailsComponent.slaLoader);
            this.loadingSla = false;

        }, (err: any) => {
            this.loadingService.resolve(SlaDetailsComponent.slaLoader);
            const msg = err.data.message || 'Error loading the SLA';
            this.loadingSla = false;
            console.error(msg);
            this.snackBar.open('Access denied to edit the SLA', 'OK', { duration: 5000 });
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
        this.savingSla = true;
        this.loadingService.register(SlaDetailsComponent.saveLoader);
        this.slaService.saveFeedSla(this.feedId, this.sla).then((response: any) => {
            this.loadingService.resolve(SlaDetailsComponent.saveLoader);
            this.savingSla = false;
            this.snackBar.open('Saved SLA', 'OK', { duration: 5000 });
            this.state.go(FEED_DEFINITION_SECTION_STATE_NAME+".sla");
        }, function () {
            this.loadingService.resolve(SlaDetailsComponent.saveLoader);
            this.savingSla = false;
            this.snackBar.open('Failed to save SLA', 'OK', { duration: 5000 });
        });
    }

    onCancelSaveSla(): void {
        this.state.go(FEED_DEFINITION_SECTION_STATE_NAME+".sla");
    }

    onDeleteSla(): void {
        this.dialogService.openConfirm({
            message: 'Delete this SLA?',
            disableClose: true,
            viewContainerRef: this.viewContainerRef,
            title: 'Confirm',
            cancelButton: 'Cancel',
            acceptButton: 'Delete',
            width: '500px',
        }).afterClosed().subscribe((accept: boolean) => {
            if (accept) {
                this.doDeleteSla();
            }
        });
    }

    doDeleteSla() {
        this.loadingService.register(SlaDetailsComponent.deleteLoader);
        this.deletingSla = true;

        this.slaService.deleteSla(this.sla.id).then(() => {
            this.loadingService.resolve(SlaDetailsComponent.deleteLoader);
            this.snackBar.open('SLA deleted', 'OK', { duration: 3000 });
            this.deletingSla = false;
            this.state.go(FEED_DEFINITION_SECTION_STATE_NAME+".sla");
        }, () => {
            this.loadingService.resolve(SlaDetailsComponent.deleteLoader);
            this.deletingSla = false;
            this.snackBar.open('Error deleting SLA', 'OK', { duration: 3000 });
        });
    }
}

