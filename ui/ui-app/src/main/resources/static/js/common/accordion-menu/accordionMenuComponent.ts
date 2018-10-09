import * as _ from "underscore";
import { Component, Input, ElementRef, SimpleChanges, OnChanges, DoCheck } from "@angular/core";
import { AccordionMenuService } from "./AccordionMenuService";
import { ObjectUtils } from "../utils/object-utils";


@Component({
    selector:'accordion-menu',
    template: `
        <mat-list class="side-menu" style="padding-top:0;">
            <mat-list-item *ngFor="let section of menu" style="height:auto;" flex layout-fill>
                <div *ngIf="!section.hidden" style="width:100%">
                    <menu-link [section]="section" [collapsed]="collapsed" (isCollapsedEmitter)="isCollapsed()" (autoFocusContentEmitter)="focusSection()"
                        *ngIf="section.type == 'link'" style="width:100%"></menu-link>
                    <menu-toggle [section]="section" [isCollapsed]="collapsed" (openToggleItemEmitter)="openToggleItem($event)"
                        *ngIf="section.type == 'toggle' " style="width:100%"></menu-toggle>
                    <mat-divider></mat-divider>
                </div>
            </mat-list-item>
        </mat-list>    
    `,
    styleUrls: ['js/common/accordion-menu/accordion.css']
})
export class AccordianMenuComponent implements OnChanges {

    @Input() menu: any;
    @Input() collapsed: any;
    @Input() allowMultipleOpen: boolean;
   
    openedSection: any = null;
    autoFocusContent: boolean = false;
    toggleSections: any = [];
    
    constructor(private accordionMenuService: AccordionMenuService,
                private element: ElementRef) {}

    ngOnChanges(changes: SimpleChanges) {
        if(changes.menu && changes.menu.currentValue && !changes.menu.firstChange) {
            this.setToggleSections();
        }
    }

    ngOnInit() {
        this.allowMultipleOpen = ObjectUtils.isDefined(this.allowMultipleOpen) ? this.allowMultipleOpen : false;
        this.setToggleSections();
    }

    setToggleSections() {
        this.toggleSections = _.filter(this.menu,(item: any)=>{
            return item.type == 'toggle';
        });

        _.each(this.toggleSections,(section: any)=>{
            if(section.expanded == true) {
                section.expandIcon = 'expand_less';
            }
            else {
                section.expandIcon = 'expand_more';
            }
            if(section.elementId == undefined) {
                section.elementId = section.text.toLowerCase().split(' ').join('_');
            }
        })
    }

    focusSection(){
        this.autoFocusContent = true;
    };

    /**
     * is the menu collapsed
     * @returns {boolean}
     */
    isCollapsed(){
        return this.collapsed == true;
    }

    /**
     * open the menu item
     * @param section
     */
    openToggleItem = (section: any) => {
        this.accordionMenuService.openToggleItem(section,$(this.element.nativeElement),this.allowMultipleOpen,this.toggleSections);
    }
}