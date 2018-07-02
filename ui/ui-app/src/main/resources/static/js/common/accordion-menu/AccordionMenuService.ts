import * as angular from "angular";
import {moduleName} from "../module-name";

export class AccordionMenuService{
    
    static readonly $inject =['$timeout'];

    constructor(private $timeout: angular.ITimeoutService){}
    
    /**
     * open an accordion menu item
     * @param section the toggle section to open
     * @param $accordionElement the accordion element
     * @param allowMultipleOpen true/false
     * @param toggleSections the array of all toggleSections
     */
    
     openToggleItem(section: any, $accordionElement: any, allowMultipleOpen: any, toggleSections: any) {

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

        this.$timeout(()=>{
            $accordionElement.parent().css('overflow-y','auto');
        },500)
    }
}

angular.module(moduleName).service('AccordionMenuService',AccordionMenuService);

