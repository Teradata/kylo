define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/define-feed/module-name');
    var DefineFeedDetailsController = /** @class */ (function () {
        function DefineFeedDetailsController($scope, $http, RestUrlService, FeedService, RegisterTemplateService, FeedInputProcessorOptionsFactory, BroadcastService, StepperService, FeedDetailsProcessorRenderingHelper) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.RestUrlService = RestUrlService;
            this.FeedService = FeedService;
            this.RegisterTemplateService = RegisterTemplateService;
            this.FeedInputProcessorOptionsFactory = FeedInputProcessorOptionsFactory;
            this.BroadcastService = BroadcastService;
            this.StepperService = StepperService;
            this.FeedDetailsProcessorRenderingHelper = FeedDetailsProcessorRenderingHelper;
            /**
             * The Angular Form for validation
             * @type {{}}
             */
            this.feedDetailsForm = {};
            this.inputProcessorId = null;
            /**
             * counter holding the number of times the user changes to a different input
             * @type {number}
             */
            this.inputChangedCounter = 0;
            this.isValid = false;
            /**
             * flag to indicate if the data is still loading
             * @type {boolean}
             */
            this.loading = false;
            this.stepperController = null;
            this.inputProcessor = [];
            this.model = FeedService.createFeedModel;
            var watchers = [];
            this.codemirrorRenderTypes = RegisterTemplateService.codemirrorRenderTypes;
            var inputDatabaseType = ["com.thinkbiganalytics.nifi.GetTableData"];
            BroadcastService.subscribe($scope, StepperService.ACTIVE_STEP_EVENT, function (event, index) {
                if (index == parseInt(_this.stepIndex)) {
                    _this.validate();
                }
            });
            var inputProcessorIdWatch = $scope.$watch(function () {
                return _this.inputProcessorId;
            }, function (newVal, oldVal) {
                if (newVal != null && _this.initialInputProcessorId == null) {
                    _this.initialInputProcessorId = newVal;
                }
                _this.updateInputProcessor(newVal);
                //mark the next step as not visited.. force the user to go to the next step
                _this.stepperController.resetStep(parseInt(_this.stepIndex) + 1);
                _this.validate();
            });
            var systemFeedNameWatch = $scope.$watch(function () {
                return _this.model.systemFeedName;
            }, function (newVal) {
                _this.validate();
            });
            var templateIdWatch = $scope.$watch(function () {
                return _this.model.templateId;
            }, function (newVal) {
                _this.loading = true;
                _this.getRegisteredTemplate();
            });
            $scope.$on('$destroy', function () {
                systemFeedNameWatch();
                templateIdWatch();
                inputProcessorIdWatch();
            });
        }
        DefineFeedDetailsController.prototype.$onInit = function () {
            this.totalSteps = this.stepperController.totalSteps;
            this.stepNumber = parseInt(this.stepIndex) + 1;
        };
        DefineFeedDetailsController.prototype.matchInputProcessor = function (inputProcessor, inputProcessors) {
            if (inputProcessor == null) {
                //input processor is null when feed is being created
                return undefined;
            }
            var matchingInput = _.find(inputProcessors, function (input) {
                if (input.id == inputProcessor.id) {
                    return true;
                }
                return (input.type == inputProcessor.type && input.name == inputProcessor.name);
            });
            return matchingInput;
        };
        /**
             * Prepares the processor properties of the specified template for display.
             *
             * @param {Object} template the template with properties
             */
        DefineFeedDetailsController.prototype.initializeProperties = function (template) {
            if (angular.isDefined(this.model.cloned) && this.model.cloned == true) {
                this.RegisterTemplateService.setProcessorRenderTemplateUrl(this.model, 'create');
                var inputProcessors = this.model.inputProcessors;
                this.inputProcessors = _.sortBy(inputProcessors, 'name');
                // Find controller services
                _.chain(this.inputProcessors.concat(this.model.nonInputProcessors))
                    .pluck("properties")
                    .flatten(true)
                    .filter(function (property) {
                    return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
                })
                    .each(this.FeedService.findControllerServicesForProperty);
            }
            else {
                this.RegisterTemplateService.initializeProperties(template, 'create', this.model.properties);
                var inputProcessors = this.RegisterTemplateService.removeNonUserEditableProperties(template.inputProcessors, true);
                //sort them by name
                this.inputProcessors = _.sortBy(inputProcessors, 'name');
                this.model.allowPreconditions = template.allowPreconditions;
                this.model.nonInputProcessors = this.RegisterTemplateService.removeNonUserEditableProperties(template.nonInputProcessors, false);
            }
            if (angular.isDefined(this.model.inputProcessor)) {
                var match = this.matchInputProcessor(this.model.inputProcessor, this.inputProcessors);
                if (angular.isDefined(match)) {
                    this.inputProcessor = match;
                    this.inputProcessorId = match.id;
                }
            }
            if (this.inputProcessorId == null && this.inputProcessors != null && this.inputProcessors.length > 0) {
                this.inputProcessorId = this.inputProcessors[0].id;
            }
            // Skip this step if it's empty
            if (this.inputProcessors.length === 0 && !_.some(this.nonInputProcessors, function (processor) {
                return processor.userEditable;
            })) {
                var step = this.StepperService.getStep("DefineFeedStepper", parseInt(this.stepIndex));
                if (step != null) {
                    step.skip = true;
                }
            }
            // Find controller services
            _.chain(template.inputProcessors.concat(template.nonInputProcessors))
                .pluck("properties")
                .flatten(true)
                .filter(function (property) {
                return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
            })
                .each(this.FeedService.findControllerServicesForProperty);
            this.loading = false;
            this.validate();
        };
        /**
             * gets the Registered Template
             * Activates/Deactivates the Table and Data Processing Steps based upon if the template is registered to define the Table or not
             * @returns {HttpPromise}
             */
        DefineFeedDetailsController.prototype.getRegisteredTemplate = function () {
            var _this = this;
            if (this.model.templateId != null && this.model.templateId != '') {
                var promise = this.$http.get(this.RestUrlService.GET_REGISTERED_TEMPLATE_URL(this.model.templateId), { params: { feedEdit: true, allProperties: true } });
                promise.then(function (response) {
                    _this.initializeProperties(response.data);
                }, function (err) { });
                return promise;
            }
        };
        /**
             * Validates the step for enable/disable of the step and continue button
             */
        DefineFeedDetailsController.prototype.validate = function () {
            this.isValid = this.loading == false && this.model.systemFeedName != '' && this.model.systemFeedName != null && this.model.templateId != null
                && (this.inputProcessors.length == 0 || (this.inputProcessors.length > 0 && this.inputProcessorId != null));
        };
        DefineFeedDetailsController.prototype.clonedAndInputChanged = function (inputProcessorId) {
            return (this.model.cloned == true && this.inputChangedCounter > 1);
        };
        DefineFeedDetailsController.prototype.notCloned = function () {
            return (angular.isUndefined(this.model.cloned) || this.model.cloned == false);
        };
        /**
             * Updates the details when the processor is changed.
             *
             * @param {string} processorId the processor id
             */
        DefineFeedDetailsController.prototype.updateInputProcessor = function (processorId) {
            // Find the processor object
            var processor = _.find(this.inputProcessors, function (processor) {
                return (processor.processorId === processorId);
            });
            if (angular.isUndefined(processor)) {
                return;
            }
            this.inputChangedCounter++;
            // Determine render type
            var renderGetTableData = this.FeedDetailsProcessorRenderingHelper.updateGetTableDataRendering(processor, this.model.nonInputProcessors);
            if (renderGetTableData) {
                if (this.model.table.method != null && this.model.table.method != 'EXISTING_TABLE') {
                    //reset the fields
                    this.model.table.tableSchema.fields = [];
                }
                this.model.table.method = 'EXISTING_TABLE';
                this.model.options.skipHeader = true;
                this.model.allowSkipHeaderOption = true;
            }
            else if (this.model.templateTableOption != "DATA_TRANSFORMATION" && (this.clonedAndInputChanged(processorId) || this.notCloned())) {
                this.model.table.method = 'SAMPLE_FILE';
                this.model.table.tableSchema.fields = [];
            }
            // Update model
            this.model.inputProcessor = processor;
            this.model.inputProcessorType = processor.type;
        };
        DefineFeedDetailsController.$inject = ["$scope", "$http", "RestUrlService", "FeedService", "RegisterTemplateService",
            "FeedInputProcessorOptionsFactory", "BroadcastService", "StepperService", "FeedDetailsProcessorRenderingHelper"];
        return DefineFeedDetailsController;
    }());
    exports.DefineFeedDetailsController = DefineFeedDetailsController;
    angular.module(moduleName).
        component("thinkbigDefineFeedDetails", {
        bindings: {
            stepIndex: '@'
        },
        require: {
            stepperController: "^thinkbigStepper"
        },
        controllerAs: 'vm',
        controller: DefineFeedDetailsController,
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-details.html',
    });
});
//# sourceMappingURL=DefineFeedDetailsDirective.js.map