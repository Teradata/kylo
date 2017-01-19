(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/register-template/register-template/register-template-step.html',
            controller: "RegisterCompleteRegistrationController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    };

    function RegisterCompleteRegistrationController($scope, $http, $mdToast, $rootScope, $mdPanel, $mdDialog, RestUrlService, StateService, RegisterTemplateService) {

        /**
         * ref back to this controller
         * @type {RegisterCompleteRegistrationController}
         */
        var self = this;

        /**
         * Order of the templates via the dndLists
         * @type {Array}
         */
        this.templateOrder = [];

        /**
         * the angular form
         */
        self.registerTemplateForm = {};

        /**
         * Indicates if the edit form is valid.
         * @type {boolean}
         */
        self.isValid = false;

        /**
         * The Source/Destination Datasources assigned to this template
         * @type {Array}
         */
        self.processorDatasourceDefinitions = [];

        /**
         * The processors that have been marked as special flow types for this template
         * @type [{"flowId":"","relationship":"","flowType":""}...]
         */
        self.templateFlowTypeProcessors = []

        self.processorRelationshipOptions = ["success", "failure", "all"];

        /**
         * Available FlowType options (i.e. Failure,Warning)
         * @type {Array}
         */
        self.processorFlowTypeOptions = [];

        /**
         * The Leaf processors in this template (and joining reusable flows) along with their 'flowId' and 'flowtype'
         * @type {Array}
         */
        self.leafProcessors = [];

        /**
         * A map with the processor FlowId and its corresponding FlowType.
         * @type {{}}
         */
        self.flowIdFlowProcessorMap = {};

        /**
         * flag to tell when the system is loading datasources
         * @type {boolean}
         */
        self.loadingFlowData = true;

        /**
         * The Template Model
         */
        this.model = RegisterTemplateService.model;

        this.message = null;

        /**
         * Flag indicating if the template succeeded registration
         * @type {boolean}
         */
        this.registrationSuccess = false;
        /**
         * The Step number
         * @type {number}
         */
        this.stepNumber = parseInt(this.stepIndex) + 1

        /**
         * The possible options to choose how this template should be displayed in the Feed Stepper
         * @type {[*]}
         */
        this.templateTableOptions =
            [{type: 'NO_TABLE', displayName: 'No table customization', description: 'User will not be given option to customize destination table'},
                {type: 'DEFINE_TABLE', displayName: 'Customize destination table', description: 'Allow users to define and customize the destination data table.'}, {
                type: 'DATA_TRANSFORMATION',
                displayName: 'Data Transformation',
                description: 'Users pick and choose different data tables and columns and apply functions to transform the data to their desired destination table'
            }]

        /**
         * the selected option for the template
         * @type {*}
         */
        this.templateTableOption = this.model.templateTableOption;

        /**
         * A map of the port names to the port Object
         * used for the connections from the outputs to input ports
         * @type {{}}
         */
        self.connectionMap = {};

        /**
         * The available options in the list of possible inputPorts to connect to
         * @type {Array} of {label: port.name, value: port.name}
         */
        self.inputPortList = [];

        // setup the Stepper types
        var initTemplateTableOptions = function () {
            if (self.templateTableOption == undefined) {

                if (self.model.defineTable) {
                    self.templateTableOption = 'DEFINE_TABLE'
                }
                else if (self.model.dataTransformation) {
                    self.templateTableOption = 'DATA_TRANSFORMATION'
                }
                else if (self.model.reusableTemplate) {
                    self.templateTableOption = 'COMMON_REUSABLE_TEMPLATE'
                }
                else {
                    self.templateTableOption = 'NO_TABLE'
                }
            }
        }

        /**
         * Calls the server to get all the Datasources and the Flow processors and flow types
         * Caled initially when the page loads and then any time a user changes a input port connection
         */
        function buildTemplateFlowData() {
            self.loadingFlowData = true;
            var assignedPortIds = [];
            _.each(self.model.reusableTemplateConnections, function (conn) {
                var inputPort = conn.inputPortDisplayName;
                var port = self.connectionMap[inputPort];
                if (port != undefined) {
                    assignedPortIds.push(port.id);
                }
            });
            var selectedPortIds = '';
            if (assignedPortIds.length > 0) {
                selectedPortIds = assignedPortIds.join(",");
            }

            RegisterTemplateService.getNiFiTemplateFlowInformation(self.model.nifiTemplateId, self.model.reusableTemplateConnections).then(function (response) {
                var map = {};

                if (response && response.data) {

                    var datasourceDefinitions = response.data.templateProcessorDatasourceDefinitions;

                    self.leafProcessors = _.filter(response.data.processors, function (processor) {
                        return processor.leaf == true;
                    });

                    self.processorFlowTypeOptions = response.data.processorFlowTypes;

                    //merge in those already selected/saved on this template
                    _.each(datasourceDefinitions, function (def) {
                        def.selectedDatasource = false;
                        if (self.model.registeredDatasourceDefinitions.length == 0) {
                            def.selectedDatasource = true;
                        }
                        else {

                            var matchingTypes = _.filter(self.model.registeredDatasourceDefinitions, function (ds) {
                                return (def.processorType == ds.processorType && ( ds.processorId == def.processorId || ds.processorName == def.processorName));
                            });
                            if (matchingTypes.length > 0) {
                                def.selectedDatasource = true;
                            }
                        }
                    });
                    //sort with SOURCEs first
                    self.processorDatasourceDefinitions = _.sortBy(datasourceDefinitions, function (def) {
                        if (def.datasourceDefinition.connectionType == 'SOURCE') {
                            return 1;
                        }
                        else {
                            return 2;
                        }
                    });

                    //reset the array
                    self.templateFlowTypeProcessors = [];

                    //get the map of processors by flowId
                    self.flowIdFlowProcessorMap = _.indexBy(self.leafProcessors, 'flowId');
                    var mappedIds = {};
                    //merge in the saved flowprocessor types with the ones on the template
                    //{processorId: [{relationship:'',flowType:''}...]}
                    _.each(self.model.processorFlowTypesMap, function (flowTypeRelationships, processorFlowId) {

                        var flowProcessor = self.flowIdFlowProcessorMap[processorFlowId];

                        if (flowProcessor != undefined) {
                            _.each(flowTypeRelationships, function (flowTypeRelationship) {
                                self.templateFlowTypeProcessors.push(
                                    {flowId: processorFlowId, relationship: flowTypeRelationship.relationship, flowType: flowTypeRelationship.flowType, _id: _.uniqueId()});
                                mappedIds[processorFlowId] = processorFlowId;
                            });
                        }
                    });

                    _.each(self.leafProcessors, function (flowProcessor) {
                        //add in any unmapped leaf nodes as failures
                        if (mappedIds[flowProcessor.flowId] == undefined) {
                            self.templateFlowTypeProcessors.push({flowId: flowProcessor.flowId, relationship: 'failure', flowType: 'FAILURE', _id: _.uniqueId()});
                        }

                    });

                }
                self.loadingFlowData = false;
            });

        };

        /**
         * Fetches the output/input ports and walks the flow to build the processor graph for the Data sources and the flow types
         */
        var initTemplateFlowData = function () {
            if (self.model.needsReusableTemplate) {
                RegisterTemplateService.fetchRegisteredReusableFeedInputPorts().then(function (response) {
                    // Update connectionMap and inputPortList
                    self.inputPortList = [];
                    if (response.data) {
                        angular.forEach(response.data, function (port, i) {
                            self.inputPortList.push({label: port.name, value: port.name});
                            self.connectionMap[port.name] = port;
                        });
                    }

                    // Check for invalid connections
                    angular.forEach(self.model.reusableTemplateConnections, function (connection) {
                        //initially mark as valid
                        self.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
                        if (!angular.isDefined(self.connectionMap[connection.inputPortDisplayName])) {
                            connection.inputPortDisplayName = null;
                            // self.$error["port-" + connection.feedOutputPortName] = true;
                            //mark as invalid
                            self.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", false);
                        }
                    });

                    buildTemplateFlowData();
                });
            }
            else {
                buildTemplateFlowData();
            }
        }

        /**
         * Initialze the template options
         */
        initTemplateTableOptions();

        /**
         * Initialize the connections and flow data
         */
        initTemplateFlowData();

        this.removeProcessorFlowType = function (flowTypeRelationship) {
            var item = _.find(self.templateFlowTypeProcessors, function (flowTypeRelationship) {
                return flowTypeRelationship._id == flowTypeRelationship._id;
            });
            if (item != undefined) {
                //remove it
                self.templateFlowTypeProcessors.splice(_.indexOf(self.templateFlowTypeProcessors, flowTypeRelationship), 1);
            }
        }

        /**
         * Adds a placeholder processor type with a marker field of 'isEmpty' to indicate this processortype is new and needs to be set
         */
        this.addFlowProcessorType = function () {
            var alreadyAdding = _.find(self.templateFlowTypeProcessors, function (flowTypeRelationship) {
                return flowTypeRelationship.isEmpty != undefined && flowTypeRelationship.isEmpty == true;
            });
            if (alreadyAdding == undefined) {
                self.templateFlowTypeProcessors.push({isNew: true, isEmpty: true, name: '', flowId: '', flowType: 'NORMAL_FLOW', relationship: 'success', id: _.uniqueId()})
            }
        }

        /**
         * Called when the user changes the processor in the select for defining the flow types
         * @param processor
         */
        this.changeProcessorFlowType = function (flowTypeRelationship) {

            //remove the isEmpty marker to allow for another Add
            if (flowTypeRelationship.isEmpty != undefined) {
                flowTypeRelationship.isEmpty = undefined
            }
            //   var matchingProcessor = self.flowIdFlowProcessorMap[flowTypeRelationship.flowId];
            //  flowTypeRelationship.flowId = matchingProcessor.flowId
            //  angular.extend(processor, matchingProcessor);
        }

        /**
         * Called when the user changes the radio buttons
         */
        this.onTableOptionChange = function () {

            if (self.templateTableOption == 'DEFINE_TABLE') {
                self.model.defineTable = true;
                self.model.dataTransformation = false;
            }
            else if (self.templateTableOption == 'DATA_TRANSFORMATION') {
                self.model.defineTable = false;
                self.model.dataTransformation = true;
            }
            else if (self.templateTableOption == 'NO_TABLE') {
                self.model.defineTable = false;
                self.model.dataTransformation = false;
            }
        }

        /**
         * Called when the user changes the output port connections
         * @param connection
         */
        self.onReusableTemplateConnectionChange = function (connection) {
            var port = self.connectionMap[connection.inputPortDisplayName];
            connection.reusableTemplateInputPortName = port.name;
            //mark as valid
            self.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
            buildTemplateFlowData();
        };

        /**
         * Called when the user clicks to change the icon
         */
        this.showIconPicker = function () {
            var iconModel = {icon: self.model.icon.title, iconColor: self.model.icon.color};
            iconModel.name = self.model.templateName;

            $mdDialog.show({
                controller: 'IconPickerDialog',
                templateUrl: 'js/shared/icon-picker-dialog/icon-picker-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    iconModel: iconModel
                }
            }).then(function (msg) {
                if (msg) {
                    self.model.icon.title = msg.icon;
                    self.model.icon.color = msg.color;
                }

            }, function () {

            });
        };

        /**
         * Called when the user clicks Register
         * @returns {*}
         */
        this.registerTemplate = function () {

            showRegistrationInProgressDialog();
            var successFn = function (response) {
                $mdDialog.hide();
                var message = 'Template Registered with ' + response.data.properties.length + ' properties';
                self.registrationSuccess = true;

                $mdToast.show(
                    $mdToast.simple()
                        .textContent(message)
                        .hideDelay(3000)
                );
                StateService.navigateToRegisterTemplateComplete(message, self.model, null);
            }
            var errorFn = function (err) {
                $mdDialog.hide();
                var message = 'Error Registering Template ' + err;
                self.registrationSuccess = false;
                $mdToast.show(
                    $mdToast.simple()
                        .textContent(message)
                        .hideDelay(3000)
                );
                showErrorDialog(message);
            }

            //get all properties that are selected
            var savedTemplate = RegisterTemplateService.getModelForSave();
            //get template order
            var order = [];
            _.each(self.templateOrder, function (template) {
                order.push(template.id);
            });
            savedTemplate.templateOrder = order;
            //create the flowid,flowtype map that wil be persisted back to the template on save

            var flowTypeRelationships = {};
            _.each(self.templateFlowTypeProcessors, function (flowTypeRel) {
                var relationships = flowTypeRelationships[flowTypeRel.flowId];
                if (relationships == undefined) {
                    flowTypeRelationships[flowTypeRel.flowId] = [];
                }
                flowTypeRelationships[flowTypeRel.flowId].push({"relationship": flowTypeRel.relationship, "flowType": flowTypeRel.flowType});
            })

            savedTemplate.processorFlowTypesMap = flowTypeRelationships;

            var thisOrder = order.length - 1;
            if (self.model.id != undefined) {
                thisOrder = _.indexOf(order, self.model.id)
            }
            else {
                thisOrder = _.indexOf(order, 'NEW');
            }
            savedTemplate.order = thisOrder

            //add in the datasources
            var selectedDatasourceDefinitions = _.filter(self.processorDatasourceDefinitions, function (ds) {
                return ds.selectedDatasource == true;
            })

            savedTemplate.registeredDatasourceDefinitions = selectedDatasourceDefinitions;

            var promise = $http({
                url: RestUrlService.REGISTER_TEMPLATE_URL(),
                method: "POST",
                data: angular.toJson(savedTemplate),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);
            return promise;
        }

        $scope.$watch(function () {
            return self.model.nifiTemplateId;
        }, function (newVal) {
            if (newVal != null) {
                self.registrationSuccess = false;
            }
        });

        /**
         * Shows a dialog with a progress when the registration is in progress
         */
        var showRegistrationInProgressDialog = function () {
            //hide any dialogs
            $mdDialog.hide();
            $mdDialog.show({
                controller: RegistrationInProgressDialogController,
                templateUrl: 'js/register-template/register-template/register-template-inprogress-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    templateName: self.model.templateName
                }
            })

        }

        /**
         * Shows an Error dialog with a message
         * @param message
         */
        var showErrorDialog = function (message) {

            $mdDialog.show({
                controller: RegistrationErrorDialogController,
                templateUrl: 'js/register-template/register-template/register-template-error-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: true,
                fullscreen: true,
                locals: {
                    nifiTemplateId: self.model.nifiTemplateId,
                    templateName: self.model.templateName,
                    message: message
                }
            });
        };

        this.showFlowTypeOptionsHelpPanel = function (ev) {
            var position = $mdPanel.newPanelPosition()
                .relativeTo('.flow-type-options-help-button')
                .addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.BELOW);

            var config = {
                attachTo: angular.element(document.body),
                controller: FlowTypeOptionsHelpPanelMenuCtrl,
                controllerAs: 'ctrl',
                template: '<div class="register-template-flow-type-help" ' +
                          '     aria-label="Processor flow types." ' +
                          '     role="listbox" layout="column" layout-align="center center">' +
                          '      <span class="md-subheader layout-padding-top-bottom ">Processor Flow Types</span> ' +
                          '  <div class="register-template-flow-type-help-item" ' +
                          '       tabindex="-1" ' +
                          '       role="option" ' +
                          '       ng-repeat="option in ctrl.processorFlowTypeOptions" layout="column">' +
                          '       <span>{{ option.displayName}}</span>' +
                          '       <span class="hint">{{option.description}}</span>' +
                          '  </div>' +
                          '</div>',
                panelClass: 'register-template-flow-type-help',
                position: position,
                locals: {
                    'processorFlowTypeOptions': self.processorFlowTypeOptions
                },
                openFrom: ev,
                clickOutsideToClose: true,
                escapeToClose: true,
                focusOnOpen: false,
                zIndex: 2
            };

            $mdPanel.open(config);
        };

    }

    angular.module(MODULE_FEED_MGR).controller("RegisterCompleteRegistrationController", RegisterCompleteRegistrationController);
    angular.module(MODULE_FEED_MGR).controller("RegisterTemplateCompleteController", RegisterTemplateCompleteController);

    angular.module(MODULE_FEED_MGR).directive("thinkbigRegisterCompleteRegistration", directive);
})();

function RegistrationErrorDialogController($scope, $mdDialog, $mdToast, $http, StateService, nifiTemplateId, templateName, message) {
    $scope.nifiTemplateId = nifiTemplateId;
    $scope.templateName = templateName;
    $scope.message = message;

    $scope.gotIt = function () {
        $mdDialog.cancel();
    };
}

function RegistrationInProgressDialogController($scope, $mdDialog, templateName) {

    $scope.templateName = templateName;
}

function RegisterTemplateCompleteController(StateService, $rootScope, $state, $stateParams) {

    var self = this;
    self.message = $stateParams.message;
    self.model = $stateParams.templateModel;

    this.gotIt = function () {
        StateService.navigateToRegisteredTemplates();
    }

}

function FlowTypeOptionsHelpPanelMenuCtrl(mdPanelRef, $timeout) {
    this._mdPanelRef = mdPanelRef;

}