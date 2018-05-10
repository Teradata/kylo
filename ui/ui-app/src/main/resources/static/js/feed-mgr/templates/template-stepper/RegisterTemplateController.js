define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var RegisterTemplateController = /** @class */ (function () {
        function RegisterTemplateController($scope, $http, $mdToast, $q, RegisterTemplateService, StateService, AccessControlService, BroadcastService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$mdToast = $mdToast;
            this.$q = $q;
            this.RegisterTemplateService = RegisterTemplateService;
            this.StateService = StateService;
            this.AccessControlService = AccessControlService;
            this.BroadcastService = BroadcastService;
            this.allowAccessControl = false;
            this.allowAdmin = false;
            this.allowEdit = false;
            /**
            * The Stepper Controller set after initialized
            * @type {null}
            */
            this.stepperController = null;
            this.loading = true;
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
            this.registeredTemplateId = this.$transition$.params().registeredTemplateId || null;
            this.nifiTemplateId = this.$transition$.params().nifiTemplateId || null;
            this.model = this.RegisterTemplateService.model;
        }
        RegisterTemplateController.prototype.$onInit = function () {
            this.ngOnInit();
        };
        RegisterTemplateController.prototype.ngOnInit = function () {
            var _this = this;
            //Wait for the properties to come back before allowing the user to go to the next step
            this.RegisterTemplateService.loadTemplateWithProperties(this.registeredTemplateId, this.nifiTemplateId).then(function (response) {
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
                    _this.BroadcastService.notify("REGISTERED_TEMPLATE_LOADED", "LOADED");
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
        RegisterTemplateController.$inject = ["$scope", "$http", "$mdToast", "$q", "RegisterTemplateService", "StateService", "AccessControlService", "BroadcastService"];
        return RegisterTemplateController;
    }());
    exports.RegisterTemplateController = RegisterTemplateController;
    angular.module(module_name_1.moduleName).component('registerTemplateController', {
        bindings: {
            $transition$: '<'
        },
        templateUrl: 'js/feed-mgr/templates/template-stepper/register-template.html',
        controller: RegisterTemplateController,
        controllerAs: 'vm'
    });
});
//# sourceMappingURL=RegisterTemplateController.js.map