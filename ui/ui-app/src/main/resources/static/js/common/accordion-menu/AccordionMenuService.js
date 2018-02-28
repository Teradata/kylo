define(["require", "exports", "angular", "../module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AccordionMenuService = /** @class */ (function () {
        function AccordionMenuService($timeout) {
            this.$timeout = $timeout;
            /**
             * open an accordion menu item
             * @param section the toggle section to open
             * @param $accordionElement the accordion element
             * @param allowMultipleOpen true/false
             * @param toggleSections the array of all toggleSections
             */
            this.openToggleItem = function (section, $accordionElement, allowMultipleOpen, toggleSections) {
                //disable scroll when opening
                $accordionElement.parent().css('overflow-y', 'hidden');
                if (!allowMultipleOpen) {
                    angular.forEach(toggleSections, function (openSection) {
                        openSection.expanded = false;
                        openSection.expandIcon = 'expand_more';
                    });
                }
                section.expanded = true;
                section.expandIcon = 'expand_less';
                $timeout(function () {
                    $accordionElement.parent().css('overflow-y', 'auto');
                }, 500);
            };
        }
        return AccordionMenuService;
    }());
    exports.AccordionMenuService = AccordionMenuService;
    angular.module(module_name_1.moduleName).service('AccordionMenuService', ['$timeout', AccordionMenuService]);
});
//# sourceMappingURL=AccordionMenuService.js.map