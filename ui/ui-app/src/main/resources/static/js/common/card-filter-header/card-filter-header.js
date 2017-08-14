define(['angular','common/module-name'], function (angular,moduleName) {
    angular.module(moduleName).directive("tbaCardFilterHeader", function () {
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
                return function postCompile(scope, element, attr) {
                    element.parents('.md-toolbar-tools:first').addClass('card-filter-header')
                };
            },
            link: function ($scope, $element, $attributes, ctrl, transcludeFn) {
            },
            controller: ['$scope', '$element', 'TableOptionsService',function ($scope, $element, TableOptionsService) {
                var self = this;
                self.filterModelOptions = self.filterModelOptions || {};

                self.renderFilter = angular.isUndefined(self.renderFilter) ? true : self.renderFilter;

                self.renderHelp = angular.isDefined(self.onShowFilterHelp);

                self.customFilterTemplate = angular.isUndefined(self.customFilterTemplate) ? '' : self.customFilterTemplate;

                /**
                 * Called when a user Clicks on a table Option
                 * @param option
                 */
                this.selectedOption = function (option) {
                    if (option.type == 'sort') {
                        var currentSort = TableOptionsService.toggleSort(self.pageName, option);
                        if (self.onSelectedOption) {
                            self.onSelectedOption()(option);
                        }
                    }
                }

                this.selectedAdditionalOption = function (option) {
                    if (self.onSelectedAdditionalOption) {
                        self.onSelectedAdditionalOption()(option);
                    }
                }

                this.showFilterHelpPanel = function (ev) {
                    if (self.onShowFilterHelp) {
                        self.onShowFilterHelp()(ev);
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
            }]
        };
    });
});
