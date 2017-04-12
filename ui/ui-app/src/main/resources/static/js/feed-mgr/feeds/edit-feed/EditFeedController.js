define(['angular','feed-mgr/feeds/module-name'], function (angular,moduleName) {
    /**
     * Controller for the Edit Feed page.
     *
     * @constructor
     */
    var EditFeedController = function($scope, $http, $mdDialog, $transition$, FeedService, RestUrlService, StateService, VisualQueryService) {
        var self = this;

        /**
         * Feed ID
         *
         * @type {string}
         */
        self.feedId = $transition$.params().feedId;

        /**
         * Feed model
         *
         * @type {Object}
         */
        self.model = FeedService.createFeedModel;
        self.model.loaded = false;

        /**
         * Selected index for the stepper
         *
         * @type {number}
         */
        self.selectedStepIndex = 0;

        /**
         * Template for the stepper
         *
         * @type {string}
         */
        self.stepperUrl = "";

        /**
         * Total number of steps for the stepper
         *
         * @type {number}
         */
        self.totalSteps = 0;

        /**
         * Fetches and displays the feed.
         */
        self.init = function() {
            var successFn = function(response) {
                // Set model
                self.model = response.data;
                self.model.loaded = true;
                FeedService.createFeedModel = self.model;

                // Update stepper based on template
                if (self.model.registeredTemplate.defineTable) {
                    self.selectedStepIndex = 2;
                    self.stepperUrl = "js/feed-mgr/feeds/define-feed/define-feed-stepper.html";
                    self.totalSteps = 7;
                } else if (self.model.registeredTemplate.dataTransformation) {
                    VisualQueryService.resetModel();
                    self.selectedStepIndex = 2;
                    self.stepperUrl = "js/feed-mgr/feeds/define-feed/define-feed-data-transform-stepper.html";
                    self.totalSteps = 9;
                } else {
                    self.stepperUrl = "js/feed-mgr/feeds/define-feed/define-feed-no-table-stepper.html";
                    self.totalSteps = 5;
                }
            };
            var errorFn = function() {
                var alert = $mdDialog.alert()
                        .parent($("body"))
                        .clickOutsideToClose(true)
                        .title("Unable to load feed details")
                        .textContent("Unable to load feed details. Please ensure that Apache NiFi is up and running and then refresh this page.")
                        .ariaLabel("Unable to load feed details")
                        .ok("Got it!");
                $mdDialog.show(alert);
            };

            $http.get(RestUrlService.GET_FEEDS_URL + "/" + self.feedId).then(successFn, errorFn);
        };

        /**
         * Resets the editor state.
         */
        this.cancelStepper = function() {
            FeedService.resetFeed();
            self.stepperUrl = "";
            StateService.FeedManager().Feed().navigateToFeeds();
        };

        // Initialize this instance
        self.init();
    };

    angular.module(moduleName).controller("EditFeedController", ["$scope","$http","$mdDialog","$transition$","FeedService","RestUrlService","StateService","VisualQueryService",EditFeedController]);


});
