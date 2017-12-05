define(['angular', 'feed-mgr/feeds/module-name'], function (angular, moduleName) {
    /**
     * Controller for the Edit Feed page.
     *
     * @constructor
     */
    var EditFeedController = function ($scope, $http, $q, $mdDialog, $transition$, FeedService, RestUrlService, StateService, VisualQueryService, AccessControlService, FeedSecurityGroups,
                                       StepperService, EntityAccessControlService, UiComponentsService) {
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
        self.selectedStepIndex = 2;

        /**
         * Fetches and displays the feed.
         */
        self.init = function () {
            var successFn = function (response) {
                // Set model
                self.model = response.data;
                self.model.loaded = true;
                FeedService.createFeedModel = self.model;

                // Determine table option
                if (self.model.registeredTemplate.templateTableOption === null) {
                    if (self.model.registeredTemplate.defineTable) {
                        self.model.registeredTemplate.templateTableOption = "DEFINE_TABLE";
                    } else if (self.model.registeredTemplate.dataTransformation) {
                        self.model.registeredTemplate.templateTableOption = "DATA_TRANSFORMATION";
                    } else {
                        self.model.registeredTemplate.templateTableOption = "NO_TABLE";
                    }
                }

                // Load table option
                self.model.templateTableOption = self.model.registeredTemplate.templateTableOption;

                if (self.model.templateTableOption !== "NO_TABLE") {
                    UiComponentsService.getTemplateTableOption(self.model.templateTableOption)
                        .then(function (tableOption) {

                            //if we have a pre-stepper configured set the properties
                            if(angular.isDefined(tableOption.preStepperTemplateUrl) && tableOption.preStepperTemplateUrl != null){
                                self.model.totalPreSteps = tableOption.totalPreSteps
                                self.model.renderTemporaryPreStep = true;
                            }
                            //signal the service that we should track rendering the table template
                            //We want to run our initializer when both the Pre Steps and the Feed Steps have completed.
                            //this flag will be picked up in the TableOptionsStepperDirective.js
                            UiComponentsService.startStepperTemplateRender(tableOption);

                            //add the template steps + 5 (general, feedDetails, properties, access, schedule)
                            self.model.totalSteps = tableOption.totalSteps +  5;
                        }, function () {
                            $mdDialog.show(
                                $mdDialog.alert()
                                    .clickOutsideToClose(true)
                                    .title("Create Failed")
                                    .textContent("The template table option could not be loaded.")
                                    .ariaLabel("Failed to create feed")
                                    .ok("Got it!")
                            );
                            StateService.FeedManager().Feed().navigateToFeeds();
                        });
                } else {
                    self.model.totalSteps = 5;
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
            if (self.model.loaded && self.model.totalSteps > 2 && StepperService.getStep("EditFeedStepper", self.model.totalSteps - 2) !== null) {
                var entityAccess = AccessControlService.checkEntityAccessControlled();
                var accessChecks = {
                    changeFeedPermissions: entityAccess && FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.CHANGE_FEED_PERMISSIONS, self.model),
                    securityGroups: FeedSecurityGroups.isEnabled()
                };
                $q.all(accessChecks).then(function (response) {
                    //disable the access control step
                    if (!response.changeFeedPermissions && !response.securityGroups) {
                        //Access Control is second to last step 0 based array index
                        StepperService.deactivateStep("EditFeedStepper", self.model.totalSteps - 2);
                    }
                });
            }
        };

        /**
         * Resets the editor state.
         */
        self.cancelStepper = function () {
            FeedService.resetFeed();
            self.stepperUrl = "";
            StateService.FeedManager().Feed().navigateToFeeds();
        };

        // Initialize this instance
        self.init();
    };

    angular.module(moduleName).controller("EditFeedController", ["$scope", "$http", "$q", "$mdDialog", "$transition$", "FeedService", "RestUrlService", "StateService", "VisualQueryService",
                                                                 "AccessControlService", "FeedSecurityGroups", "StepperService", "EntityAccessControlService", "UiComponentsService", EditFeedController]);
});
