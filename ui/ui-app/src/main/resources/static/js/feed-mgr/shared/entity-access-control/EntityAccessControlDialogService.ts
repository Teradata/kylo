import * as angular from 'angular';
import * as _ from "underscore";
import { EntityAccessControlService } from './EntityAccessControlService';
import { FeedService } from '../../services/FeedService';
import {moduleName} from "../../module-name";

export class EntityAccessControlDialogService {
    static readonly $inject = ["$mdDialog"];
    constructor(private $mdDialog : any) {

    }
    showAccessControlDialog = (entity: any, entityType: any, entityTitle: any, onSave: any, onCancel: any) => {

        var callbackEvents = { onSave: onSave, onCancel: onCancel };
        return this.$mdDialog.show({
            controller: 'EntityAccessControlDialogController',
            templateUrl: './entity-access-control-dialog.html',
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            fullscreen: true,
            locals: { entity: entity, entityType: entityType, entityTitle: entityTitle, callbackEvents: callbackEvents }
        }).then( (msg: any) => {
            //respond to action in dialog if necessary... currently dont need to do anything
        }, () => {

        });
    }
}

angular.module(moduleName).service('EntityAccessControlDialogService', EntityAccessControlDialogService);

class controller {

    static readonly $inject = ["$scope", '$mdDialog', "$q", "$http", "RestUrlService", 
    "EntityAccessControlService", "entity", "entityType", "entityTitle",
    "callbackEvents", "FeedService"];

    constructor(private $scope: IScope,private $mdDialog: angular.material.IDialogService,private $q: angular.IQService,private $http: angular.IHttpService,private RestUrlService: any
        ,private entityAccessControlService: EntityAccessControlService,private entity: any,private entityType: any,private entityTitle: any
        ,private callbackEvents: any,private feedService: FeedService) {

        $scope.entityTitle = entityTitle;

        $scope.entityType = entityType;

        /**
         * The Angular form for validation
         * @type {{}}
         */
        $scope.theForm = {}

        /**
         * The entity to provide Access Control
         */
        $scope.entity = entity;
        /**
        * Indexing control
        */
        $scope.allowIndexing = $scope.entity.allowIndexing;

        /**
         * convert the model to a RoleMembershipChange object
         * @returns {Array}
         */
        function toRoleMembershipChange() {
            return this.entityAccessControlService.toRoleMembershipChange($scope.entity.roleMemberships);
        }

        /**
         * Save the Role Changes for this entity
         * @param $event
         */
        $scope.onSave = ($event: any) => {
            // var roleMembershipChanges =
            //     EntityAccessControlService.saveRoleMemberships($scope.entityType, $scope.entity.id, $scope.entity.roleMemberships, function (r:any) {
            //         if (angular.isFunction(callbackEvents.onSave)) {
            //             callbackEvents.onSave(r);
            //         }
            // $mdDialog.hide();
            if ($scope.allowIndexing != $scope.entity.allowIndexing) {
                var indexInfoForDisplay = "";
                if (!$scope.allowIndexing) {
                    indexInfoForDisplay = "Disabling indexing of metadata and schema...";
                } else {
                    indexInfoForDisplay = "Enabling indexing of metadata and schema...";
                }
                this.feedService.showFeedSavingDialog($event, indexInfoForDisplay, $scope.entity.feedName);
                var copy = angular.copy(this.feedService.editFeedModel);
                copy.allowIndexing = $scope.allowIndexing;
                this.feedService.saveFeedModel(copy).then((response: any) => {
                    this.feedService.hideFeedSavingDialog();
                    $scope.entity.allowIndexing = copy.allowIndexing;
                    var roleMembershipChanges =
                        this.entityAccessControlService.saveRoleMemberships($scope.entityType, $scope.entity.id, $scope.entity.roleMemberships, function (r: any) {
                            if (angular.isFunction(callbackEvents.onSave)) {
                                callbackEvents.onSave(r);
                            }
                            $mdDialog.hide();
                        });
                }, (response: any) => {
                    console.log("Error saving feed: " + $scope.entity.feedName);
                });
            } else {
                var roleMembershipChanges =
                    this.entityAccessControlService.saveRoleMemberships($scope.entityType, $scope.entity.id, $scope.entity.roleMemberships, function (r: any) {
                        if (angular.isFunction(callbackEvents.onSave)) {
                            callbackEvents.onSave(r);
                        }
                        $mdDialog.hide();
                    });
            }
            // });
        };

        $scope.onCancel = ($event: any) => {
            $mdDialog.hide();
            if (angular.isFunction(callbackEvents.onCancel)) {
                callbackEvents.onCancel();
            }
        }

    }
};

angular.module(moduleName).controller('EntityAccessControlDialogController',controller);

