define(['angular','feed-mgr/feeds/define-feed/module-name'], function (angular,moduleName) {

    var controller = function ($scope, $http,$transition$, $mdDialog, $q, AccessControlService, FeedService, FeedSecurityGroups,RestUrlService, StateService, UiComponentsService) {

        var self = this;

        /**
         * Indicates if feeds may be imported from an archive.
         * @type {boolean}
         */
        self.allowImport = false;

        this.layout = 'first';
        this.template = null;
        self.model = FeedService.createFeedModel;
        if(angular.isUndefined(self.model)){
            FeedService.resetFeed();
        }
        self.model.totalSteps = null;
        var requestedTemplate = $transition$.params().templateName || '';
        var requestedTemplateId = $transition$.params().templateId || '';
        var feedDescriptor = $transition$.params().feedDescriptor || '';
        self.model.feedDescriptor = feedDescriptor;

        self.allTemplates = [];
        self.firstTemplates = [];
        self.displayMoreLink = false;
        /**
         * Return a list of the Registered Templates in the system
         * @returns {HttpPromise}
         */
        function getRegisteredTemplates() {
            var successFn = function (response) {

                if (response.data) {

                    var data = _.chain(response.data).filter(function (template) {
                        return template.state === 'ENABLED'
                    }).sortBy('order')
                        .value();

                    if (data.length > 1) {
                        self.displayMoreLink = true;
                    }
                    self.allTemplates = data;
                    self.firstTemplates = _.first(data, 3);

                }

            };
            var errorFn = function (err) {

            };
            var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;
        }

        this.more = function () {
            this.layout = 'all';
        };

        this.gotoImportFeed = function () {
            StateService.FeedManager().Feed().navigatetoImportFeed();
        };
        getRegisteredTemplates().then(function(response) {
            if(angular.isDefined(requestedTemplate) && requestedTemplate != ''){
                var match = _.find(self.allTemplates,function(template) {
                   return template.templateName == requestedTemplate || template.id == requestedTemplateId;
                });
                if(angular.isDefined(match)) {
                    FeedService.resetFeed();
                    self.selectTemplate(match);
                }
            }
        });

        this.selectTemplate = function (template) {
            self.model.templateId = template.id;
            self.model.templateName = template.templateName;
            //setup some initial data points for the template
            self.model.defineTable = template.defineTable;
            self.model.allowPreconditions = template.allowPreconditions;
            self.model.dataTransformationFeed = template.dataTransformation;

            // Determine table option
            if (template.templateTableOption) {
                self.model.templateTableOption = template.templateTableOption;
            } else if (template.defineTable) {
                self.model.templateTableOption = "DEFINE_TABLE";
            } else if (template.dataTransformation) {
                self.model.templateTableOption = "DATA_TRANSFORMATION";
            } else {
                self.model.templateTableOption = "NO_TABLE";
            }

            // Load table option
            if (self.model.templateTableOption !== "NO_TABLE") {
                UiComponentsService.getTemplateTableOption(self.model.templateTableOption)
                    .then(function (tableOption) {
                        self.model.totalSteps = tableOption.totalSteps + 5;
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
        };

        self.cancelStepper = function () {
            FeedService.resetFeed();
            self.model.totalSteps = null;
        };


        self.onStepperInitialized = function(stepper) {
            var accessChecks = {entityAccess: AccessControlService.checkEntityAccessControlled(), securityGroups: FeedSecurityGroups.isEnabled()};
            $q.all(accessChecks).then(function (response) {
                var entityAccess = AccessControlService.isEntityAccessControlled();
                var securityGroupsAccess = response.securityGroups;
                //disable the access control step
                if(!entityAccess && !securityGroupsAccess) {
                    //Access Control is second to last step 0 based array indexc
                    stepper.deactivateStep(self.model.totalSteps -2);
                }
            });
        };

        // Fetch the allowed actions
        AccessControlService.getUserAllowedActions()
            .then(function (actionSet) {
                self.allowImport = AccessControlService.hasAction(AccessControlService.FEEDS_IMPORT, actionSet.actions);
            });
    };

    angular.module(moduleName).controller('DefineFeedController', ["$scope", "$http","$transition$", "$mdDialog", "$q", "AccessControlService", "FeedService", "FeedSecurityGroups", "RestUrlService", "StateService",
                                                                   "UiComponentsService", controller]);

});
