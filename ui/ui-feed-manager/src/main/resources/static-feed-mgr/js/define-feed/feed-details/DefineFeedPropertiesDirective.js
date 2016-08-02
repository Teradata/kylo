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

        self.transformChip = function(chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return {name: chip}
        };
    };

    angular.module(MODULE_FEED_MGR).controller('DefineFeedPropertiesController', DefineFeedPropertiesDirective);
    angular.module(MODULE_FEED_MGR).directive('thinkbigDefineFeedProperties', directive);
})();
