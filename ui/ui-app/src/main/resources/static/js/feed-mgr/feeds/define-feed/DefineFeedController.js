define(["require", "exports", "angular", "underscore", "../../../services/AccessControlService"], function (require, exports, angular, _, AccessControlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/define-feed/module-name');
    var DefineFeedController = /** @class */ (function () {
        function DefineFeedController($scope, $http, $mdDialog, $q, $transition$, accessControlService, FeedService, FeedSecurityGroups, RestUrlService, StateService, UiComponentsService) {
            this.$scope = $scope;
            this.$http = $http;
            this.$mdDialog = $mdDialog;
            this.$q = $q;
            this.$transition$ = $transition$;
            this.accessControlService = accessControlService;
            this.FeedService = FeedService;
            this.FeedSecurityGroups = FeedSecurityGroups;
            this.RestUrlService = RestUrlService;
            this.StateService = StateService;
            this.UiComponentsService = UiComponentsService;
            this.requestedTemplate = this.$transition$.params().templateName || '';
            this.requestedTemplateId = this.$transition$.params().templateId || '';
            /**
             * Indicates if feeds may be imported from an archive.
             * @type {boolean}
             */
            this.allowImport = false;
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
             * flag to indicate we need to display the 'more templates' link
             * @type {boolean}
             */
            this.displayMoreLink = false;
            /**
             * Flag to indicate we are cloning
             * This is set via a $transition$ param in the init() method
             * @type {boolean}
             */
            this.cloning = false;
            /**
             * After the stepper is initialized this will get called to setup access control
             * @param stepper
             */
            this.onStepperInitialized = (function (stepper) {
                var _this = this;
                var accessChecks = { entityAccess: this.accessControlService.checkEntityAccessControlled(), securityGroups: this.FeedSecurityGroups.isEnabled() };
                this.$q.all(accessChecks).then(function (response) {
                    var entityAccess = _this.accessControlService.isEntityAccessControlled();
                    var securityGroupsAccess = response.securityGroups;
                    //disable the access control step
                    if (!entityAccess && !securityGroupsAccess) {
                        //Access Control is second to last step 0 based array indexc
                        stepper.deactivateStep(_this.model.totalSteps - 2);
                    }
                    if (_this.cloning) {
                        _this.hideCloningDialog();
                    }
                });
            }).bind(this);
            /**
             * Navigate to the import feed screen
             */
            this.gotoImportFeed = (function () {
                this.StateService.FeedManager().Feed().navigatetoImportFeed();
            }).bind(this);
            var self = this;
            this.model = FeedService.createFeedModel;
            if (angular.isUndefined(this.model)) {
                FeedService.resetFeed();
            }
            /**
             * The total number of steps to deisplay and render for the feed stepper
             * @type {null}
             */
            this.model.totalSteps = null;
            /**
             * The stepper url.
             *
             * @type {string}
             */
            this.model.stepperTemplateUrl = 'js/feed-mgr/feeds/define-feed/define-feed-stepper.html';
            this.requestedTemplate = $transition$.params().templateName || '';
            this.requestedTemplateId = $transition$.params().templateId || '';
            this.allTemplates = [];
            this.firstTemplates = [];
            this.displayMoreLink = false;
            this.cloning = false;
            //initialize the controller
            this.init();
        }
        /**
         * hide the cloning dialog
         */
        DefineFeedController.prototype.hideCloningDialog = function () {
            this.$mdDialog.hide();
        };
        /**
         * Click the more link to show all the template cards
         */
        DefineFeedController.prototype.more = function () {
            this.layout = 'all';
        };
        ;
        /**
         * Select a template
         * @param template
         */
        DefineFeedController.prototype.selectTemplate = function (template) {
            var _this = this;
            this.model.templateId = template.id;
            this.model.templateName = template.templateName;
            //setup some initial data points for the template
            this.model.defineTable = template.defineTable;
            this.model.allowPreconditions = template.allowPreconditions;
            this.model.dataTransformationFeed = template.dataTransformation;
            // Determine table option
            if (template.templateTableOption) {
                this.model.templateTableOption = template.templateTableOption;
            }
            else if (template.defineTable) {
                this.model.templateTableOption = "DEFINE_TABLE";
            }
            else if (template.dataTransformation) {
                this.model.templateTableOption = "DATA_TRANSFORMATION";
            }
            else {
                this.model.templateTableOption = "NO_TABLE";
            }
            //set the total pre-steps for this feed to be 0. They will be taken from the templateTableOption
            this.model.totalPreSteps = 0;
            //When rendering the pre-step we place a temp tab/step in the front for the initial steps to transclude into and then remove it.
            //set this render flag to false initially
            this.model.renderTemporaryPreStep = false;
            // Load table option
            if (this.model.templateTableOption !== "NO_TABLE") {
                this.UiComponentsService.getTemplateTableOption(this.model.templateTableOption)
                    .then(function (tableOption) {
                    //if we have a pre-stepper configured set the properties
                    if (angular.isDefined(tableOption.preStepperTemplateUrl) && tableOption.preStepperTemplateUrl != null) {
                        _this.model.totalPreSteps = tableOption.totalPreSteps;
                        _this.model.renderTemporaryPreStep = true;
                    }
                    //signal the service that we should track rendering the table template
                    //We want to run our initializer when both the Pre Steps and the Feed Steps have completed.
                    //this flag will be picked up in the TableOptionsStepperDirective.js
                    _this.UiComponentsService.startStepperTemplateRender(tableOption);
                    //add the template steps + 5 (general, feedDetails, properties, access, schedule)
                    _this.model.totalSteps = tableOption.totalSteps + 5;
                }, function () {
                    _this.$mdDialog.show(_this.$mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Create Failed")
                        .textContent("The template table option could not be loaded.")
                        .ariaLabel("Failed to create feed")
                        .ok("Got it!"));
                    _this.StateService.FeedManager().Feed().navigateToFeeds();
                });
            }
            else {
                this.model.totalSteps = 5;
            }
        };
        ;
        /**
         * Cancel the stepper
         */
        DefineFeedController.prototype.cancelStepper = function () {
            this.FeedService.resetFeed();
            this.model.totalSteps = null;
        };
        ;
        /**
         * Return a list of the Registered Templates in the system
         * @returns {HttpPromise}
         */
        DefineFeedController.prototype.getRegisteredTemplates = function () {
            var _this = this;
            var successFn = function (response) {
                if (response.data) {
                    var data = _.chain(response.data).filter(function (template) {
                        return template.state === 'ENABLED';
                    }).sortBy('order')
                        .value();
                    if (data.length > 1) {
                        _this.displayMoreLink = true;
                    }
                    _this.allTemplates = data;
                    _this.firstTemplates = _.first(data, 3);
                    if (_this.cloning) {
                        _this.model = _this.FeedService.cloneFeed();
                        var registeredTemplate = _this.model.registeredTemplate;
                        var templateObj = {
                            id: registeredTemplate.id,
                            templateName: registeredTemplate.templateName,
                            defineTable: registeredTemplate.defineTable,
                            allowPreconditions: registeredTemplate.allowPreconditions,
                            dataTransformation: registeredTemplate.dataTransformation,
                            templateTableOption: registeredTemplate.templateTableOption
                        };
                        _this.selectTemplate(templateObj);
                    }
                }
            };
            var errorFn = function (err) {
            };
            var promise = this.$http.get(this.RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;
        };
        /**
 * initialize the controller
 */
        DefineFeedController.prototype.init = function () {
            var _this = this;
            var isCloning = this.$transition$.params().bcExclude_cloning;
            var cloneFeedName = this.$transition$.params().bcExclude_cloneFeedName;
            this.cloning = angular.isUndefined(isCloning) ? false : isCloning;
            var feedDescriptor = this.$transition$.params().feedDescriptor || '';
            this.model.feedDescriptor = feedDescriptor;
            this.getRegisteredTemplates().then(function (response) {
                if (angular.isDefined(_this.requestedTemplate) && _this.requestedTemplate != '') {
                    var match = _.find(_this.allTemplates, function (template) {
                        return template.templateName == _this.requestedTemplate || template.id == _this.requestedTemplateId;
                    });
                    if (angular.isDefined(match)) {
                        _this.FeedService.resetFeed();
                        if (angular.isDefined(feedDescriptor) && feedDescriptor != '') {
                            _this.model.feedDescriptor = feedDescriptor;
                        }
                        _this.selectTemplate(match);
                    }
                }
            });
            if (this.cloning) {
                this.showCloningDialog(cloneFeedName);
            }
            // Fetch the allowed actions
            this.accessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.allowImport = _this.accessControlService.hasAction(AccessControlService_1.default.FEEDS_IMPORT, actionSet.actions);
            });
        };
        /**
         * Show a dialog while the cloning is setting up the stepper with the data
         * @param cloneFeedName
         */
        DefineFeedController.prototype.showCloningDialog = function (cloneFeedName) {
            if (angular.isUndefined(cloneFeedName)) {
                cloneFeedName = "the feed";
            }
            this.$mdDialog.show({
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
                };
            }
        };
        ;
        return DefineFeedController;
    }());
    exports.DefineFeedController = DefineFeedController;
    angular.module(moduleName).controller('DefineFeedController', ["$scope", "$http", "$mdDialog", "$q", "$transition$", "AccessControlService", "FeedService", "FeedSecurityGroups", "RestUrlService", "StateService",
        "UiComponentsService", DefineFeedController]);
});
//# sourceMappingURL=DefineFeedController.js.map