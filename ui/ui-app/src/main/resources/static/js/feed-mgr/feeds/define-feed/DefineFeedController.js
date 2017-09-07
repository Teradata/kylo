define(['angular', 'feed-mgr/feeds/define-feed/module-name'], function (angular, moduleName) {

    var controller = function ($scope, $http, $mdDialog, $q, $transition$, AccessControlService, FeedService, FeedSecurityGroups, RestUrlService, StateService, UiComponentsService) {

        var self = this;

        /**
         * Indicates if feeds may be imported from an archive.
         * @type {boolean}
         */
        self.allowImport = false;

        /**
         * the layout choosen.  Either 'first', or 'all'  changed via the 'more' link
         * @type {string}
         */
        this.layout = 'first';

        /**
         * The selected template
         * @type {null}
         */
        this.template = null;

        /**
         * The model for creating the feed
         * @type {*}
         */
        self.model = FeedService.createFeedModel;

        /**
         * the number of steps for the selected feed/template
         * @type {null}
         */
        self.model.totalSteps = null;

        /**
         * All the templates available
         * @type {Array}
         */
        self.allTemplates = [];

        /**
         * Array of the first n templates to be displayed prior to the 'more' link
         * @type {Array}
         */
        self.firstTemplates = [];

        /**
         * flag to indicate we need to display the 'more templates' link
         * @type {boolean}
         */
        self.displayMoreLink = false;
        /**
         * Flag to indicate we are cloning
         * This is set via a $transition$ param in the init() method
         * @type {boolean}
         */
        self.cloning = false;

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
                    if (self.cloning) {
                        self.model = FeedService.cloneFeed();
                        var registeredTemplate = self.model.registeredTemplate;
                        var templateObj = {
                            id: registeredTemplate.id,
                            templateName: registeredTemplate.templateName,
                            defineTable: registeredTemplate.defineTable,
                            allowPreconditions: registeredTemplate.allowPreconditions,
                            dataTransformation: registeredTemplate.dataTransformation,
                            templateTableOption: registeredTemplate.templateTableOption
                        }
                        self.selectTemplate(templateObj);
                    }

                }

            };
            var errorFn = function (err) {

            };
            var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;
        }

        /**
         * Click the more link to show all the template cards
         */
        this.more = function () {
            this.layout = 'all';
        };

        /**
         * Navigate to the import feed screen
         */
        this.gotoImportFeed = function () {
            StateService.FeedManager().Feed().navigatetoImportFeed();
        };

        /**
         * Select a template
         * @param template
         */
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

        /**
         * Cancel the stepper
         */
        self.cancelStepper = function () {
            FeedService.resetFeed();
            self.model.totalSteps = null;
        };

        /**
         * After the stepper is initialized this will get called to setup access control
         * @param stepper
         */
        self.onStepperInitialized = function (stepper) {
            var accessChecks = {entityAccess: AccessControlService.checkEntityAccessControlled(), securityGroups: FeedSecurityGroups.isEnabled()};
            $q.all(accessChecks).then(function (response) {
                var entityAccess = AccessControlService.isEntityAccessControlled();
                var securityGroupsAccess = response.securityGroups;
                //disable the access control step
                if (!entityAccess && !securityGroupsAccess) {
                    //Access Control is second to last step 0 based array indexc
                    stepper.deactivateStep(self.model.totalSteps - 2);
                }
                if (self.cloning) {
                    hideCloningDialog();
                }
            });

        };

        /**
         * initialize the controller
         */
        function init() {

            var isCloning = $transition$.params().bcExclude_cloning;
            var cloneFeedName = $transition$.params().bcExclude_cloneFeedName;
            self.cloning = angular.isUndefined(isCloning) ? false : isCloning;

            getRegisteredTemplates();
            if (self.cloning) {
                showCloningDialog(cloneFeedName);
            }
            // Fetch the allowed actions
            AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                    self.allowImport = AccessControlService.hasAction(AccessControlService.FEEDS_IMPORT, actionSet.actions);
                });

        }

        /**
         * hide the cloning dialog
         */
        function hideCloningDialog() {
            $mdDialog.hide();
        }

        /**
         * Show a dialog while the cloning is setting up the stepper with the data
         * @param cloneFeedName
         */
        function showCloningDialog(cloneFeedName) {
            if (angular.isUndefined(cloneFeedName)) {
                cloneFeedName = "the feed";
            }
            $mdDialog.show({
                templateUrl: 'js/feed-mgr/feeds/define-feed/clone-feed-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: true,
                locals: {
                    feedName: cloneFeedName
                },
                controller: CloningDialogController,
                fullscreen: true
            });
            function CloningDialogController($scope, $mdDialog, feedName) {
                $scope.feedName = feedName;
                $scope.closeDialog = function () {
                    $mdDialog.hide();
                }
            }
        }

        //initialize the controller
        init();

    };

    angular.module(moduleName).controller('DefineFeedController',
        ["$scope", "$http", "$mdDialog", "$q", "$transition$", "AccessControlService", "FeedService", "FeedSecurityGroups", "RestUrlService", "StateService",
         "UiComponentsService", controller]);

});
