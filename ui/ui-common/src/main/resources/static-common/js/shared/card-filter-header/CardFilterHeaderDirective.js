/*-
 * #%L
 * thinkbig-ui-common
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
angular.module(COMMON_APP_MODULE_NAME).directive("tbaCardFilterHeader", function()  {
    return {
        scope: {},
        bindToController: {
            cardTitle:'=',
            viewType:'=',
            filterModel:'=',
            filterModelOptions: '=?',
            sortOptions:'=',
            pageName:'@',
            onSelectedOption: '&',
            additionalOptions: '=?',
            onSelectedAdditionalOption: "&?",
            onMenuOpen: '&?'
        },
        controllerAs:'$cardFilterHeader',
        templateUrl:'js/shared/card-filter-header/card-filter-header-template.html',
        compile: function() {
            return function postCompile(scope, element, attr) {
                element.parents('.md-toolbar-tools:first').addClass('card-filter-header')
            };
        },
        link: function ($scope, $element, $attributes, ctrl, transcludeFn) {
        },
        controller: function($scope, $element, TableOptionsService, PaginationDataService){
            var self = this;
            self.filterModelOptions = self.filterModelOptions || {};

            /**
             * Called when a user Clicks on a table Option
             * @param option
             */
            this.selectedOption = function(option) {
                if(option.type == 'sort') {
                    var currentSort = TableOptionsService.toggleSort(self.pageName, option);
                     if(self.onSelectedOption){
                        self.onSelectedOption()(option);
                    }
                }
            }

            this.selectedAdditionalOption = function (option) {
                if (self.onSelectedAdditionalOption) {
                    self.onSelectedAdditionalOption()(option);
                }
            }

            /**
             *
             * @param options {sortOptions:self.sortOptions,additionalOptions:self.additionalOptions}
             */
            this.menuOpen = function (options) {
                if (self.onMenuOpen) {
                    self.onMenuOpen()(options);
                }
            }
        }
    };
});
