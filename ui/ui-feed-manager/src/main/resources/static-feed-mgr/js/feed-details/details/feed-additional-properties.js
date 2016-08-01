/**
 * This Directive is wired in to the FeedStatusIndicatorDirective.
 * It uses the OverviewService to watch for changes and update after the Indicator updates
 */
(function() {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {},
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/details/feed-additional-properties.html',
            controller: "FeedAdditionalPropertiesController",
            link: function($scope, element, attrs, controller) {

            }

        };
    };

    var FeedAdditionalPropertiesController = function($scope, FeedService, FeedTagService) {

        var self = this;

        this.model = FeedService.editFeedModel;
        this.editModel = {};
        this.editableSection = false;

        this.feedTagService = FeedTagService;
        self.tagChips = {};
        self.tagChips.selectedItem = null;
        self.tagChips.searchText = null;
        this.isValid = true;

        /**
         * List of user-defined properties.
         *
         * @type {Array.<{key: string, value: string}>}
         */
        self.userPropertyList = FeedService.getUserPropertyList(this.model);

        /**
         * Adds a new user-defined property.
         */
        self.addProperty = function() {
            self.userPropertyList.push({key: "", value: "", $error: {}});
        };

        /**
         * Deletes the item at the specified index from the user-defined properties list.
         *
         * @param {number} index the index of the property to delete
         */
        self.deleteProperty = function(index) {
            self.userPropertyList.splice(index, 1);
            self.updateUserProperties();
        };

        this.transformChip = function(chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return {name: chip}
        };

        /**
         * Updates the model with the list of user-defined properties.
         */
        self.updateUserProperties = function() {
            var keys = {};
            var userProperties = {};

            angular.forEach(self.userPropertyList, function(property) {
                // Validate property
                if (angular.isUndefined(property.$error)) {
                    property.$error = {};
                }

                property.$error.duplicate = angular.isDefined(keys[property.key]);

                // Add to user properties object
                if (property.key.length > 0) {
                    keys[property.key] = true;
                    userProperties[property.key] = property.value;
                }
            });

            self.editModel.userProperties = userProperties;
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
            self.editModel.userProperties = _.pick(self.model.userProperties, function(value, key) {
                return !key.startsWith("jcr:");
            });

            // Refresh user property list
            self.userPropertyList = FeedService.getUserPropertyList(self.editModel);
        };

        this.onCancel = function() {
            // do nothing
        };

        this.onSave = function(ev) {
            //save changes to the model
            FeedService.showFeedSavingDialog(ev, "Saving Feed " + self.model.feedName, self.model.feedName);
            var copy = angular.copy(FeedService.editFeedModel);

            copy.tags = self.editModel.tags;
            copy.dataOwner = self.editModel.dataOwner;
            copy.userProperties = self.editModel.userProperties;

            FeedService.saveFeedModel(copy).then(function(response) {
                FeedService.hideFeedSavingDialog();
                self.editableSection = false;
                //save the changes back to the model
                self.model.tags = self.editModel.tags;
                self.model.dataOwner = self.editModel.dataOwner;
                self.model.userProperties = self.editModel.userProperties;
            }, function(response) {
                FeedService.hideFeedSavingDialog();
                FeedService.buildErrorData(self.model.feedName, response.data);
                FeedService.showFeedErrorsDialog();
                //make it editable
                self.editableSection = true;
            });
        }
    };

    angular.module(MODULE_FEED_MGR).controller('FeedAdditionalPropertiesController', FeedAdditionalPropertiesController);
    angular.module(MODULE_FEED_MGR).directive('thinkbigFeedAdditionalProperties', directive);
})();
