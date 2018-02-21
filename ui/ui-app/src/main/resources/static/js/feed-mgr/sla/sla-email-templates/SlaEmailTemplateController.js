define(["require", "exports", "angular", "../module-name", "underscore"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($transition$, $mdDialog, $mdToast, $http, SlaEmailTemplateService, StateService, AccessControlService) {
            this.$transition$ = $transition$;
            this.$mdDialog = $mdDialog;
            this.$mdToast = $mdToast;
            this.$http = $http;
            this.SlaEmailTemplateService = SlaEmailTemplateService;
            this.StateService = StateService;
            this.AccessControlService = AccessControlService;
            this.allowEdit = false;
            /**
             * The current template we are editing
             * @type {null}
             */
            this.template = this.SlaEmailTemplateService.template;
            this.emailAddress = '';
            this.queriedTemplate = null;
            this.isDefault = false;
            /**
             * the list of available sla actions the template(s) can be assigned to
             * @type {Array}
             */
            this.availableSlaActions = [];
            this.templateVariables = this.SlaEmailTemplateService.getTemplateVariables();
            this.relatedSlas = [];
            this.validate = function () {
                this.SlaEmailTemplateService.validateTemplate(this.template.subject, this.template.template).then(function (response) {
                    response.data.sendTest = false;
                    this.showTestDialog(response.data);
                });
            };
            this.sendTestEmail = function () {
                this.SlaEmailTemplateService.sendTestEmail(this.emailAddress, this.template.subject, this.template.template).then(function (response) {
                    response.data.sendTest = true;
                    if (response.data.success) {
                        this.$mdToast.show(this.$mdToast.simple()
                            .textContent('Successfully sent the template')
                            .hideDelay(3000));
                    }
                    else {
                        this.$mdToast.show(this.$mdToast.simple()
                            .textContent('Error sending the template ')
                            .hideDelay(3000));
                        this.showTestDialog(response.data);
                    }
                });
            };
            this.saveTemplate = function () {
                this.showDialog("Saving", "Saving template. Please wait...");
                var successFn = function (response) {
                    this.hideDialog();
                    if (response.data) {
                        this.$mdToast.show(this.$mdToast.simple()
                            .textContent('Successfully saved the template')
                            .hideDelay(3000));
                    }
                };
                var errorFn = function (err) {
                    this.hideDialog();
                    this.$mdToast.show(this.$mdToast.simple()
                        .textContent('Error saving template ')
                        .hideDelay(3000));
                };
                this.SlaEmailTemplateService.save(this.template).then(successFn, errorFn);
            };
            this.exampleTemplate = function () {
                this.template.subject = 'SLA Violation for $sla.name';
                this.template.template = '<html>\n<body> \n' +
                    '\t<table>\n' +
                    '\t\t<tr>\n' +
                    '\t\t\t<td align="center" style="background-color:rgb(43,108,154);">\n' +
                    '\t\t\t\t<img src="https://kylo.io/assets/Kylo-Logo-REV.png" height="50%" width="50%">\n' +
                    '\t\t\t</td>\n' +
                    '\t\t</tr>\n' +
                    '\t\t<tr>\n' +
                    '\t\t\t<td>\n' +
                    '\t\t\t\t<table>\n' +
                    '\t\t\t\t\t<tr>\n' +
                    '\t\t\t\t\t\t<td>$sla.name</td>\n' +
                    '\t\t\t\t\t </tr>\n' +
                    '\t\t\t\t\t<tr>\n' +
                    '\t\t\t\t\t\t<td>$sla.description</td>\n' +
                    '\t\t\t\t\t</tr>\n' +
                    '\t\t\t\t\t<tr>\n' +
                    '\t\t\t\t\t\t<td colspan="2">\n' +
                    '\t\t\t\t\t\t\t<h3>Assessment Description</h3>\n' +
                    '\t\t\t\t\t\t</td>\n' +
                    '\t\t\t\t\t</tr>\n' +
                    '\t\t\t\t\t<tr>\n' +
                    '\t\t\t\t\t\t<td colspan="2" style="white-space:pre-wrap;">$assessmentDescription</td>\n' +
                    '\t\t\t\t\t</tr>\n' +
                    '\t\t\t\t</table>\n' +
                    '\t\t\t</td>\n' +
                    '\t\t</tr>\n' +
                    '\t</table>\n' +
                    '</body>\n</html>';
                '</html>';
            };
            this.getAvailableActionItems = function () {
                this.SlaEmailTemplateService.getAvailableActionItems().then(function (response) {
                    this.availableSlaActions = response;
                });
            };
            this.navigateToSla = function (slaId) {
                this.StateService.FeedManager().Sla().navigateToServiceLevelAgreement(slaId);
            };
            this.getRelatedSlas = function () {
                this.relatedSlas = [];
                if (this.template != null && angular.isDefined(this.template) && angular.isDefined(this.template.id)) {
                    this.SlaEmailTemplateService.getRelatedSlas(this.template.id).then(function (response) {
                        _.each(response.data, function (sla) {
                            this.relatedSlas.push(sla);
                            this.template.enabled = true;
                        });
                    });
                }
            };
            this.showTestDialog = function (resolvedTemplate) {
                this.$mdDialog.show({
                    controller: 'VelocityTemplateTestController',
                    templateUrl: 'js/feed-mgr/sla/sla-email-templates/test-velocity-dialog.html',
                    parent: angular.element(document.body),
                    clickOutsideToClose: true,
                    fullscreen: true,
                    locals: {
                        resolvedTemplate: resolvedTemplate,
                        emailAddress: this.emailAddress
                    }
                })
                    .then(function (answer) {
                    //do something with result
                }, function () {
                    //cancelled the dialog
                });
            };
            this.showDialog = function (title, message) {
                this.$mdDialog.show(this.$mdDialog.alert()
                    .parent(angular.element(document.body))
                    .clickOutsideToClose(false)
                    .title(title)
                    .textContent(message)
                    .ariaLabel(title));
            };
            this.hideDialog = function () {
                this.$mdDialog.hide();
            };
            this.templateId = this.$transition$.params().emailTemplateId;
            if (angular.isDefined(this.templateId) && this.templateId != null && (this.template == null || angular.isUndefined(this.template))) {
                this.queriedTemplate = null;
                this.SlaEmailTemplateService.getExistingTemplates().then(function () {
                    this.template = SlaEmailTemplateService.getTemplate(this.templateId);
                    if (angular.isUndefined(this.template)) {
                        ///WARN UNABLE TO FNID TEMPLATE
                        this.showDialog("Unable to find template", "Unable to find the template for " + this.templateId);
                    }
                    else {
                        this.queriedTemplate = angular.copy(this.template);
                        this.isDefault = this.queriedTemplate.default;
                        this.getRelatedSlas();
                    }
                });
            }
            else if ((this.template != null || angular.isDefined(this.template))) {
                this.queriedTemplate = angular.copy(this.template);
                this.isDefault = this.queriedTemplate.default;
            }
            else {
                //redirect back to email template list page
                StateService.FeedManager().Sla().navigateToEmailTemplates();
            }
            this.getAvailableActionItems();
            this.getRelatedSlas();
            // Fetch the allowed actions
            AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                this.allowEdit = AccessControlService.hasAction(AccessControlService.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE, actionSet.actions);
            });
        }
        return controller;
    }());
    exports.controller = controller;
    var testDialogController = /** @class */ (function () {
        function testDialogController($scope, $sce, $mdDialog, resolvedTemplate, emailAddress) {
            this.$scope = $scope;
            this.$sce = $sce;
            this.$mdDialog = $mdDialog;
            this.resolvedTemplate = resolvedTemplate;
            this.emailAddress = emailAddress;
            this.trustAsHtml = function (string) {
                return this.$sce.trustAsHtml(string);
            };
            $scope.resolvedTemplateSubject = $sce.trustAsHtml(resolvedTemplate.subject);
            $scope.resolvedTemplateBody = $sce.trustAsHtml(resolvedTemplate.body);
            $scope.resolvedTemplate = resolvedTemplate;
            $scope.emailAddress = emailAddress;
            $scope.hide = function () {
                $mdDialog.hide();
            };
            $scope.cancel = function () {
                $mdDialog.cancel();
            };
        }
        return testDialogController;
    }());
    exports.testDialogController = testDialogController;
    angular.module(module_name_1.moduleName).controller('VelocityTemplateTestController', ["$scope", "$sce", "$mdDialog", "resolvedTemplate", testDialogController]);
    angular.module(module_name_1.moduleName).controller('SlaEmailTemplateController', ['$transition$', '$mdDialog', '$mdToast', '$http',
        'SlaEmailTemplateService', 'StateService', 'AccessControlService',
        controller]);
});
//# sourceMappingURL=SlaEmailTemplateController.js.map