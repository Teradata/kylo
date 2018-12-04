import {HttpClient} from "@angular/common/http";
import {Component, Inject, Injectable} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material/dialog";
import * as angular from 'angular';
import {FeedService} from '../../services/FeedService';
import {RestUrlService} from "../../services/RestUrlService";
import {EntityAccessControlService} from './EntityAccessControlService';

@Injectable()
export class EntityAccessControlDialogService {

    constructor(private dialog: MatDialog) {
    }

    showAccessControlDialog(entity: any, entityType: any, entityTitle: any, onSave: any, onCancel: any) {
        const config: MatDialogConfig = {data: {entity: entity, entityType: entityType, entityTitle: entityTitle, callbackEvents: {onSave: onSave, onCancel: onCancel}}};
        return this.dialog.open(EntityAccessControlDialogController, config).afterClosed();
    }
}

@Component({
    templateUrl: './entity-access-control-dialog.html'
})
export class EntityAccessControlDialogController {

    entity: any;
    entityType: any;
    entityTitle: any;
    callbackEvents: any;

    allowIndexing: any;
    theForm = new FormGroup({});

    constructor(private dialog: MatDialogRef<EntityAccessControlDialogController>,
                @Inject(MAT_DIALOG_DATA) private data: any,
                private http: HttpClient,
                private restUrlService: RestUrlService,
                private entityAccessControlService: EntityAccessControlService,
                private feedService: FeedService) {
        this.entity = data.entity;
        this.entityType = data.entityType;
        this.entityTitle = data.entityTitle;
        this.callbackEvents = data.callbackEvents;

        this.allowIndexing = data.entity.allowIndexing;
    }

    /**
     * convert the model to a RoleMembershipChange object
     * @returns {Array}
     */
    toRoleMembershipChange() {
        return this.entityAccessControlService.toRoleMembershipChange(this.entity.roleMemberships);
    }

    /**
     * Save the Role Changes for this entity
     * @param $event
     */
    onSave($event: any) {
        // var roleMembershipChanges =
        //     EntityAccessControlService.saveRoleMemberships($scope.entityType, $scope.entity.id, $scope.entity.roleMemberships, function (r:any) {
        //         if (angular.isFunction(callbackEvents.onSave)) {
        //             callbackEvents.onSave(r);
        //         }
        // $mdDialog.hide();
        if (this.allowIndexing != this.entity.allowIndexing) {
            var indexInfoForDisplay = "";
            if (!this.allowIndexing) {
                indexInfoForDisplay = "Disabling indexing of metadata and schema...";
            } else {
                indexInfoForDisplay = "Enabling indexing of metadata and schema...";
            }
            this.feedService.showFeedSavingDialog($event, indexInfoForDisplay, this.entity.feedName);
            var copy = angular.copy(this.feedService.editFeedModel);
            copy.allowIndexing = this.allowIndexing;
            this.feedService.saveFeedModel(copy).then((response: any) => {
                this.feedService.hideFeedSavingDialog();
                this.entity.allowIndexing = copy.allowIndexing;
                var roleMembershipChanges =
                    this.entityAccessControlService.saveRoleMemberships(this.entityType, this.entity.id, this.entity.roleMemberships, (r: any) => {
                        if (typeof this.callbackEvents.onSave === "function") {
                            this.callbackEvents.onSave(r);
                        }
                        this.dialog.close(true);
                    });
            }, (response: any) => {
                console.log("Error saving feed: " + this.entity.feedName);
            });
        } else {
            var roleMembershipChanges =
                this.entityAccessControlService.saveRoleMemberships(this.entityType, this.entity.id, this.entity.roleMemberships, (r: any) => {
                    if (angular.isFunction(this.callbackEvents.onSave)) {
                        this.callbackEvents.onSave(r);
                    }
                    this.dialog.close(true);
                });
        }
        // });
    };

    onCancel($event: any) {
        this.dialog.close();
        if (angular.isFunction(this.callbackEvents.onCancel)) {
            this.callbackEvents.onCancel();
        }
    }

}


