(function () {

    var controller = function($scope,$stateParams, $http,$mdToast,RegisterTemplateService, StateService) {

        var self = this;

        /**
         * Reference to the RegisteredTemplate Kylo id passed when editing a template
         * @type {null|*}
         */
        this.registeredTemplateId = $stateParams.registeredTemplateId || null;

        /**
         * Reference to the NifiTemplate Id. Used if kylo id above is not present
         * @type {null|*}
         */
        this.nifiTemplateId = $stateParams.nifiTemplateId || null;

        /**
         * The model being edited/created
         */
        this.model = RegisterTemplateService.model;

        self.cancelStepper = function() {
            //or just reset the url
            RegisterTemplateService.resetModel();
            self.stepperUrl = null;
            StateService.navigateToRegisteredTemplates();
        }




        function init(){
            self.loading = true;
                //Wait for the properties to come back before allowing hte user to go to the next step
                RegisterTemplateService.loadTemplateWithProperties(self.registeredTemplateId, self.nifiTemplateId).then(function(response) {

                    self.loading = false;
                });
        }
        init();

    }

    angular.module(MODULE_FEED_MGR).controller('RegisterTemplateController',controller);



}());