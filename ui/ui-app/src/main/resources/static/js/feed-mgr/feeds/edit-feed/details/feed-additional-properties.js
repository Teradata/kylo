define(['angular','feed-mgr/feeds/edit-feed/module-name', 'pascalprecht.translate'], function (angular,moduleName) {

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
            link: function($scope, element, attrs, controller) {
                if ($scope.versions === undefined) {
                    $scope.versions = false;
                }
            }
        };
    };

    var FeedAdditionalPropertiesController = function($scope,$q, AccessControlService, EntityAccessControlService,FeedService, FeedTagService, FeedSecurityGroups, $filter) {

        var self = this;
        self.versions = $scope.versions;
        /**
         * Indicates if the feed properties may be edited.
         * @type {boolean}
         */
        self.allowEdit = !self.versions;

        this.model = FeedService.editFeedModel;
        this.versionFeedModel = FeedService.versionFeedModel;
        this.versionFeedModelDiff = FeedService.versionFeedModelDiff;
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

        if (self.versions) {
            $scope.$watch(function(){
                return FeedService.versionFeedModel;
            },function(newVal) {
                self.versionFeedModel = FeedService.versionFeedModel;
            });
            $scope.$watch(function(){
                return FeedService.versionFeedModelDiff;
            },function(newVal) {
                self.versionFeedModelDiff = FeedService.versionFeedModelDiff;
            });
        }

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
            FeedService.showFeedSavingDialog(ev, $filter('translate')('views.feed-additional-properties.Saving'), self.model.feedName);
            var copy = angular.copy(FeedService.editFeedModel);

            copy.tags = self.editModel.tags;
            copy.dataOwner = self.editModel.dataOwner;
            copy.userProperties = self.editModel.userProperties;
            copy.securityGroups = self.editModel.securityGroups;
            //Server may have updated value. Don't send via UI.
            copy.historyReindexingStatus = undefined;

            FeedService.saveFeedModel(copy).then(function(response) {
                FeedService.hideFeedSavingDialog();
                self.editableSection = false;
                //save the changes back to the model
                self.model.tags = self.editModel.tags;
                self.model.dataOwner = self.editModel.dataOwner;
                self.model.userProperties = self.editModel.userProperties;
                self.model.securityGroups = self.editModel.securityGroups;
                //Get the updated value from the server.
                self.model.historyReindexingStatus = response.data.feedMetadata.historyReindexingStatus;
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
            self.allowEdit = !self.versions && access && !self.model.view.properties.disabled;
        });

        this.findVersionedUserProperty = function(property) {
            var versionedProperty = _.find(self.versionFeedModel.userProperties, function(p) {
                return p.systemName === property.systemName;
            });
            if (versionedProperty === undefined) {
                versionedProperty = {};
            }
            return versionedProperty;
        };

        this.diff = function(path) {
            return FeedService.diffOperation(path);
        };
        this.diffCollection = function(path) {
            return FeedService.diffCollectionOperation(path);
        };
    };

    angular.module(moduleName).controller('FeedAdditionalPropertiesController',["$scope","$q","AccessControlService","EntityAccessControlService","FeedService","FeedTagService","FeedSecurityGroups","$filter",FeedAdditionalPropertiesController]);
    angular.module(moduleName).directive('thinkbigFeedAdditionalProperties', directive);
});
