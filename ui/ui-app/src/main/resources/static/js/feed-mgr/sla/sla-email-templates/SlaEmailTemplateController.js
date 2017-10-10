define(['angular',"feed-mgr/sla/module-name"], function (angular,moduleName) {

    var controller = function ($transition$, $mdDialog, $mdToast, $http, SlaEmailTemplateService) {
        var self = this;

        /**
         * The current template we are editing
         * @type {null}
         */
        this.template = SlaEmailTemplateService.template;

        this.queriedTemplate = null;
        this.isDefault = false;

        var templateId = $transition$.params().emailTemplateId;
        if(angular.isDefined(templateId) && (self.template == null || angular.isUndefined(self.template))){
            self.queriedTemplate = null;
            SlaEmailTemplateService.getExistingTemplates().then(function() {
                self.template = SlaEmailTemplateService.getTemplate(templateId);
                if(angular.isUndefined(self.template)) {
                    ///WARN UNABLE TO FNID TEMPLATE
                    showDialog("Unable to find template","Unable to find the template for "+templateId);
                }
                else {
                    self.queriedTemplate = angular.copy(self.template);
                    self.isDefault = self.queriedTemplate.default
                    getRelatedSlas();
                }
            })
        }
        else if((self.template != null || angular.isDefined(self.template))){
            self.queriedTemplate = angular.copy(self.template);
            self.isDefault = self.queriedTemplate.default
        }

        /**
         * the list of available sla actions the template(s) can be assigned to
         * @type {Array}
         */
        this.availableSlaActions = [];

        this.templateVariables= SlaEmailTemplateService.getTemplateVariables();

        self.relatedSlas = [];


        this.validate = function () {

            SlaEmailTemplateService.validateTemplate(self.template.subject,self.template.template).then(function (response) {
                showTestDialog(response.data);
            });

        }



        this.saveTemplate = function () {
            showDialog("Saving", "Saving template. Please wait...");

           var successFn = function (response) {
                hideDialog();
                if (response.data) {
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('Successfully saved the template')
                            .hideDelay(3000)
                    );
                }
            }
            var errorFn = function (err) {
                hideDialog();
                $mdToast.show(
                    $mdToast.simple()
                        .textContent('Error saving template ')
                        .hideDelay(3000)
                );
            }

           SlaEmailTemplateService.save(self.template).then(successFn, errorFn);
        }

        function getAvailableActionItems() {
            SlaEmailTemplateService.getAvailableActionItems().then(function (response) {
                    self.availableSlaActions = response;
            });
        }

        function getRelatedSlas(){
            self.relatedSlas = [];
            if(self.template != null && angular.isDefined(self.template) && angular.isDefined(self.template.id)) {
                SlaEmailTemplateService.getRelatedSlas(self.template.id).then(function(response){
                    _.each(response.data,function(sla){
                        self.relatedSlas.push(sla)
                        self.template.enabled = true;
                    })
                })
            }
        }

        function showTestDialog(resolvedTemplate) {
            $mdDialog.show({
                controller: 'VelocityTemplateTestController',
                templateUrl: 'js/feed-mgr/sla/sla-email-templates/test-velocity-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: true,
                fullscreen: true,
                locals: {
                    resolvedTemplate: resolvedTemplate
                }
            })
                .then(function (answer) {
                    //do something with result
                }, function () {
                    //cancelled the dialog
                });
        };

        function showDialog(title, message) {
            $mdDialog.show(
                $mdDialog.alert()
                    .parent(angular.element(document.body))
                    .clickOutsideToClose(false)
                    .title(title)
                    .textContent(message)
                    .ariaLabel(title)
            );
        }

        function hideDialog() {
            $mdDialog.hide();
        }

        getAvailableActionItems();
        getRelatedSlas();


    };

    /**
     * The Controller used for the Feed Saving Dialog
     */
    var testDialogController = function ($scope,$sce, $mdDialog, resolvedTemplate) {
        var self = this;

        $scope.resolvedTemplateSubject = $sce.trustAsHtml(resolvedTemplate.subject);
        $scope.resolvedTemplateBody = $sce.trustAsHtml(resolvedTemplate.body);

        this.trustAsHtml = function(string) {
            return $sce.trustAsHtml(string);
        };

        $scope.hide = function () {
            $mdDialog.hide();
        };


        $scope.cancel = function () {
            $mdDialog.cancel();
        };

    };

    angular.module(moduleName).controller('VelocityTemplateTestController', ["$scope", "$sce", "$mdDialog", "resolvedTemplate", testDialogController]);

    angular.module(moduleName).controller('SlaEmailTemplateController', ['$transition$', '$mdDialog', '$mdToast', '$http','SlaEmailTemplateService', controller]);

});
