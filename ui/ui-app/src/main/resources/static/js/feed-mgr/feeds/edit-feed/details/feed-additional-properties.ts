import * as angular from 'angular';
import 'pascalprecht.translate';
const moduleName = require('feed-mgr/feeds/edit-feed/module-name');
var directive = function() {
    return {
        restrict: "EA",
        bindToController: {},
        controllerAs: 'vm',
        scope: {},
        templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-additional-properties.html',
        controller: "FeedAdditionalPropertiesController",
        link: function($scope:any, element:any, attrs:any, controller:any) {

        }

    };
};



export class FeedAdditionalPropertiesController {
// define(['angular','feed-mgr/feeds/edit-feed/module-name', 'pascalprecht.translate'], function (angular,moduleName) {

        /**
         * Indicates if the feed properties may be edited.
         * @type {boolean}
         */
        allowEdit:boolean = false;
        
        model:any = this.FeedService.editFeedModel;
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

            this.FeedService.saveFeedModel(copy).then((response:any) => {
                this.FeedService.hideFeedSavingDialog();
                this.editableSection = false;
                //save the changes back to the model
                this.model.tags = this.editModel.tags;
                this.model.dataOwner = this.editModel.dataOwner;
                this.model.userProperties = this.editModel.userProperties;
                this.model.securityGroups = this.editModel.securityGroups;
            }, (response:any) => {
                this.FeedService.hideFeedSavingDialog();
                this.FeedService.buildErrorData(this.model.feedName, response);
                this.FeedService.showFeedErrorsDialog();
                //make it editable
                this.editableSection = true;
            });
        };


    constructor (private $scope:any,private $q:any, private AccessControlService:any, private EntityAccessControlService:any,private FeedService:any, private FeedTagService:any, private FeedSecurityGroups:any, private $filter:any) {
        var self = this;

        this.tagChips.selectedItem = null;
        this.tagChips.searchText = null;


        this.securityGroupChips.selectedItem = null;
        this.securityGroupChips.searchText = null;
        


        FeedSecurityGroups.isEnabled().then((isValid:any) => {
                self.securityGroupsEnabled = isValid;
            }

        );

        

        $scope.$watch(() => {
            return FeedService.editFeedModel;
        }, (newVal:any) => {
            //only update the model if it is not set yet
            if (self.model == null) {
                self.model = FeedService.editFeedModel;
            }
        });

        //Apply the entity access permissions
        $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT,self.model,AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then((access:any) => {
            self.allowEdit = access && !self.model.view.properties.disabled;
        });
    }


}
angular.module(moduleName).controller('FeedAdditionalPropertiesController',["$scope","$q","AccessControlService","EntityAccessControlService","FeedService","FeedTagService","FeedSecurityGroups","$filter",FeedAdditionalPropertiesController]);
angular.module(moduleName).directive('thinkbigFeedAdditionalProperties', directive);
