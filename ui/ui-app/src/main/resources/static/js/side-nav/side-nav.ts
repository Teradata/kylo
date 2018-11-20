import * as angular from 'angular';
import * as _ from 'underscore';
import {moduleName} from "./module-name";
import AccessConstants from "../constants/AccessConstants";
import "./module";
import "../services/services.module";
import "pascalprecht.translate";
import {Transition, TransitionService} from "@uirouter/core";
import {AccessControlService} from "../services/AccessControlService";
import {SideNavService} from  "../services/SideNavService";
import {StateService} from "../services/StateService";
import AngularModuleExtensionService from "../services/AngularModuleExtensionService";
import {AccordionMenuService} from "../common/accordion-menu/AccordionMenuService";
import {FEED_DEFINITION_STATE_NAME} from "../feed-mgr/model/feed/feed-constants";


export default class SideNav implements ng.IComponentController {

    /**
     * A map with the moduleName
     * @type {{}}
     */
    menuMap: any = {};
    
    MENU_KEY: any = {"OPS_MGR":"OPS_MGR","FEED_MGR":"FEED_MGR","ADMIN":"ADMIN"}
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
    $accordionElement: any = null;

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {

        this.$scope.sideNavService = this.SideNavService;
        /**
         * The menu
         * @type {Array}
         */
        this.$scope.menu = [];
        /**
         * The selected menu item
         * @type {null}
         */
        this.$scope.selectedMenuItem = null;
        /**
         * Function to call when selecting an item on the left nav
         * @type {selectMenuItem}
         */
        this.$scope.selectMenuItem = this.selectMenuItem;
        this.$scope.collapsed = false;
        this.$scope.expandCollapseSideNavList = this.expandCollapseSideNavList;
        this.$scope.adminTitle = this.$filter('translate')('views.main.adminTitle');
        this.$scope.feedManagerTitle = this.$filter('translate')('views.main.feedManagerTitle');
        this.$scope.opsManagerTitle = this.$filter('translate')('views.main.opsManagerTitle');

        this.$scope.menuTitle = '';
        
        this.AngularModuleExtensionService.onInitialized(this.onAngularExtensionsInitialized);

    
        if(this.AngularModuleExtensionService.isInitialized()){
            this.buildSideNavMenu();
        }

        this.$transitions.onSuccess({},(transition: Transition)=>{
        var currentState = transition.to();
        let moduleName = currentState.data ? currentState.data.module : undefined;
        let currentStateName = currentState.name;
        let menuLinkKey = (currentState.data  && currentState.data.menuLink) ? currentState.data.menuLink : this.menuStateMap[currentStateName];
        let menuLink : any = undefined;
        if(menuLinkKey) {
            menuLink = this.menuStateMap[menuLinkKey];
        }
        if(menuLink == undefined){
            currentStateName = (typeof currentState.name === "string" && currentState.name.indexOf(".") !== -1) ? currentState.name.substring(0, currentState.name.indexOf(".")) : currentState.name;
            menuLink = this.menuStateMap[currentStateName];
        }

        var parentMenu = this.menuStateToMenuToggleMap[currentStateName];

        if(menuLink != undefined ){
            if(this.currentMenuLink != null && this.currentMenuLink != menuLink) {
                this.currentMenuLink.selected = false;
            }
            this.currentMenuLink = menuLink;
            this.currentMenuLink.selected = true;
        }

        if(this.$accordionElement == null){
            this.$accordionElement = this.$element.find('accordion-menu');
        }
        if(parentMenu == undefined && currentState != undefined && currentState.data != undefined) {
            //attempt to locate the menu based upon the moduleName defined on the state
             if(moduleName != undefined) {
                var menuToggle = this.menuToggleItemForModuleName(moduleName);
                if(menuToggle != null){
                    parentMenu = menuToggle;
                }
            }
        }
        
        if(parentMenu != undefined && 
            this.$accordionElement != null && 
            this.$accordionElement != undefined){
            if(!parentMenu.expanded){
                this.AccordionMenuService.openToggleItem(parentMenu,
                                                        this.$accordionElement,
                                                        false,
                                                        this.toggleSections);
            }
            }
        });   

    }

    static readonly $inject = ["$mdSidenav", "$mdDialog","$rootScope","$transitions",
                                "$timeout","SideNavService", "AccessControlService", 
                                "StateService","AccordionMenuService", "AngularModuleExtensionService", 
                                "$filter", "$scope", "$element"];
    
    constructor(private $mdSidenav: angular.material.ISidenavService,
                private $mdDialog: angular.material.IDialogService,
                private $rootScope: angular.IScope,
                private $transitions: TransitionService,
                private $timeout: angular.ITimeoutService,
                private SideNavService: SideNavService,
                private AccessControlService: AccessControlService,
                private StateService: StateService,
                private AccordionMenuService: AccordionMenuService,
                private AngularModuleExtensionService: any,
                private $filter: angular.IFilterService,
                private $scope: IScope,
                private $element: JQuery) {}

    updateMenuText = () => {
        var toggleItems = _.filter(this.$scope.menu,(item: any)=>{
        return item.type == 'toggle';
        });
        _.each(toggleItems,(item: any)=>{
            if(item.origText == undefined) {
                item.origText = item.text;
            }
            item.collapsed = this.$scope.collapsed;
            if(this.$scope.collapsed){
                item.text = item.narrowText;

            }
            else {
                item.text = item.origText;
            }
        })
    }

    collapse = () => {
        this.$scope.collapsed = true;
        // angular.element('md-sidenav > md-content >div:first').css('overflow-','hidden')
            angular.element('md-sidenav.site-sidenav').css('overflow','hidden')
            angular.element('md-sidenav.site-sidenav > md-content').css('overflow','hidden')
            angular.element('md-sidenav.site-sidenav').addClass('collapsed');
        this.updateMenuText();
    }

    expand = () => {
        this.$scope.collapsed = false;
        //  angular.element('md-sidenav > md-content >div:first').css('overflow-y','auto')
            angular.element('md-sidenav.site-sidenav').css('overflow','auto')
            angular.element('md-sidenav.site-sidenav > md-content').css('overflow','auto')
            angular.element('md-sidenav.site-sidenav').removeClass('collapsed');
        this.updateMenuText();
    }

    buildFeedManagerMenu: any=()=>{
        let links: any[] = [];
        links.push({sref: "feeds",type:'link', icon: "linear_scale", 
                    text: this.$filter('translate')('views.main.feeds'), 
                    permission: AccessConstants.UI_STATES.FEEDS.permissions});
        links.push({sref: "categories",type:'link', icon: "folder_special", text: this.$filter('translate')('views.main.categories'), permission: AccessConstants.UI_STATES.CATEGORIES.permissions});
        // links.push({sref: "explorer", type: "link", icon: "find_in_page", text: "Explorer"});
        links.push({sref: "sla",type:'link', icon: "beenhere", text: this.$filter('translate')('views.main.sla'), permission: AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.permissions});
        links.push({sref: "visual-query",type:'link', icon: "transform", text:this.$filter('translate')('views.main.visual-query'), fullscreen: true, permission:AccessConstants.UI_STATES.VISUAL_QUERY.permissions});
        links.push({sref: "catalog",type:'link', icon: "grid_on", text: this.$filter('translate')('views.main.tables'), permission: AccessConstants.UI_STATES.CATALOG.permissions});
        this.addExtensionLinks(this.MENU_KEY.FEED_MGR, links);
        var menu = ({type:'toggle', 
                text: this.$filter('translate')('views.main.feed-manage'),
                narrowText:this.$filter('translate')('views.main.feed-manage-narrow'),
                expanded:true,
                elementId:'toggle_feed_manager',
                links:links});
        menu.links = links;
        this.menuMap[this.MENU_KEY.FEED_MGR] = menu;
        return menu;
    }

    isSideNavHidden=()=> { //To Be Revised // SMB
        return (this.$scope.$mdMedia('gt-md') && this.SideNavService.isLockOpen);
    };

    toggleSideNavList=()=> {
        this.$scope.$q.when(true).then( ()=> {
            this.$mdSidenav('left').toggle();
        });
    }
    /**
     * Builds additional menu items
     * @param rootMenu
     */
    buildExtensionsMenu: any=(rootMenu: any)=>{
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

    addExtensionLinks: any =(menuName: any, links: any)=>{
        var extensionLinks = this.extensionsMenus[menuName];
        if(extensionLinks && extensionLinks.links){
            _.each(extensionLinks.links,(link: any)=>{
                //ensure we set this type to be a child
                link.type = 'link';
                links.push(link);
            })
        }
    }

    closeSideNavList=()=>
    {
        this.$mdSidenav('left').close();
    }

    accessDeniedDialog: any =(title: any,content: any)=>{
        this.$mdDialog.show(
            this.$mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title(title)
                            .textContent(content)
                            .ariaLabel(this.$filter('translate')('views.main.access-denied'))
                            .ok(this.$filter('translate')('views.main.got-it'))
        );
    }

    joinArray = (mainArr: any, joiningArr: any): any => {
                _.each(joiningArr,(item: any)=>{
                    mainArr.push(item);
                })
        return mainArr;
    }

    buildMenuStateMap: any=(menu: any)=>{
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
                moduleName == 'kylo.feedmgr.templates'||
                moduleName == this.MENU_KEY.ADMIN ){
            return this.menuMap[this.MENU_KEY.ADMIN];
        }
        else {
            return null;
        }
    } 

    buildOpsManagerMenu: any =()=> {
                    
        let links: any[] = [];
        links.push({sref: "dashboard",type:'link', icon: "dashboard", text: this.$filter('translate')('views.main.dashboard'), defaultActive: false, permission: AccessConstants.UI_STATES.DASHBOARD.permissions});
        links.push({sref: "service-health",type:'link', icon: "vector_triangle", text: this.$filter('translate')('views.main.services'), defaultActive: false, permission: AccessConstants.UI_STATES.SERVICE_HEALTH.permissions});
        links.push({sref: "jobs",type:'link', icon: "settings", text: this.$filter('translate')('views.main.jobs'), defaultActive: false, permission: AccessConstants.UI_STATES.JOBS.permissions});
        links.push({sref: "alerts", icon: "notifications", text: this.$filter('translate')('views.main.alerts'), defaultActive: false, permission: AccessConstants.UI_STATES.ALERTS.permissions});
        links.push({sref: "service-level-assessments",type:'link', icon: "work", text: this.$filter('translate')('views.main.sla-assessments'), defaultActive: false, permission: AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENTS.permissions});
        links.push({sref: "scheduler",type:'link', icon: "today", text: this.$filter('translate')('views.main.sla-schedule'), defaultActive: false, permission: AccessConstants.UI_STATES.SCHEDULER.permissions});
        links.push({sref: "charts",type:'link', icon: "insert_chart", text: this.$filter('translate')('views.main.charts'), defaultActive: false, permission: AccessConstants.UI_STATES.CHARTS.permissions});
        this.addExtensionLinks(this.MENU_KEY.OPS_MGR, links);
        var menu = ({type:'toggle', 
                    text: this.$filter('translate')('views.main.operations'),
                    narrowText:this.$filter('translate')('views.main.operations-narrow'),
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
    buildAdminMenu: any=()=>{
        
        let links: any[] = [];
        links.push({sref: "catalog.admin-connectors",type: "link", icon: "settings_input_hdmi", text: this.$filter('translate')('views.main.catalogConnectors'), defaultActive: false, permission: AccessConstants.ADMIN_CONNECTORS});
        links.push({sref: "domain-types", type: "link", icon: "local_offer", text: this.$filter('translate')('views.main.domain-types'), defaultActive: false, permission: AccessConstants.FEEDS_ADMIN});
        links.push({sref: "business-metadata", type:'link', icon: "business", text: this.$filter('translate')('views.main.properties'), defaultActive: false, permission: AccessConstants.CATEGORIES_ADMIN});
        links.push({sref: "registered-templates",type:'link', icon: "layers", text: this.$filter('translate')('views.main.templates'), defaultActive: false, permission: AccessConstants.TEMPLATES_ACCESS});
        links.push({sref: "users",type:'link', icon: "account_box", text: this.$filter('translate')('views.main.users'), defaultActive: false, permission: AccessConstants.USERS_ACCESS});
        links.push({sref: "groups",type:'link', icon: "group", text: this.$filter('translate')('views.main.groups'), defaultActive: false, permission: AccessConstants.GROUP_ACCESS});
        links.push({sref: "sla-email-template",type:'link', icon: "email", text: this.$filter('translate')('views.main.sla-email'), defaultActive: false, permission: AccessConstants.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE});
        links.push({sref: "repository", type: "link", icon: "local_grocery_store", text: this.$filter('translate')('views.main.repository'), defaultActive: false, permission: AccessConstants.GROUP_ACCESS});
        this.addExtensionLinks(this.MENU_KEY.ADMIN, links);
            let menu = ({type:'toggle', 
                    text: this.$filter('translate')('views.main.admin'),
                    narrowText:this.$filter('translate')('views.main.admin-narrow'),
                    expanded:false,
                    links:links,
                    });
        menu.links = links;
        this.menuMap[this.MENU_KEY.ADMIN] = menu;
        return menu
    }

    buildSideNavMenu: any =()=> {
        var menu = [];

        //identify any additional menu items
        this.extensionsMenus = this.AngularModuleExtensionService.getNavigationMenu();

        menu.push(this.buildOpsManagerMenu());
        menu.push(this.buildFeedManagerMenu());
        menu.push(this.buildAdminMenu());

        this.buildExtensionsMenu(menu);
        this.buildMenuStateMap(menu);

        this.toggleSections = _.filter(menu,(item: any)=>{
        return item.type == 'toggle';
        });

        //clear the binding
        this.$scope.menu.length = 0;
        //readd in the values
        _.each(menu,(item: any)=>{
            this.$scope.menu.push(item);
        })
    };

   onAngularExtensionsInitialized:any = () => {
        this.buildSideNavMenu();
    }

    selectMenuItem=($event: any, menuItem: any)=> {
        this.$scope.selectedMenuItem = menuItem;
        this.closeSideNavList();
    }
    
    expandCollapseSideNavList=()=> {
        if(this.$scope.collapsed){
           this.expand();
        }
        else {
            this.collapse();
        }
    }

}

angular.module(moduleName).component("kyloSideNav", {
    controller: SideNav,
    templateUrl: './side-nav.html'
});
