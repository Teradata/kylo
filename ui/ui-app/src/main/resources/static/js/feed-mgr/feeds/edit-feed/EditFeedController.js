define(['angular', 'feed-mgr/feeds/module-name'], function (angular, moduleName) {
    /**
     * Controller for the Edit Feed page.
     *
     * @constructor
     */
    var EditFeedController = function ($scope, $http, $q, $mdDialog, $transition$, FeedService, RestUrlService, StateService, VisualQueryService, AccessControlService, FeedSecurityGroups,
                                       StepperService, EntityAccessControlService) {
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
        self.init = function () {
            var successFn = function (response) {
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
                    self.model.dataTransformationFeed = true;
                    VisualQueryService.resetModel();

                    self.selectedStepIndex = 2;
                    self.stepperUrl = "js/feed-mgr/feeds/define-feed/define-feed-data-transform-stepper.html";
                    self.totalSteps = 9;
                } else {
                    self.stepperUrl = "js/feed-mgr/feeds/define-feed/define-feed-no-table-stepper.html";
                    self.totalSteps = 5;
                }

                self.onStepperInitialized();
            };
            var errorFn = function () {
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
         * initialize the stepper and setup step display
         */
        self.onStepperInitialized = function () {
            if (self.model.loaded && self.totalSteps > 2 && StepperService.getStep("EditFeedStepper", self.totalSteps - 2) !== null) {
                var entityAccess = AccessControlService.checkEntityAccessControlled();
                var accessChecks = {
                    changeFeedPermissions: entityAccess && FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.CHANGE_FEED_PERMISSIONS, self.model),
                    securityGroups: FeedSecurityGroups.isEnabled()
                };
                $q.all(accessChecks).then(function (response) {
                    //disable the access control step
                    if (!response.changeFeedPermissions && !response.securityGroups) {
                        //Access Control is second to last step 0 based array index
                        StepperService.deactivateStep("EditFeedStepper", self.totalSteps - 2);
                    }
                });
            }
        };

        /**
         * Resets the editor state.
         */
        this.cancelStepper = function () {
            FeedService.resetFeed();
            self.stepperUrl = "";
            StateService.FeedManager().Feed().navigateToFeeds();
        };

        // Initialize this instance
        self.init();
    };

    angular.module(moduleName).controller("EditFeedController", ["$scope", "$http", "$q", "$mdDialog", "$transition$", "FeedService", "RestUrlService", "StateService", "VisualQueryService",
                                                                 "AccessControlService", "FeedSecurityGroups", "StepperService", "EntityAccessControlService", EditFeedController]);
});
