import * as _ from "underscore";
import { Injectable } from "@angular/core";

@Injectable()
export class AccordionMenuService{
    
    /**
     * open an accordion menu item
     * @param section the toggle section to open
     * @param $accordionElement the accordion element
     * @param allowMultipleOpen true/false
     * @param toggleSections the array of all toggleSections
     */
    
     openToggleItem(section: any, accordionElement: any, allowMultipleOpen: any, toggleSections: any) {

        //disable scroll when opening
        accordionElement.parent().css('overflow-y','hidden');

        if (!allowMultipleOpen) {
            _.forEach(toggleSections, (openSection: any)=> {
                openSection.expanded = false;
                openSection.expandIcon = 'expand_more';
            });
        }
        section.expanded = true;
        section.expandIcon = 'expand_less';

        setTimeout(()=>{
            accordionElement.parent().css('overflow-y','auto');
        },500)
    }
}
