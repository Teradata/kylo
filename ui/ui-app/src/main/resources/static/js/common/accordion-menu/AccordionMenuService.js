define(['angular','common/module-name'], function (angular,moduleName) {

    angular.module(moduleName).service('AccordionMenuService',['$timeout',function ($timeout) {

        /**
         * open an accordion menu item
         * @param section the toggle section to open
         * @param $accordionElement the accordion element
         * @param allowMultipleOpen true/false
         * @param toggleSections the array of all toggleSections
         */
        this.openToggleItem = function (section, $accordionElement, allowMultipleOpen, toggleSections) {

            //disable scroll when opening
            $accordionElement.parent().css('overflow-y','hidden');

            if (!allowMultipleOpen) {
                angular.forEach(toggleSections, function (openSection) {
                    openSection.expanded = false;
                    openSection.expandIcon = 'expand_more';
                });
            }
            section.expanded = true;
            section.expandIcon = 'expand_less';

            $timeout(function(){
                $accordionElement.parent().css('overflow-y','auto');
            },500)


        }
    }]);



});