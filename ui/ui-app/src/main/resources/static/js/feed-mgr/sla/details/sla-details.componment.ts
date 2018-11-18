import {Component, Injector, Input, OnInit, ViewContainerRef} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {FEED_DEFINITION_SECTION_STATE_NAME, FEED_DEFINITION_STATE_NAME, FEED_DEFINITION_SUMMARY_STATE_NAME} from '../../model/feed/feed-constants';
import * as _ from 'underscore';
import {Sla} from '../model/sla.model';
import {Feed, FeedState} from '../../model/feed/feed.model';
import {FormGroup} from '@angular/forms';
import {LoadingMode, LoadingType, TdLoadingService} from '@covalent/core/loading';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TdDialogService} from '@covalent/core/dialogs';
import {TranslateService} from '@ngx-translate/core';
import AccessConstants from '../../../constants/AccessConstants';
import {SlaService} from "../../services/sla.service";

import "rxjs/add/observable/empty";
import 'rxjs/add/observable/forkJoin'
import "rxjs/add/observable/of";
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import "rxjs/add/observable/from";
import 'rxjs/add/observable/fromPromise';
import {Observable} from "rxjs/Observable";
import {PartialObserver} from "rxjs/Observer";
import {catchError} from "rxjs/operators/catchError";
import {concatMap} from "rxjs/operators/concatMap";
import {filter} from "rxjs/operators/filter";
import {finalize} from "rxjs/operators/finalize";
import {map} from "rxjs/operators/map";
import {switchMap} from "rxjs/operators/switchMap";
import {tap} from "rxjs/operators/tap";
import {Subject} from "rxjs/Subject";
import {error} from "ng-packagr/lib/util/log";

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
    styleUrls: ["./sla-details.component.scss"],
    templateUrl: "./sla-details.component.html"
})
export class SlaDetailsComponent implements OnInit {

    private static feedLoader: string = "SlaDetailsComponent.feedLoader";
    private static slaLoader: string = "SlaDetailsComponent.slaLoader";
    private static saveLoader: string = "SlaDetailsComponent.saveLoader";
    private static deleteLoader: string = "SlaDetailsComponent.deleteLoader";

    @Input() stateParams:any;

    @Input("feed")
    feedModel: Feed;

    private slaId: string;
    private accessControlService: any;
    private policyInputFormService: any;
    allowEdit = false;
    sla: Sla;
    /**
     * a copy of the loaded SLA name (separate from the modifiable sla object (sla.name) for display purposes
     */
    slaName:string;

    slaForm: FormGroup = new FormGroup({});
    private feedId: string;
    private savingSla = false;
    loadingFeed = false;
    loadingSla = false;
    private deletingSla = false;
    mode: FormMode;
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

    constructor(private $$angularInjector: Injector, private state: StateService,
                private loadingService: TdLoadingService, private snackBar: MatSnackBar, private dialogService: TdDialogService,
                private viewContainerRef: ViewContainerRef, private translateService: TranslateService,private slaService:SlaService)
{

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
            this.sla.editable = true;
            this.applyEditPermissionsToSLA(this.sla);

        }
        this.feedId = this.stateParams ? this.stateParams.feedId : undefined;
    }

    loadSla(slaId: string) {
        this.loadingService.register(SlaDetailsComponent.slaLoader);
        this.loadingSla = true;

        Observable.fromPromise<Sla>( <Promise<Sla>>this.slaService.getSlaForEditForm(slaId))
            .pipe(
                catchError(err => {
                    this.loadingService.resolve(SlaDetailsComponent.slaLoader);
                    const msg = err.data.message || this.labelErrorLoadingSla;
                    this.loadingSla = false;
                    console.error(msg);
                    this.snackBar.open(this.labelAccessDenied, this.labelOk, { duration: 5000 });
                    throw err;
                } ),
                switchMap((sla:Sla) =>  this.applyEditPermissionsToSLA(sla)),
                finalize(() =>{
                this.loadingService.resolve(SlaDetailsComponent.slaLoader);
                this.loadingSla = false;
                })
            ).subscribe(sla => {
                this.sla = sla;
                this.slaName = sla.name;
                _.each(sla.rules, (rule: any) => {
                    rule.editable = sla.canEdit;
                    rule.mode = FormMode.ModeEdit;
                    rule.groups = this.policyInputFormService.groupProperties(rule);
                    this.policyInputFormService.updatePropertyIndex(rule);
                });

                _.each(sla.actionConfigurations, (rule: any) => {
                    rule.editable = sla.canEdit;
                    rule.mode = FormMode.ModeEdit;
                    rule.groups = this.policyInputFormService.groupProperties(rule);
                    this.policyInputFormService.updatePropertyIndex(rule);
                    //validate the rules
                    this.slaService.validateSlaActionRule(rule);

            });
    });
    }

    private applyEditPermissionsToSLA(sla: Sla) :Observable<Sla> {
        const entityAccessControlled = this.accessControlService.isEntityAccessControlled();
       let promise = this.accessControlService.getUserAllowedActions();
       let subject = new Subject<Sla>();
       promise.then((response: any) => {
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
            subject.next(sla);
            subject.complete();
        },
           (error:any) => {
           subject.next(sla)
               subject.complete();
           });
       return subject.asObservable();
    }


    onSaveSla():void {
        this.savingSla = true;
        this.loadingService.register(SlaDetailsComponent.saveLoader);
        if(this.feedModel) {
            this.slaService.saveFeedSla(this.feedId, this.sla).then((response: any) => {
                this.loadingService.resolve(SlaDetailsComponent.saveLoader);
                this.savingSla = false;
                this.snackBar.open(this.labelSavedSla, this.labelOk, {duration: 3000});
                this.state.go("^.list");
            }, function () {
                this.loadingService.resolve(SlaDetailsComponent.saveLoader);
                this.savingSla = false;
                this.snackBar.open(this.labelFailedToSaveSla, this.labelOk, {duration: 3000});
            });
        }
        else {
            this.slaService.saveSla(this.sla).then((response: any) => {
                this.loadingService.resolve(SlaDetailsComponent.saveLoader);
                this.savingSla = false;
                this.snackBar.open(this.labelSavedSla, this.labelOk, {duration: 3000});
                this.state.go("^.list");
            }, function () {
                this.loadingService.resolve(SlaDetailsComponent.saveLoader);
                this.savingSla = false;
                this.snackBar.open(this.labelFailedToSaveSla, this.labelOk, {duration: 3000});
            });
        }
    }

    onCancelSaveSla(): void {
        this.state.go("^.list");
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
            this.state.go("^.list");
        }, () => {
            this.loadingService.resolve(SlaDetailsComponent.deleteLoader);
            this.deletingSla = false;
            this.snackBar.open(this.labelErrorDeletingSla, this.labelOk, { duration: 3000 });
        });
    }
}

