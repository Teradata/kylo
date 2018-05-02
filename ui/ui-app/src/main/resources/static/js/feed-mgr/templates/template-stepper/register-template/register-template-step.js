define(["require", "exports", "angular", "underscore", "../../module-name"], function (require, exports, angular, _, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/feed-mgr/templates/template-stepper/register-template/register-template-step.html',
            controller: "RegisterCompleteRegistrationController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    };
    var RegisterCompleteRegistrationController = /** @class */ (function () {
        function RegisterCompleteRegistrationController($scope, $http, $mdToast, $mdDialog, RestUrlService, StateService, RegisterTemplateService, EntityAccessControlService) {
            this.$scope = $scope;
            this.$http = $http;
            this.$mdToast = $mdToast;
            this.$mdDialog = $mdDialog;
            this.RestUrlService = RestUrlService;
            this.StateService = StateService;
            this.RegisterTemplateService = RegisterTemplateService;
            this.EntityAccessControlService = EntityAccessControlService;
            /**
             * Order of the templates via the dndLists
             * @type {Array}
             */
            this.templateOrder = [];
            /**
             * the angular form
             */
            this.registerTemplateForm = {};
            /**
             * Indicates if the edit form is valid.
             * @type {boolean}
             */
            this.isValid = false;
            /**
             * The Source/Destination Datasources assigned to this template
             * @type {Array}
             */
            this.processorDatasourceDefinitions = [];
            /**
             * flag to tell when the system is loading datasources
             * @type {boolean}
             */
            this.loadingFlowData = true;
            /**
             * Global message to present to the user
             */
            this.message = null;
            /**
             * Flag indicating if the template succeeded registration
             * @type {boolean}
             */
            this.registrationSuccess = false;
            /**
             * A map of the port names to the port Object
             * used for the connections from the outputs to input ports
             * @type {{}}
             */
            this.connectionMap = {};
            /**
             * The available options in the list of possible inputPorts to connect to
             * @type {Array} of {label: port.name, value: port.name}
             */
            this.inputPortList = [];
            this.remoteProcessGroupValidation = { validatingPorts: true, valid: false, invalidMessage: null };
            /**
             * The Template Model
             */
            this.model = RegisterTemplateService.model;
            //set the step number
            this.stepNumber = parseInt(this.stepIndex) + 1;
        }
        RegisterCompleteRegistrationController.prototype.onInit = function () {
            var _this = this;
            /**
             * Initialize the connections and flow data
             */
            this.initTemplateFlowData();
            /**
             * if this template has a RemotePRocessGroup then validate to make sure its connection to the remote input port exists
             */
            this.validateRemoteInputPort();
            this.$scope.$watch(function () {
                return _this.model.nifiTemplateId;
            }, function (newVal) {
                if (newVal != null) {
                    _this.registrationSuccess = false;
                }
            });
        };
        RegisterCompleteRegistrationController.prototype.$onInit = function () {
            this.onInit();
        };
        /**
         * Calls the server to get all the Datasources and the Flow processors and flow types
         * Caled initially when the page loads and then any time a user changes a input port connection
         */
        RegisterCompleteRegistrationController.prototype.buildTemplateFlowData = function () {
            var _this = this;
            this.loadingFlowData = true;
            var assignedPortIds = [];
            _.each(this.model.reusableTemplateConnections, function (conn) {
                var inputPort = conn.inputPortDisplayName;
                var port = _this.connectionMap[inputPort];
                if (port != undefined) {
                    assignedPortIds.push(port.id);
                }
            });
            var selectedPortIds = '';
            if (assignedPortIds.length > 0) {
                selectedPortIds = assignedPortIds.join(",");
            }
            //only attempt to query if we have connections set
            var hasPortConnections = (this.model.reusableTemplateConnections == null || this.model.reusableTemplateConnections.length == 0) || (this.model.reusableTemplateConnections != null && this.model.reusableTemplateConnections.length > 0 && assignedPortIds.length == this.model.reusableTemplateConnections.length);
            if (hasPortConnections) {
                this.RegisterTemplateService.getNiFiTemplateFlowInformation(this.model.nifiTemplateId, this.model.reusableTemplateConnections).then(function (response) {
                    var map = {};
                    if (response && response.data) {
                        var datasourceDefinitions = response.data.templateProcessorDatasourceDefinitions;
                        //merge in those already selected/saved on this template
                        _.each(datasourceDefinitions, function (def) {
                            def.selectedDatasource = false;
                            if (_this.model.registeredDatasourceDefinitions.length == 0) {
                                def.selectedDatasource = true;
                            }
                            else {
                                var matchingTypes = _.filter(_this.model.registeredDatasourceDefinitions, function (ds) {
                                    return (def.processorType == ds.processorType && (ds.processorId == def.processorId || ds.processorName == def.processorName));
                                });
                                if (matchingTypes.length > 0) {
                                    def.selectedDatasource = true;
                                }
                            }
                        });
                        //sort with SOURCE's first
                        _this.processorDatasourceDefinitions = _.sortBy(datasourceDefinitions, function (def) {
                            if (def.datasourceDefinition.connectionType == 'SOURCE') {
                                return 1;
                            }
                            else {
                                return 2;
                            }
                        });
                    }
                    _this.loadingFlowData = false;
                });
            }
            else {
                this.loadingFlowData = false;
            }
        };
        ;
        /**
         * if this template has a RemotePRocessGroup then validate to make sure its connection to the remote input port exists
         */
        RegisterCompleteRegistrationController.prototype.validateRemoteInputPort = function () {
            var _this = this;
            this.remoteProcessGroupValidation.validatingPorts = true;
            this.remoteProcessGroupValidation.invalidMessage = "";
            if (this.model.additionalProperties) {
                var remoteInputPortProperty_1 = _.find(this.model.additionalProperties, function (prop) {
                    return prop.processorType = "REMOTE_PROCESS_GROUP" && prop.key == "Remote Input Port";
                });
                if (remoteInputPortProperty_1 != undefined) {
                    //validate it against the possible remote input ports
                    this.RegisterTemplateService.fetchRootInputPorts().then(function (response) {
                        if (response && response.data) {
                            var matchingPort = _.find(response.data, function (port) {
                                return port.name == remoteInputPortProperty_1.value;
                            });
                            if (matchingPort != undefined) {
                                //VALID
                                _this.remoteProcessGroupValidation.valid = true;
                            }
                            else {
                                //INVALID
                                _this.remoteProcessGroupValidation.valid = false;
                                _this.remoteProcessGroupValidation.invalidMessage = "The Remote Input Port defined for this Remote Process Group, <b>" + remoteInputPortProperty_1.value + "</b> does not exist.<br/>" +
                                    "You need to register a reusable template with this '<b>" + remoteInputPortProperty_1.value + "</b>' input port and check the box 'Remote Process Group Aware' when registering to make it available to this template. ";
                            }
                            _this.remoteProcessGroupValidation.validatingPorts = false;
                        }
                    }, function (err) {
                        _this.remoteProcessGroupValidation.validatingPorts = false;
                        _this.remoteProcessGroupValidation.valid = false;
                        _this.remoteProcessGroupValidation.invalidMessage = "Unable to verify input port connections for your remote process group";
                    });
                }
                else {
                    this.remoteProcessGroupValidation.validatingPorts = false;
                    this.remoteProcessGroupValidation.valid = true;
                }
            }
            else {
                this.remoteProcessGroupValidation.validatingPorts = false;
                this.remoteProcessGroupValidation.valid = true;
            }
        };
        /**
         * Fetches the output/input ports and walks the flow to build the processor graph for the Data sources and the flow types
         */
        RegisterCompleteRegistrationController.prototype.initTemplateFlowData = function () {
            var _this = this;
            if (this.model.needsReusableTemplate) {
                this.RegisterTemplateService.fetchRegisteredReusableFeedInputPorts().then(function (response) {
                    // Update connectionMap and inputPortList
                    _this.inputPortList = [];
                    if (response.data) {
                        angular.forEach(response.data, function (port, i) {
                            _this.inputPortList.push({ label: port.name, value: port.name, description: port.destinationProcessGroupName });
                            _this.connectionMap[port.name] = port;
                        });
                    }
                    // Check for invalid connections
                    angular.forEach(_this.model.reusableTemplateConnections, function (connection) {
                        //initially mark as valid
                        _this.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
                        if (!angular.isDefined(_this.connectionMap[connection.inputPortDisplayName])) {
                            connection.inputPortDisplayName = null;
                            //mark as invalid
                            _this.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", false);
                        }
                    });
                    _this.buildTemplateFlowData();
                });
            }
            else {
                this.buildTemplateFlowData();
            }
        };
        /**
         * Called when the user changes the output port connections
         * @param connection
         */
        RegisterCompleteRegistrationController.prototype.onReusableTemplateConnectionChange = function (connection) {
            var port = this.connectionMap[connection.inputPortDisplayName];
            connection.reusableTemplateInputPortName = port.name;
            //mark as valid
            this.registerTemplateForm["port-" + connection.feedOutputPortName].$setValidity("invalidConnection", true);
            this.buildTemplateFlowData();
        };
        ;
        /**
         * Called when the user clicks to change the icon
         */
        RegisterCompleteRegistrationController.prototype.showIconPicker = function () {
            var _this = this;
            var iconModel = { icon: this.model.icon.title, iconColor: this.model.icon.color };
            iconModel.name = this.model.templateName;
            this.$mdDialog.show({
                controller: 'IconPickerDialog',
                templateUrl: 'js/common/icon-picker-dialog/icon-picker-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    iconModel: iconModel
                }
            }).then(function (msg) {
                if (msg) {
                    _this.model.icon.title = msg.icon;
                    _this.model.icon.color = msg.color;
                }
            }, function () {
            });
        };
        ;
        /**
         * Called when the user clicks Register
         * @returns {*}
         */
        RegisterCompleteRegistrationController.prototype.registerTemplate = function () {
            var _this = this;
            this.showRegistrationInProgressDialog();
            var successFn = function (response) {
                _this.$mdDialog.hide();
                var message = 'Template Registered with ' + response.data.properties.length + ' properties';
                _this.registrationSuccess = true;
                _this.$mdToast.show(_this.$mdToast.simple()
                    .textContent(message)
                    .hideDelay(3000));
                _this.StateService.FeedManager().Template().navigateToRegisterTemplateComplete(message, _this.model, null);
            };
            var errorFn = function (response) {
                _this.$mdDialog.hide();
                var message = 'Error Registering Template ' + response.data.message;
                _this.registrationSuccess = false;
                _this.$mdToast.show(_this.$mdToast.simple()
                    .textContent(message)
                    .hideDelay(3000));
                _this.showErrorDialog(message);
            };
            //get all properties that are selected
            var savedTemplate = this.RegisterTemplateService.getModelForSave();
            //prepare access control changes if any
            this.EntityAccessControlService.updateRoleMembershipsForSave(savedTemplate.roleMemberships);
            //get template order
            var order = [];
            _.each(this.templateOrder, function (template) {
                order.push(template.id);
            });
            savedTemplate.templateOrder = order;
            var thisOrder = order.length - 1;
            if (this.model.id != undefined) {
                thisOrder = _.indexOf(order, this.model.id);
            }
            else {
                thisOrder = _.indexOf(order, 'NEW');
            }
            savedTemplate.order = thisOrder;
            //add in the datasources
            var selectedDatasourceDefinitions = _.filter(this.processorDatasourceDefinitions, function (ds) {
                return ds.selectedDatasource == true;
            });
            savedTemplate.registeredDatasourceDefinitions = selectedDatasourceDefinitions;
            var promise = this.$http({
                url: this.RestUrlService.REGISTER_TEMPLATE_URL(),
                method: "POST",
                data: angular.toJson(savedTemplate),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);
            return promise;
        };
        /**
         * Shows a dialog with a progress when the registration is in progress
         */
        RegisterCompleteRegistrationController.prototype.showRegistrationInProgressDialog = function () {
            //hide any dialogs
            this.$mdDialog.hide();
            this.$mdDialog.show({
                controller: ["$scope", "templateName", RegistrationInProgressDialogController],
                templateUrl: 'js/feed-mgr/templates/template-stepper/register-template/register-template-inprogress-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    templateName: this.model.templateName
                }
            });
        };
        /**
         * Shows an Error dialog with a message
         * @param message
         */
        RegisterCompleteRegistrationController.prototype.showErrorDialog = function (message) {
            this.$mdDialog.show({
                controller: ["$scope", "$mdDialog", "nifiTemplateId", "templateName", "message", RegistrationErrorDialogController],
                templateUrl: 'js/feed-mgr/templates/template-stepper/register-template/register-template-error-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: true,
                fullscreen: true,
                locals: {
                    nifiTemplateId: this.model.nifiTemplateId,
                    templateName: this.model.templateName,
                    message: message
                }
            });
        };
        ;
        RegisterCompleteRegistrationController.$inject = ["$scope", "$http", "$mdToast", "$mdDialog", "RestUrlService", "StateService", "RegisterTemplateService", "EntityAccessControlService"];
        return RegisterCompleteRegistrationController;
    }());
    exports.RegisterCompleteRegistrationController = RegisterCompleteRegistrationController;
    angular.module(module_name_1.moduleName).controller("RegisterCompleteRegistrationController", RegisterCompleteRegistrationController);
    angular.module(module_name_1.moduleName).controller("RegisterTemplateCompleteController", ["StateService", RegisterTemplateCompleteController]);
    angular.module(module_name_1.moduleName).directive("thinkbigRegisterCompleteRegistration", directive);
    function RegistrationErrorDialogController($scope, $mdDialog, nifiTemplateId, templateName, message) {
        $scope.nifiTemplateId = nifiTemplateId;
        $scope.templateName = templateName;
        $scope.message = message;
        $scope.gotIt = function () {
            $mdDialog.cancel();
        };
    }
    function RegistrationInProgressDialogController($scope, templateName) {
        $scope.templateName = templateName;
    }
    function RegisterTemplateCompleteController(StateService) {
        this.gotIt = function () {
            StateService.FeedManager().Template().navigateToRegisteredTemplates();
        };
    }
});
//# sourceMappingURL=register-template-step.js.map