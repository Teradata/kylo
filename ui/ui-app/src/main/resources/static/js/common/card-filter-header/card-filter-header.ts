import * as angular from "angular";
import {moduleName} from "../module-name";

angular.module(moduleName).directive("tbaCardFilterHeader", ()=> {
    return {
            scope: {},
            bindToController: {
                cardTitle: '=',
                viewType: '=',
                filterModel: '=',
                filterModelOptions: '=?',
                sortOptions: '=',
                pageName: '@',
                onSelectedOption: '&',
                additionalOptions: '=?',
                onSelectedAdditionalOption: "&?",
                onMenuOpen: '&?',
                onShowFilterHelp: '&?',
                renderFilter:'=?',
                cardController:'=?',
                customFilterTemplate:'@?'
            },
            controllerAs: '$cardFilterHeader',
            templateUrl: 'js/common/card-filter-header/card-filter-header-template.html',
            compile: function () {
                return function postCompile(scope: any, element: any, attr: any) {
                    element.parents('.md-toolbar-tools:first').addClass('card-filter-header')
                };
            },
            link: function ($scope: any, $element: any, $attributes: any, ctrl: any, transcludeFn: any) {
            },
            controller: ['$scope', '$element', 'TableOptionsService',function ($scope: any, $element: any, TableOptionsService: any) {
                var self = this;
                self.filterModelOptions = self.filterModelOptions || {};

                self.renderFilter = angular.isUndefined(self.renderFilter) ? true : self.renderFilter;

                self.renderHelp = angular.isDefined(self.onShowFilterHelp);

                self.customFilterTemplate = angular.isUndefined(self.customFilterTemplate) ? '' : self.customFilterTemplate;

                /**
                 * Called when a user Clicks on a table Option
                 * @param option
                 */
                this.selectedOption = function (option: any) {
                    if (option.type == 'sort') {
                        var currentSort = TableOptionsService.toggleSort(self.pageName, option);
                        if (self.onSelectedOption) {
                            self.onSelectedOption()(option);
                        }
                    }
                }

                this.selectedAdditionalOption = function (option: any) {
                    if (self.onSelectedAdditionalOption) {
                        self.onSelectedAdditionalOption()(option);
                    }
                }

                this.showFilterHelpPanel = function (ev: any) {
                    if (self.onShowFilterHelp) {
                        self.onShowFilterHelp()(ev);
                    }
                }

                /**
                 *
                 * @param options {sortOptions:self.sortOptions,additionalOptions:self.additionalOptions}
                 */
                this.menuOpen = function (options: any) {
                    if (self.onMenuOpen) {
                        self.onMenuOpen()(options);
                    }
                }
            }]
        };
    });