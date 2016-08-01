(function() {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            controllerAs: 'vm',
            require: ['thinkbigDefineFeedProperties', '^thinkbigStepper'],
            scope: {},
            templateUrl: 'js/define-feed/feed-details/define-feed-properties.html',
            controller: "DefineFeedPropertiesController",
            link: function($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }

        };
    };

    var DefineFeedPropertiesDirective = function($scope, $http, $mdToast, RestUrlService, FeedTagService, FeedService) {
        var self = this;
        self.stepNumber = parseInt(this.stepIndex) + 1;
        self.model = FeedService.createFeedModel;
        self.feedTagService = FeedTagService;
        self.tagChips = {};
        self.tagChips.selectedItem = null;
        self.tagChips.searchText = null;
        self.isValid = true;

        /**
         * List of user-defined properties.
         *
         * @type {Array.<{key: string, value: string}>}
         */
        self.userPropertyList = FeedService.getUserPropertyList(self.model);

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

        self.transformChip = function(chip) {
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
            self.model.userProperties = userProperties;
        };
    };

    angular.module(MODULE_FEED_MGR).controller('DefineFeedPropertiesController', DefineFeedPropertiesDirective);
    angular.module(MODULE_FEED_MGR).directive('thinkbigDefineFeedProperties', directive);
})();
