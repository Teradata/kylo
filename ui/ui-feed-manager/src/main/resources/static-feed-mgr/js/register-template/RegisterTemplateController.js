/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
