define(['angular',"feed-mgr/templates/module-name"], function (angular,moduleName) {

    var controller = function($scope,$transition$, $http,$mdToast,RegisterTemplateService, StateService) {

        var self = this;

        console.log("$transition$",$transition$)
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

        console.log('self', self,'trans',$transition$.params())

        /**
         * The model being edited/created
         */
        this.model = RegisterTemplateService.model;

        self.cancelStepper = function() {
            //or just reset the url
            RegisterTemplateService.resetModel();
            self.stepperUrl = null;
            StateService.FeedManager().Template().navigateToRegisteredTemplates();
        }


        function init(){
            self.loading = true;
                //Wait for the properties to come back before allowing the user to go to the next step
                RegisterTemplateService.loadTemplateWithProperties(self.registeredTemplateId, self.nifiTemplateId).then(function(response) {

                    self.loading = false;
                    RegisterTemplateService.warnInvalidProcessorNames();
                });
        }
        init();

    }

    angular.module(moduleName).controller('RegisterTemplateController',["$scope","$transition$","$http","$mdToast","RegisterTemplateService","StateService",controller]);



});
