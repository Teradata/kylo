import * as angular from 'angular';
import {Input} from '@angular/core';

export class DataTransformPropertiesController implements ng.IComponentController {

    /**
     * Indicates if the feed NiFi properties may be edited.
     * @type {boolean}
     */
    allowEdit: boolean = false;

    /**
     * Indicates if version information is to be displayed
     * @type {boolean}
     */
    @Input()
    versions: boolean;

    /**
     * Indicates that the editable section is visible.
     * @type {boolean}
     */
    editableSection: boolean = false;

    /**
     * Feed model.
     * @type {Object}
     */
    model: any = this.FeedService.editFeedModel;
    versionModel: any = this.FeedService.versionFeedModel;

    static readonly $inject = ["$scope", "$q", "AccessControlService", "FeedService", "StateService", "VisualQueryService"];

    constructor(private $scope: any, private $q: any, private AccessControlService: any, private FeedService: any, private StateService: any, private VisualQueryService: any) {
        // Watch for model changes
        this.$scope.$watch(() => {
                return FeedService.editFeedModel;
            },
            () => {
                //only update the model if it is not set yet
                if (this.model == null) {
                    this.model = angular.copy(FeedService.editFeedModel);
                }
            });

        if (this.versions) {
            this.$scope.$watch(() => {
                    return FeedService.versionFeedModel;
                },
                () => {
                   this.versionModel = angular.copy(FeedService.versionFeedModel);
                });
        }

        //Apply the entity access permissions
        this.$q.when(this.AccessControlService.hasPermission(this.AccessControlService.FEEDS_EDIT, this.model, this.AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS))
            .then((access: any) => {
                this.allowEdit = !this.versions && access;
            });
    }

    /**
     * Navigates to the Edit Feed page for the current feed.
     */
    navigateToEditFeedInStepper() {
        this.VisualQueryService.resetModel();
        this.StateService.FeedManager().Feed().navigateToEditFeedInStepper(this.model.feedId);
    }

    diff = function(path: any) {
        return this.FeedService.diffOperation(path);
    };

    diffCollection = function(path: any) {
        return this.FeedService.diffCollectionOperation(path);
    };

}

angular.module("kylo.plugin.template-table-option.data-transformation", [])
    .component("kyloDataTransformProperties", {
        bindings: {
            versions: "<"
        },
        controller: DataTransformPropertiesController,
        controllerAs: "vm",
        templateUrl: "js/plugin/template-table-option/data-transformation/data-transform-properties.html"
    });
