import * as $ from 'jquery';
import * as _ from 'underscore';
import AccessConstants from "../constants/AccessConstants";
import "pascalprecht.translate";
import {TransitionService} from "@uirouter/core"; 
import AccessControlService from "../services/AccessControlService";
import StateService from "../services/StateService";
import {AccordionMenuService} from "../common/accordion-menu/AccordionMenuService";
import AngularModuleExtensionService from '../services/AngularModuleExtensionService';
import { Component, ElementRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { TdDialogService } from '@covalent/core/dialogs';
import SideNavService from '../services/SideNavService';

@Component({
    selector: "kylo-side-nav",
    templateUrl: "js/side-nav/side-nav.html",
    styles: [`
        div.mat-sidenav.collapsed {
            max-width: 50px !important;
            min-width: 50px !important;
            width: 50px !important;
        }
    `]
})
export class SideNavComponent {

    /**
     * A map with the moduleName
     * @type {{}}
     */
    menuMap: any = {};
    
    MENU_KEY: any = {"OPS_MGR":"OPS_MGR","FEED_MGR":"FEED_MGR","ADMIN":"ADMIN","MKTPLC":"MKTPLC"}
    extensionsMenus: any = {};  
    /**
     * a pointer to the highlighted menu item
     * @type {null}
     */
    currentMenuLink: any = null;            
    /**
     * A map with the sref,parent menu toggle text
     * this is used to determine what Accordion group should be open given the current state
     * @type {{}}
     */
    menuStateToMenuToggleMap: any = {};
    /**
     * Map of the state (sref) to the menu item
     * @type {{}}
     */
    menuStateMap: any = {};
    /**
     * Array of the top level accordion toggle menu items (i.e. Feed Manager, Operations, Admin)
     * @type {Array}
     */
    toggleSections: any = [];
    /**
     * the <accordion-menu> html $element
     * @type {null}
     */
    accordionElement: any = null;

    menu: any = [];
    sideNavService: any;
    selectedMenuItem: null;
    collapsed: boolean = false;
    adminTitle: string;
    feedManagerTitle: string;
    opsManagerTitle: string;
    menuTitle: string = '';

    ngOnInit() {

        this.sideNavService = this.SideNavService;
        /**
         * The selected menu item
         * @type {null}
         */
        this.selectedMenuItem = null;
        /**
         * Function to call when selecting an item on the left nav
         * @type {selectMenuItem}
         */
        this.collapsed = false;
        this.adminTitle = this.translate.instant('views.main.adminTitle');
        this.feedManagerTitle = this.translate.instant('views.main.feedManagerTitle');
        this.opsManagerTitle = this.translate.instant('views.main.opsManagerTitle');

        this.AngularModuleExtensionService.onInitialized(this.onAngularExtensionsInitialized.bind(this));

        if(this.AngularModuleExtensionService.isInitialized()){
            this.buildSideNavMenu();
        }

        this.transitions.onSuccess({},(transition: any)=>{
            var currentState = transition.to();
            var parentMenu = this.menuStateToMenuToggleMap[currentState.name];
            var menuLink = this.menuStateMap[currentState.name];
            if(menuLink != undefined ){
                if(this.currentMenuLink != null && this.currentMenuLink != menuLink) {
                    this.currentMenuLink.selected = false;
                }
                this.currentMenuLink = menuLink;
                this.currentMenuLink.selected = true;
            }

            if(this.accordionElement == null){
                this.accordionElement = $(this.element.nativeElement).find('accordion-menu');
            }
            if(parentMenu == undefined && currentState != undefined && currentState.data != undefined) {
                //attempt to locate the menu based upon the moduleName defined on the state
                var moduleName = currentState.data.module;
                if(moduleName != undefined) {
                    var menuToggle = this.menuToggleItemForModuleName(moduleName);
                    if(menuToggle != null){
                        parentMenu = menuToggle;
                    }
                }
            }
            
            if(parentMenu != undefined && this.accordionElement != null && this.accordionElement != undefined) {
                if(!parentMenu.expanded) {
                    this.AccordionMenuService.openToggleItem(parentMenu, this.accordionElement, false, this.toggleSections);
                }
            }
        });   

    }

    constructor(private transitions: TransitionService,
                private dialog: TdDialogService,
                private SideNavService: SideNavService,
                private AccessControlService: AccessControlService,
                private StateService: StateService,
                private AccordionMenuService: AccordionMenuService,
                private AngularModuleExtensionService: AngularModuleExtensionService,
                private translate: TranslateService,
                private element: ElementRef) {}

    updateMenuText() {
        var toggleItems = _.filter(this.menu,(item: any)=>{
        return item.type == 'toggle';
        });
        _.each(toggleItems,(item: any)=>{
            if(item.origText == undefined) {
                item.origText = item.text;
            }
            item.collapsed = this.collapsed;
            if(this.collapsed){
                item.text = item.narrowText;

            }
            else {
                item.text = item.origText;
            }
        })
    }

    collapse() {
        this.collapsed = true;
        $('div.mat-sidenav').css('overflow','hidden')
        $('div.mat-sidenav > .sidenav-content').css('overflow','hidden')
        $('div.mat-sidenav').addClass('collapsed');
        this.updateMenuText();
    }

    expand() {
        this.collapsed = false;
        $('div.mat-sidenav').css('overflow','auto')
        $('div.mat-sidenav > .sidenav-content').css('overflow','auto')
        $('div.mat-sidenav').removeClass('collapsed');
        this.updateMenuText();
    }

    buildFeedManagerMenu(): any {
        let links: any[] = [];
        links.push({sref: "feeds",type:'link', icon: "linear_scale", 
                    text: this.translate.instant('views.main.feeds'), 
                    permission: AccessConstants.UI_STATES.FEEDS.permissions});
        links.push({sref: "categories",type:'link', icon: "folder_special", text: this.translate.instant('views.main.categories'), permission: AccessConstants.UI_STATES.CATEGORIES.permissions});
        // links.push({sref: "explorer", type: "link", icon: "find_in_page", text: "Explorer"});
        links.push({sref: "service-level-agreements",type:'link', icon: "beenhere", text: this.translate.instant('views.main.sla'), permission: AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.permissions});
        links.push({sref: "visual-query",type:'link', icon: "transform", text:this.translate.instant('views.main.visual-query'), fullscreen: true, permission:AccessConstants.UI_STATES.VISUAL_QUERY.permissions});
        links.push({sref: "catalog",type:'link', icon: "grid_on", text: this.translate.instant('views.main.tables'), permission: AccessConstants.UI_STATES.TABLES.permissions});
        this.addExtensionLinks(this.MENU_KEY.FEED_MGR, links);
        var menu = ({type:'toggle', 
                text: this.translate.instant('views.main.feed-manage'),
                narrowText:this.translate.instant('views.main.feed-manage-narrow'),
                expanded:true,
                elementId:'toggle_feed_manager',
                links:links});
        menu.links = links;
        this.menuMap[this.MENU_KEY.FEED_MGR] = menu;
        return menu;
    }

    /**
     * Builds additional menu items
     * @param rootMenu
     */
    buildExtensionsMenu(rootMenu: any): any {
        var additionalKeys = _.keys(this.extensionsMenus);
        additionalKeys = _.filter(additionalKeys, (key: any)=> {return this.MENU_KEY[key] == undefined });
        if(additionalKeys.length >0){
            _.each(additionalKeys,(key: any)=>{
                var menu = this.extensionsMenus[key];
                //ensure this is a toggle type
                menu.type = 'toggle';
                _.each(menu.links,(link: any)=> {
                    //ensure we set this type to be a child
                    link.type = 'link';
                });
                this.menuMap[key] = menu;
                rootMenu.push(menu);
            })
        }
    }

    addExtensionLinks(menuName: any, links: any): any {
        var extensionLinks = this.extensionsMenus[menuName];
        if(extensionLinks && extensionLinks.links){
            _.each(extensionLinks.links,(link: any)=>{
                //ensure we set this type to be a child
                link.type = 'link';
                links.push(link);
            })
        }
    }

    closeSideNavList() {
        this.sideNavService('left').close();
    }

    accessDeniedDialog(title: any,content: any):any {
        this.dialog.openAlert({
            message : content,
            title : title,
            ariaLabel : this.translate.instant('views.main.access-denied'),
            closeButton : this.translate.instant('views.main.got-it'),
            disableClose : false
        });
    }

    joinArray(mainArr: any, joiningArr: any): any {
                _.each(joiningArr,(item: any)=>{
                    mainArr.push(item);
                })
        return mainArr;
    }

    buildMenuStateMap(menu: any): any {
        this.menuStateToMenuToggleMap = {};
        this.menuStateMap = {};
        _.each(menu,(menuToggle: any)=> {
            _.each(menuToggle.links,(item: any)=>{
                this.menuStateToMenuToggleMap[item.sref] = menuToggle;
                this.menuStateMap[item.sref] = item;
            });
        });
    }

    menuToggleItemForModuleName = (moduleName: any): any => {
        if(moduleName.indexOf('opsmgr') >=0){
            return this.menuMap[this.MENU_KEY.OPS_MGR];
        }
        else if(moduleName.indexOf('feedmgr') >=0 &&
                moduleName != 'kylo.feedmgr.datasources' && 
                moduleName != "kylo.feedmgr.domain-types" && 
                moduleName != 'kylo.feedmgr.templates'){
            return this.menuMap[this.MENU_KEY.FEED_MGR];
        }
        else if(moduleName.indexOf('auth') >=0 || 
                moduleName == 'kylo.feedmgr.datasources' ||
                moduleName == "kylo.feedmgr.domain-types" || 
                moduleName == 'kylo.feedmgr.templates'){
            return this.menuMap[this.MENU_KEY.ADMIN];
        }
        else {
            return null;
        }
    } 

    buildOpsManagerMenu(): any {
                    
        let links: any[] = [];
        links.push({sref: "dashboard",type:'link', icon: "dashboard", text: this.translate.instant('views.main.dashboard'), defaultActive: false, permission: AccessConstants.UI_STATES.DASHBOARD.permissions});
        links.push({sref: "service-health",type:'link', icon: "vector_triangle", text: this.translate.instant('views.main.services'), defaultActive: false, permission: AccessConstants.UI_STATES.SERVICE_HEALTH.permissions});
        links.push({sref: "jobs",type:'link', icon: "settings", text: this.translate.instant('views.main.jobs'), defaultActive: false, permission: AccessConstants.UI_STATES.JOBS.permissions});
        links.push({sref: "alerts", icon: "notifications", text: this.translate.instant('views.main.alerts'), defaultActive: false, permission: AccessConstants.UI_STATES.ALERTS.permissions});
        links.push({sref: "service-level-assessments",type:'link', icon: "work", text: this.translate.instant('views.main.sla-assessments'), defaultActive: false, permission: AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENTS.permissions});
        links.push({sref: "scheduler",type:'link', icon: "today", text: this.translate.instant('views.main.sla-schedule'), defaultActive: false, permission: AccessConstants.UI_STATES.SCHEDULER.permissions});
        links.push({sref: "charts",type:'link', icon: "insert_chart", text: this.translate.instant('views.main.charts'), defaultActive: false, permission: AccessConstants.UI_STATES.CHARTS.permissions});
        this.addExtensionLinks(this.MENU_KEY.OPS_MGR, links);
        var menu = ({type:'toggle', 
                    text: this.translate.instant('views.main.operations'),
                    narrowText:this.translate.instant('views.main.operations-narrow'),
                    expanded:false,
                    links:links});
        menu.links = links;
        this.menuMap[this.MENU_KEY.OPS_MGR] = menu;
        return menu;
    }

    /**
     * Build the Admin Menu
     * @param allowed
     */
    buildAdminMenu(): any{
        
        let links: any[] = [];
        links.push({sref: "datasources", type: "link", icon: "storage", text: this.translate.instant('views.main.data-sources'), defaultActive: false, permission: AccessConstants.DATASOURCE_ACCESS});
        links.push({sref: "domain-types", type: "link", icon: "local_offer", text: this.translate.instant('views.main.domain-types'), defaultActive: false, permission: AccessConstants.FEEDS_ADMIN});
        links.push({sref: "business-metadata", type:'link', icon: "business", text: this.translate.instant('views.main.properties'), defaultActive: false, permission: AccessConstants.CATEGORIES_ADMIN});
        links.push({sref: "registered-templates",type:'link', icon: "layers", text: this.translate.instant('views.main.templates'), defaultActive: false, permission: AccessConstants.TEMPLATES_ACCESS});
        links.push({sref: "users",type:'link', icon: "account_box", text: this.translate.instant('views.main.users'), defaultActive: false, permission: AccessConstants.USERS_ACCESS});
        links.push({sref: "groups",type:'link', icon: "group", text: this.translate.instant('views.main.groups'), defaultActive: false, permission: AccessConstants.GROUP_ACCESS});
        links.push({sref: "sla-email-templates",type:'link', icon: "email", text: this.translate.instant('views.main.sla-email'), defaultActive: false, permission: AccessConstants.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE});
        this.addExtensionLinks(this.MENU_KEY.ADMIN, links);
        let menu = ({type:'toggle', 
                    text: this.translate.instant('views.main.admin'),
                    narrowText:this.translate.instant('views.main.admin-narrow'),
                    expanded:false,
                    links:links,
                });
        menu.links = links;
        this.menuMap[this.MENU_KEY.ADMIN] = menu;
        return menu
    }

    /**
     * Build the Marketplace Menu
     * @param allowed
     */
    buildMarketplaceMenu(){

        let links: any[] = [];
        links.push({sref: "marketplace", type: "link", icon: "local_grocery_store", text: this.translate.instant('views.main.marketplace'), defaultActive: false, permission: AccessConstants.DATASOURCE_ACCESS});
        this.addExtensionLinks(this.MENU_KEY.MKTPLC, links);
        let menu = ({type:'toggle',
                    text: this.translate.instant('views.main.marketplace'),
                    narrowText:this.translate.instant('views.main.marketplace-narrow'),
                    expanded:false,
                    links:links,
                });
        menu.links = links;
        this.menuMap[this.MENU_KEY.MKTPLC] = menu;
        return menu
    }

    buildSideNavMenu():any {
        var menu = [];

        //identify any additional menu items
        this.extensionsMenus = this.AngularModuleExtensionService.getNavigationMenu();

        menu.push(this.buildOpsManagerMenu());
        menu.push(this.buildFeedManagerMenu());
        menu.push(this.buildAdminMenu());
        // menu.push(this.buildMarketplaceMenu());

        this.buildExtensionsMenu(menu);
        this.buildMenuStateMap(menu);

        this.toggleSections = _.filter(menu,(item: any)=>{
        return item.type == 'toggle';
        });

        //clear the binding
        this.menu.length = 0;
        //readd in the values
        let menuArray: any = [];
        _.each(menu,(item: any)=>{
            menuArray.push(item);
        })
        this.menu = menuArray;
    };

   onAngularExtensionsInitialized():any {
        this.buildSideNavMenu();
    }

    selectMenuItem(menuItem: any) {
        this.selectedMenuItem = menuItem;
        this.closeSideNavList();
    }
    
    expandCollapseSideNavList() {
        if(this.collapsed){
           this.expand();
        }
        else {
            this.collapse();
        }
    }

}

