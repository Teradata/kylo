define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {},
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-additional-properties.html',
            controller: "FeedAdditionalPropertiesController",
            link: function($scope, element, attrs, controller) {

            }

        };
    };

    var FeedAdditionalPropertiesController = function($scope,$q, AccessControlService, EntityAccessControlService,FeedService, FeedTagService, FeedSecurityGroups) {

        var self = this;

        /**
         * Indicates if the feed properties may be edited.
         * @type {boolean}
         */
        self.allowEdit = false;

        this.model = FeedService.editFeedModel;
        this.editModel = {};
        this.editableSection = false;

        this.feedTagService = FeedTagService;
        self.tagChips = {};
        self.tagChips.selectedItem = null;
        self.tagChips.searchText = null;
        this.isValid = true;

        this.feedSecurityGroups = FeedSecurityGroups;

        self.securityGroupChips = {};
        self.securityGroupChips.selectedItem = null;
        self.securityGroupChips.searchText = null;
        self.securityGroupsEnabled = false;

        FeedSecurityGroups.isEnabled().then(function(isValid) {
                self.securityGroupsEnabled = isValid;
            }

        );

        this.transformChip = function(chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return {name: chip}
        };

        $scope.$watch(function() {
            return FeedService.editFeedModel;
        }, function(newVal) {
            //only update the model if it is not set yet
            if (self.model == null) {
                self.model = FeedService.editFeedModel;
            }
        });

        this.onEdit = function() {
            // Determine tags value
            var tags = angular.copy(FeedService.editFeedModel.tags);
            if (tags == undefined || tags == null) {
                tags = [];
            }

            // Copy model for editing
            self.editModel = {};
            self.editModel.dataOwner = self.model.dataOwner;
            self.editModel.tags = tags;
            self.editModel.userProperties = angular.copy(self.model.userProperties);

            self.editModel.securityGroups = angular.copy(FeedService.editFeedModel.securityGroups);
            if (self.editModel.securityGroups == undefined) {
                self.editModel.securityGroups = [];
            }
        };

        this.onCancel = function() {
            // do nothing
        };

        this.onSave = function(ev) {
            //save changes to the model
            FeedService.showFeedSavingDialog(ev, "Saving...", self.model.feedName);
            var copy = angular.copy(FeedService.editFeedModel);

            copy.tags = self.editModel.tags;
            copy.dataOwner = self.editModel.dataOwner;
            copy.userProperties = self.editModel.userProperties;
            copy.securityGroups = self.editModel.securityGroups;

            FeedService.saveFeedModel(copy).then(function(response) {
                FeedService.hideFeedSavingDialog();
                self.editableSection = false;
                //save the changes back to the model
                self.model.tags = self.editModel.tags;
                self.model.dataOwner = self.editModel.dataOwner;
                self.model.userProperties = self.editModel.userProperties;
                self.model.securityGroups = self.editModel.securityGroups;
            }, function(response) {
                FeedService.hideFeedSavingDialog();
                FeedService.buildErrorData(self.model.feedName, response);
                FeedService.showFeedErrorsDialog();
                //make it editable
                self.editableSection = true;
            });
        };


        //Apply the entity access permissions
        $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT,self.model,AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function(access) {
            self.allowEdit = access && !self.model.view.properties.disabled;
        });

    };

    angular.module(moduleName).controller('FeedAdditionalPropertiesController',["$scope","$q","AccessControlService","EntityAccessControlService","FeedService","FeedTagService","FeedSecurityGroups",FeedAdditionalPropertiesController]);
    angular.module(moduleName).directive('thinkbigFeedAdditionalProperties', directive);
});
