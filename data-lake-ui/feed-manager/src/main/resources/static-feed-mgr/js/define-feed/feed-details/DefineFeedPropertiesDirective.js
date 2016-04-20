
(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            controllerAs: 'vm',
            require:['thinkbigDefineFeedProperties','^thinkbigStepper'],
            scope: {},
            templateUrl: 'js/define-feed/feed-details/define-feed-properties.html',
            controller: "DefineFeedPropertiesController",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }

        };
    }

    var controller =  function($scope, $http,$mdToast,RestUrlService, FeedTagService,FeedService) {

        this.stepNumber = parseInt(this.stepIndex)+1
        var self = this;
        this.model = FeedService.createFeedModel;
        this.feedTagService = FeedTagService;
        self.tagChips = {};
        self.tagChips.selectedItem = null;
        self.tagChips.searchText = null;
        this.isValid = true;

        this.transformChip = function(chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return { name: chip }
        }





    };


    angular.module(MODULE_FEED_MGR).controller('DefineFeedPropertiesController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigDefineFeedProperties', directive);

})();
