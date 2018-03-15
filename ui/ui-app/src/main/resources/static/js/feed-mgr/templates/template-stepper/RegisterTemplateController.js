define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var RegisterTemplateController = /** @class */ (function () {
        function RegisterTemplateController($scope, $transition$, $http, $mdToast, $q, RegisterTemplateService, StateService, AccessControlService) {
            var _this = this;
            this.$scope = $scope;
            this.$transition$ = $transition$;
            this.$http = $http;
            this.$mdToast = $mdToast;
            this.$q = $q;
            this.RegisterTemplateService = RegisterTemplateService;
            this.StateService = StateService;
            this.AccessControlService = AccessControlService;
            this.init = function () {
                _this.loading = true;
                //Wait for the properties to come back before allowing the user to go to the next step
                _this.RegisterTemplateService.loadTemplateWithProperties(_this.registeredTemplateId, _this.nifiTemplateId).then(function (response) {
                    _this.loading = false;
                    _this.RegisterTemplateService.warnInvalidProcessorNames();
                    _this.$q.when(_this.RegisterTemplateService.checkTemplateAccess()).then(function (response) {
                        if (!response.isValid) {
                            //PREVENT access
                        }
                        _this.allowAccessControl = response.allowAccessControl;
                        _this.allowAdmin = response.allowAdmin;
                        _this.allowEdit = response.allowEdit;
                        _this.updateAccessControl();
                    });
                }, function (err) {
                    _this.loading = false;
                    _this.RegisterTemplateService.resetModel();
                    _this.allowAccessControl = false;
                    _this.allowAdmin = false;
                    _this.allowEdit = false;
                    _this.updateAccessControl();
                });
            };
            this.updateAccessControl = function () {
                if (!_this.allowAccessControl && _this.stepperController) {
                    //deactivate the access control step
                    _this.stepperController.deactivateStep(3);
                }
                else if (_this.stepperController) {
                    _this.stepperController.activateStep(3);
                }
            };
            this.cancelStepper = function () {
                //or just reset the url
                _this.RegisterTemplateService.resetModel();
                _this.stepperUrl = null;
                _this.StateService.FeedManager().Template().navigateToRegisteredTemplates();
            };
            this.onStepperInitialized = function (stepper) {
                _this.stepperController = stepper;
                if (!_this.AccessControlService.isEntityAccessControlled()) {
                    //disable Access Control
                    stepper.deactivateStep(3);
                }
                _this.updateAccessControl();
            };
            /**
             * Reference to the RegisteredTemplate Kylo id passed when editing a template
             * @type {null|*}
             */
            this.registeredTemplateId = $transition$.params().registeredTemplateId || null;
            /**
             * Reference to the NifiTemplate Id. Used if kylo id above is not present
             * @type {null|*}
             */
            this.nifiTemplateId = $transition$.params().nifiTemplateId || null;
            /**
             * The model being edited/created
             */
            this.model = RegisterTemplateService.model;
            this.allowAccessControl = false;
            this.allowAdmin = false;
            this.allowEdit = false;
            /**
             * The Stepper Controller set after initialized
             * @type {null}
             */
            this.stepperController = null;
            this.init();
        }
        return RegisterTemplateController;
    }());
    exports.RegisterTemplateController = RegisterTemplateController;
    angular.module(module_name_1.moduleName).controller('RegisterTemplateController', ["$scope", "$transition$", "$http", "$mdToast", "$q", "RegisterTemplateService", "StateService", "AccessControlService", RegisterTemplateController]);
});
//# sourceMappingURL=RegisterTemplateController.js.map