(function() {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            require: ['thinkbigDefineFeedDetails', '^thinkbigStepper'],
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/define-feed/feed-details/define-feed-details.html',
            controller: "DefineFeedDetailsController",
            link: function($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }
        };
    };

    function DefineFeedDetailsController($scope, $http, RestUrlService, FeedService, RegisterTemplateService, FeedInputProcessorOptionsFactory, BroadcastService, StepperService,
                                         FeedDetailsProcessorRenderingHelper) {
        var self = this;

        /**
         * The Angular Form for validation
         * @type {{}}
         */
        this.feedDetailsForm = {};

        this.stepNumber = parseInt(this.stepIndex) + 1;
        this.model = FeedService.createFeedModel;

        this.inputProcessors = [];

        this.inputProcessorId = null;

        this.isValid = false;

        this.stepperController = null;
        var watchers = [];

        this.codemirrorRenderTypes = RegisterTemplateService.codemirrorRenderTypes;

        var inputDatabaseType = ["com.thinkbiganalytics.nifi.GetTableData"]

        BroadcastService.subscribe($scope, StepperService.ACTIVE_STEP_EVENT, onActiveStep)

        function onActiveStep(event, index) {
            if (index == parseInt(self.stepIndex)) {
                validate();
            }
        }

        /**
         * Finds the allowed controller services for the specified property and sets the allowable values.
         *
         * @param {Object} property the property to be updated
         */
        function findControllerServicesForProperty(property) {
            // Show progress indicator
            property.isLoading = true;

            // Fetch the list of controller services
            FeedService.getAvailableControllerServices(property.propertyDescriptor.identifiesControllerService)
                    .then(function(services) {
                        // Update the allowable values
                        property.isLoading = false;
                        property.propertyDescriptor.allowableValues = _.map(services, function(service) {
                            return {displayName: service.name, value: service.id}
                        });
                    }, function() {
                        // Hide progress indicator
                        property.isLoading = false;
                    });
        }

        /**
         * Prepares the processor properties of the specified template for display.
         *
         * @param {Object} template the template with properties
         */
        function initializeProperties(template) {
            RegisterTemplateService.initializeProperties(template, 'create', self.model.properties);
            self.inputProcessors = RegisterTemplateService.removeNonUserEditableProperties(template.inputProcessors, true);
            //self.model.inputProcessor = _.find(self.model.inputProcessors,function(processor){
            //    return self.model.inputProcessorType == processor.type;
            // });
            self.model.nonInputProcessors = RegisterTemplateService.removeNonUserEditableProperties(template.nonInputProcessors, false);

            if (self.inputProcessorId == null && self.inputProcessors != null && self.inputProcessors.length > 0) {
                self.inputProcessorId = self.inputProcessors[0].id;
            }
            // Skip this step if it's empty
            if (self.inputProcessors.length === 0 && !_.some(self.nonInputProcessors, function(processor) {
                        return processor.userEditable
                    })) {
                StepperService.getStep("DefineFeedStepper", parseInt(self.stepIndex)).skip = true;
            }

            // Find controller services
            _.chain(template.inputProcessors.concat(template.nonInputProcessors))
                    .pluck("properties")
                    .flatten(true)
                    .filter(function(property) {
                        return angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService);
                    })
                    .each(findControllerServicesForProperty);
        }

        /**
         * gets the Registered Template
         * Activates/Deactivates the Table and Data Processing Steps based upon if the template is registered to define the Table or not
         * @returns {HttpPromise}
         */
        function getRegisteredTemplate() {
            if (self.model.templateId != null && self.model.templateId != '') {
                var successFn = function(response) {
                    initializeProperties(response.data);
                };
                var errorFn = function(err) {

                };
                var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATE_URL(self.model.templateId));
                promise.then(successFn, errorFn);
                return promise;
            }
        }

        /**
         * Validates the step for enable/disable of the step and continue button
         */
        function validate() {
            self.isValid = self.model.systemFeedName != '' && self.model.systemFeedName != null && self.model.templateId != null
                           && (self.inputProcessors.length == 0 || (self.inputProcessors.length > 0 && self.inputProcessorId != null));
        }

        var inputProcessorIdWatch = $scope.$watch(function() {
            return self.inputProcessorId;
        }, function(newVal) {
            updateInputProcessor(newVal);
            validate();
        });

        var systemFeedNameWatch = $scope.$watch(function() {
            return self.model.systemFeedName;
        }, function(newVal) {
            validate();
        });

        var templateIdWatch = $scope.$watch(function() {
            return self.model.templateId;
        }, function(newVal) {
            getRegisteredTemplate();
        });

        function setRenderTemplateForProcessor(processor) {
            if (processor.feedPropertiesUrl == undefined) {
                processor.feedPropertiesUrl = null;
            }
            if (processor.feedPropertiesUrl == null) {
                processor.feedPropertiesUrl = FeedInputProcessorOptionsFactory.templateForProcessor(processor, 'create');

            }
        }

        /**
         * Updates the details when the processor is changed.
         *
         * @param {string} processorId the processor id
         */
        function updateInputProcessor(processorId) {
            // Find the processor object
            var processor = _.find(self.inputProcessors, function(processor) {
                return (processor.processorId === processorId);
            });
            if (angular.isUndefined(processor)) {
                return;
            }

            // Determine render type
            var renderGetTableData = FeedDetailsProcessorRenderingHelper.updateGetTableDataRendering(processor, self.model.nonInputProcessors);
            // var renderSqoop = FeedDetailsProcessorRenderingHelper.updateSqoopProcessorRendering(processor, self.model.nonInputProcessors);

            if (renderGetTableData) {
                self.model.table.method = 'EXISTING_TABLE';
                self.model.options.skipHeader = true;
                self.model.allowSkipHeaderOption = false;
            } else {
                self.model.table.method = 'SAMPLE_FILE';
            }

            // Update model
            self.model.inputProcessor = processor;
            self.model.inputProcessorType = processor.type;
        }

        $scope.$on('$destroy', function() {
            systemFeedNameWatch();
            templateIdWatch();
            inputProcessorIdWatch();
        })

    }

    angular.module(MODULE_FEED_MGR).controller('DefineFeedDetailsController', DefineFeedDetailsController);
    angular.module(MODULE_FEED_MGR).directive('thinkbigDefineFeedDetails', directive);
})();
