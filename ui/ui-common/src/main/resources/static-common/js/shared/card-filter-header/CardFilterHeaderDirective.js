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
            onSelectedOption:'&'
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
        }
    };
});