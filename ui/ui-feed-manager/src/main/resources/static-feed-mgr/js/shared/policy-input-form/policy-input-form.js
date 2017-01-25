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

    /**
     * the rule and options need to have been initialized by the PolicyInputFormService grouping
     *
     */
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                rule: '=',
                theForm: '=',
                feed: '=?',
                mode: '&' //NEW or EDIT
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/shared/policy-input-form/policy-input-form.html',
            controller: "PolicyInputFormController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller = function ($scope, $q, PolicyInputFormService) {

        var self = this;
        self.editChips = {};
        self.editChips.selectedItem = null;
        self.editChips.searchText = null;
        self.validateRequiredChips = function (property) {
            return PolicyInputFormService.validateRequiredChips(self.theForm, property);
        }
        self.queryChipSearch = PolicyInputFormService.queryChipSearch;
        self.transformChip = PolicyInputFormService.transformChip;

    };


    angular.module(MODULE_FEED_MGR).controller('PolicyInputFormController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigPolicyInputForm', directive);

})();
