import {Component, Injector, Input, OnInit, ViewContainerRef} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_STATE_NAME, FEED_DEFINITION_SUMMARY_STATE_NAME} from '../../../../../model/feed/feed-constants';
import * as _ from 'underscore';
import {Sla} from '../sla.componment';
import {Feed, FeedState} from '../../../../../model/feed/feed.model';
import {FeedLoadingService} from '../../../services/feed-loading-service';
import {FormGroup} from '@angular/forms';
import {LoadingMode, LoadingType, TdLoadingService} from '@covalent/core/loading';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TdDialogService} from '@covalent/core/dialogs';
import {TranslateService} from '@ngx-translate/core';
import AccessConstants from '../../../../../../constants/AccessConstants';

export enum FormMode {
    ModeEdit = "EDIT",
    ModeNew = "NEW",
}

export class RuleType {
    name: string;
    mode: FormMode;

    constructor() {
        this.name = '';
        this.mode = FormMode.ModeNew;
    }
}

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
    private accessControlService: any;
    private policyInputFormService: any;
    allowEdit = false;
    private sla: Sla;
    private feedModel: Feed;
    slaForm: FormGroup = new FormGroup({});
    private feedId: string;
    private savingSla = false;
    private loadingFeed = false;
    private loadingSla = false;
    private deletingSla = false;
    private mode: FormMode;
    editMode = FormMode.ModeEdit;
    newMode = FormMode.ModeNew;
    stateDisabled = FeedState.DISABLED;
    private labelErrorLoadingSla: string;
    private labelAccessDenied: string;
    private labelOk: string;
    private labelSavedSla: string;
    private labelFailedToSaveSla: string;
    private labelDeleteSla: string;
    private labelConfirm: string;
    private labelCancel: string;
    private labelDelete: string;
    private labelDeleted: string;
    private labelErrorDeletingSla: string;

    constructor(private $$angularInjector: Injector, private state: StateService, private feedLoadingService: FeedLoadingService,
                private loadingService: TdLoadingService, private snackBar: MatSnackBar, private dialogService: TdDialogService,
                private viewContainerRef: ViewContainerRef, private translateService: TranslateService) {
        this.slaService = $$angularInjector.get("SlaService");
        this.accessControlService = $$angularInjector.get("AccessControlService");
        this.policyInputFormService = $$angularInjector.get("PolicyInputFormService");
        this.createLoader(SlaDetailsComponent.feedLoader);
        this.createLoader(SlaDetailsComponent.slaLoader);
        this.createLoader(SlaDetailsComponent.saveLoader);
        this.createLoader(SlaDetailsComponent.deleteLoader);

        this.labelErrorLoadingSla = this.translateService.instant("Sla.Details.ErrorLoadingSla");
        this.labelAccessDenied = this.translateService.instant("Sla.Details.AccessDenied");
        this.labelOk = this.translateService.instant("Sla.Details.Ok");
        this.labelSavedSla = this.translateService.instant("Sla.Details.SavedSla");
        this.labelFailedToSaveSla = this.translateService.instant("Sla.Details.FailedToSaveSla");
        this.labelDeleteSla = this.translateService.instant("Sla.Details.DeleteSla");
        this.labelConfirm = this.translateService.instant("Sla.Details.Confirm");
        this.labelCancel = this.translateService.instant("Sla.Details.Cancel");
        this.labelDelete = this.translateService.instant("Sla.Details.Delete");
        this.labelDeleted = this.translateService.instant("Sla.Details.DeletedSla");
        this.labelErrorDeletingSla = this.translateService.instant("Sla.Details.ErrorDeletingSla");

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
            this.loadSla(this.slaId);
            this.mode = FormMode.ModeEdit;
        } else {
            this.mode = FormMode.ModeNew;
            this.sla = new Sla();
            //allow the user to edit this SLA if it is new
            this.sla.canEdit = true;
            this.applyEditPermissionsToSLA(this.sla);

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
                rule.mode = FormMode.ModeEdit;
                rule.groups = this.policyInputFormService.groupProperties(rule);
                this.policyInputFormService.updatePropertyIndex(rule);
            });

            _.each(this.sla.actionConfigurations, (rule: any) => {
                rule.editable = this.sla.canEdit;
                rule.mode = FormMode.ModeEdit;
                rule.groups = this.policyInputFormService.groupProperties(rule);
                this.policyInputFormService.updatePropertyIndex(rule);
                //validate the rules
                this.slaService.validateSlaActionRule(rule);

            });
            this.loadingService.resolve(SlaDetailsComponent.slaLoader);
            this.loadingSla = false;

        }, (err: any) => {
            this.loadingService.resolve(SlaDetailsComponent.slaLoader);
            const msg = err.data.message || this.labelErrorLoadingSla;
            this.loadingSla = false;
            console.error(msg);
            this.snackBar.open(this.labelAccessDenied, this.labelOk, { duration: 5000 });
        });

    }

    private applyEditPermissionsToSLA(sla: Sla) {
        const entityAccessControlled = this.accessControlService.isEntityAccessControlled();
        this.accessControlService.getUserAllowedActions().then((response: any) => {
            const allowFeedEdit = this.accessControlService.hasAction(AccessConstants.FEEDS_EDIT, response.actions);
            const allowSlaEdit = this.accessControlService.hasAction(AccessConstants.SLA_EDIT, response.actions);

            if (entityAccessControlled) {
                this.allowEdit = sla.canEdit && allowSlaEdit && allowFeedEdit;
                sla.editable =  this.allowEdit;
            }
            else {
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
            this.snackBar.open(this.labelSavedSla, this.labelOk, { duration: 3000 });
            this.state.go(FEED_DEFINITION_SUMMARY_STATE_NAME+".sla");
        }, function () {
            this.loadingService.resolve(SlaDetailsComponent.saveLoader);
            this.savingSla = false;
            this.snackBar.open(this.labelFailedToSaveSla, this.labelOk, { duration: 3000 });
        });
    }

    onCancelSaveSla(): void {
        this.state.go(FEED_DEFINITION_SUMMARY_STATE_NAME+".sla");
    }

    onDeleteSla(): void {
        this.dialogService.openConfirm({
            message: this.labelDeleteSla,
            disableClose: true,
            viewContainerRef: this.viewContainerRef,
            title: this.labelConfirm,
            cancelButton: this.labelCancel,
            acceptButton: this.labelDelete,
            width: '300px',
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
            this.snackBar.open(this.labelDeleted, this.labelOk, { duration: 3000 });
            this.deletingSla = false;
            this.state.go(FEED_DEFINITION_SUMMARY_STATE_NAME+".sla");
        }, () => {
            this.loadingService.resolve(SlaDetailsComponent.deleteLoader);
            this.deletingSla = false;
            this.snackBar.open(this.labelErrorDeletingSla, this.labelOk, { duration: 3000 });
        });
    }
}

