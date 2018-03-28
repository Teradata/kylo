import * as angular from 'angular';
import * as _ from 'underscore';
import 'pascalprecht.translate';
const moduleName = require('feed-mgr/feeds/edit-feed/module-name');
var directive = function() {
    return {
        restrict: "EA",
        bindToController: {},
        controllerAs: 'vm',
            scope: {
                versions: '=?'
            },
        templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-additional-properties.html',
        controller: "FeedAdditionalPropertiesController",
        link: function($scope:any, element:any, attrs:any, controller:any) {
                if ($scope.versions === undefined) {
                    $scope.versions = false;
                }
        }
    };
};



export class FeedAdditionalPropertiesController {
// define(['angular','feed-mgr/feeds/edit-feed/module-name', 'pascalprecht.translate'], function (angular,moduleName) {
        versions:any = this.$scope.versions;
        /**
         * Indicates if the feed properties may be edited.
         * @type {boolean}
         */
        allowEdit:boolean = !this.versions;
        
        model:any = this.FeedService.editFeedModel;
        versionFeedModel:any = this.FeedService.versionFeedModel;
        versionFeedModelDiff:any = this.FeedService.versionFeedModelDiff;
        editModel:any = {};
        editableSection:boolean = false;

        feedTagService:any = this.FeedTagService;
        tagChips:any = {};
        securityGroupChips:any = {};
        isValid:boolean = true;
        feedSecurityGroups:any = this.FeedSecurityGroups;
        securityGroupsEnabled:boolean = false
 
        transformChip = function(chip:any) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return {name: chip}
        };

        onEdit = function() {
            // Determine tags value
            var tags = angular.copy(this.FeedService.editFeedModel.tags);
            if (tags == undefined || tags == null) {
                tags = [];
            }

            // Copy model for editing
            this.editModel = {};
            this.editModel.dataOwner = this.model.dataOwner;
            this.editModel.tags = tags;
            this.editModel.userProperties = angular.copy(this.model.userProperties);

            this.editModel.securityGroups = angular.copy(this.FeedService.editFeedModel.securityGroups);
            if (this.editModel.securityGroups == undefined) {
                this.editModel.securityGroups = [];
            }
        };
        
        
        onCancel = function() {
            // do nothing
        };

        onSave = function(ev:any) {
            //save changes to the model
            this.FeedService.showFeedSavingDialog(ev, this.$filter('translate')('views.feed-additional-properties.Saving'), this.model.feedName);
            var copy = angular.copy(this.FeedService.editFeedModel);

            copy.tags = this.editModel.tags;
            copy.dataOwner = this.editModel.dataOwner;
            copy.userProperties = this.editModel.userProperties;
            copy.securityGroups = this.editModel.securityGroups;
            //Server may have updated value. Don't send via UI.
            copy.historyReindexingStatus = undefined;

            this.FeedService.saveFeedModel(copy).then((response:any) => {
                this.FeedService.hideFeedSavingDialog();
                this.editableSection = false;
                //save the changes back to the model
                this.model.tags = this.editModel.tags;
                this.model.dataOwner = this.editModel.dataOwner;
                this.model.userProperties = this.editModel.userProperties;
                this.model.securityGroups = this.editModel.securityGroups;
                //Get the updated value from the server.
                this.model.historyReindexingStatus = response.data.feedMetadata.historyReindexingStatus;
            }, (response:any) => {
                this.FeedService.hideFeedSavingDialog();
                this.FeedService.buildErrorData(this.model.feedName, response);
                this.FeedService.showFeedErrorsDialog();
                //make it editable
                this.editableSection = true;
            });
        };


    constructor (private $scope:any,private $q:any, private AccessControlService:any, private EntityAccessControlService:any,private FeedService:any, private FeedTagService:any, private FeedSecurityGroups:any, private $filter:any) {
       // var self = this;
        this.tagChips.selectedItem = null;
        this.tagChips.searchText = null;
        this.securityGroupChips.selectedItem = null;
        this.securityGroupChips.searchText = null;

        FeedSecurityGroups.isEnabled().then((isValid:any) => {
                this.securityGroupsEnabled = isValid;
            }
        );
        $scope.$watch(() => {
            return FeedService.editFeedModel;
        }, (newVal:any) => {
            //only update the model if it is not set yet
            if (this.model == null) {
                this.model = FeedService.editFeedModel;
            }
        });
        
        if (this.versions) {
            $scope.$watch(()=>{
                return this.FeedService.versionFeedModel;
            },(newVal:any)=>{
                this.versionFeedModel = this.FeedService.versionFeedModel;
            });
            $scope.$watch(()=>{
                return this.FeedService.versionFeedModelDiff;
            },(newVal:any)=>{
                this.versionFeedModelDiff = this.FeedService.versionFeedModelDiff;
            });
        }

        //Apply the entity access permissions
        $q.when(AccessControlService.hasPermission(EntityAccessControlService.FEEDS_EDIT,this.model,EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then((access:any) => {
            this.allowEdit = !this.versions && access && !this.model.view.properties.disabled;
        });
    }

    findVersionedUserProperty(property:any) {
        var versionedProperty = _.find(this.versionFeedModel.userProperties, function(p:any) {
            return p.systemName === property.systemName;
        });
        if (versionedProperty === undefined) {
            versionedProperty = {};
        }
        return versionedProperty;
    };

    diff(path:any) {
        return this.FeedService.diffOperation(path);
    };
    diffCollection(path:any) {
        return this.FeedService.diffCollectionOperation(path);
    };
}
angular.module(moduleName).controller('FeedAdditionalPropertiesController',["$scope","$q","AccessControlService","EntityAccessControlService","FeedService","FeedTagService","FeedSecurityGroups","$filter",FeedAdditionalPropertiesController]);
angular.module(moduleName).directive('thinkbigFeedAdditionalProperties', directive);
