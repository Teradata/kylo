import * as angular from 'angular';
import * as _ from 'underscore';
import {moduleName} from "./module-name";
import AccessConstants from "../constants/AccessConstants";
import "./module";
import "../services/services.module";
import "pascalprecht.translate";

export class directive implements ng.IDirective {
    restrict: any = "E";
    scope: any = {};
    templateUrl: any= 'js/side-nav/side-nav.html';
    constructor(private $mdSidenav: any,
            private $mdDialog: any,
            private $rootScope: any,
            private $transitions: any,
            private $timeout: any,
            private SideNavService: any,
            private AccessControlService: any,
            private StateService: any,
            private AccordionMenuService: any,
            private AngularModuleExtensionService: any,
            private $filter: any
      ) {
            }// close constructor

    static factory() {
      var sidedirective = ($mdSidenav: any,
                            $mdDialog: any,
                            $rootScope: any,
                            $transitions: any,
                            $timeout: any,
                            SideNavService: any,
                            AccessControlService: any,
                            StateService: any,
                            AccordionMenuService: any,
                            AngularModuleExtensionService: any,
                            $filter: any) => {
        return new directive($mdSidenav, $mdDialog,$rootScope,$transitions,$timeout, SideNavService, AccessControlService, StateService,AccordionMenuService, AngularModuleExtensionService, $filter);
      }
      return sidedirective;
    }
    
    link=($scope: any, $element: any)=>{             
              /**
             * Build the Feed Manager Left Nav
             * @param allowed
             */
           // let AccessConstants = new AccessConstants();
            let buildFeedManagerMenu: any=()=>{
                let links: any[] = [];
                links.push({sref: "feeds",type:'link', icon: "linear_scale", 
                            text: this.$filter('translate')('views.main.feeds'), 
                            permission: AccessConstants.UI_STATES.FEEDS.permissions});
                links.push({sref: "categories",type:'link', icon: "folder_special", text: this.$filter('translate')('views.main.categories'), permission: AccessConstants.UI_STATES.CATEGORIES.permissions});
                links.push({sref: "service-level-agreements",type:'link', icon: "beenhere", text: this.$filter('translate')('views.main.sla'), permission: AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.permissions});
                links.push({sref: "visual-query",type:'link', icon: "transform", text:this.$filter('translate')('views.main.visual-query'), fullscreen: true, permission:AccessConstants.UI_STATES.VISUAL_QUERY.permissions});
                links.push({sref: "catalog",type:'link', icon: "grid_on", text: this.$filter('translate')('views.main.tables'), permission: AccessConstants.UI_STATES.TABLES.permissions});
                addExtensionLinks(MENU_KEY.FEED_MGR, links);
                var menu = ({type:'toggle', 
                        text: this.$filter('translate')('views.main.feed-manage'),
                        narrowText:this.$filter('translate')('views.main.feed-manage-narrow'),
                        expanded:true,
                        elementId:'toggle_feed_manager',
                        links:links});
                menu.links = links;
                menuMap[MENU_KEY.FEED_MGR] = menu;
                return menu;
            }

            /**
             * Build the Ops Manager Left Nav
             * TODO Switch Permissions to correct ones (i.e remove OPERATIONS_MANAGER_ACCESS, add in detailed permission AccessConstants.CHARTS_ACCESS)
             * @param allowed
             */
  
            let buildOpsManagerMenu: any =()=> {
                
                let links: any[] = [];
                links.push({sref: "dashboard",type:'link', icon: "dashboard", text: this.$filter('translate')('views.main.dashboard'), defaultActive: false, permission: AccessConstants.UI_STATES.DASHBOARD.permissions});
                links.push({sref: "service-health",type:'link', icon: "vector_triangle", text: this.$filter('translate')('views.main.services'), defaultActive: false, permission: AccessConstants.UI_STATES.SERVICE_HEALTH.permissions});
                links.push({sref: "jobs",type:'link', icon: "settings", text: this.$filter('translate')('views.main.jobs'), defaultActive: false, permission: AccessConstants.UI_STATES.JOBS.permissions});
                links.push({sref: "alerts", icon: "notifications", text: this.$filter('translate')('views.main.alerts'), defaultActive: false, permission: AccessConstants.UI_STATES.ALERTS.permissions});
                links.push({sref: "service-level-assessments",type:'link', icon: "work", text: this.$filter('translate')('views.main.sla-assessments'), defaultActive: false, permission: AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENTS.permissions});
                links.push({sref: "scheduler",type:'link', icon: "today", text: this.$filter('translate')('views.main.sla-schedule'), defaultActive: false, permission: AccessConstants.UI_STATES.SCHEDULER.permissions});
                links.push({sref: "charts",type:'link', icon: "insert_chart", text: this.$filter('translate')('views.main.charts'), defaultActive: false, permission: AccessConstants.UI_STATES.CHARTS.permissions});
                addExtensionLinks(MENU_KEY.OPS_MGR, links);
                var menu = ({type:'toggle', 
                            text: this.$filter('translate')('views.main.operations'),
                            narrowText:this.$filter('translate')('views.main.operations-narrow'),
                            expanded:false,
                            links:links});
                menu.links = links;
                menuMap[MENU_KEY.OPS_MGR] = menu;
                return menu;
            }
          
           /**
             * Build the Admin Menu
             * @param allowed
             */
           let buildAdminMenu: any=()=>{
                
                let links: any[] = [];
                links.push({sref: "datasources", type: "link", icon: "storage", text: this.$filter('translate')('views.main.data-sources'), defaultActive: false, permission: this.AccessControlService.DATASOURCE_ACCESS});
                links.push({sref: "domain-types", type: "link", icon: "local_offer", text: this.$filter('translate')('views.main.domain-types'), defaultActive: false, permission: this.AccessControlService.FEEDS_ADMIN});
                links.push({sref: "business-metadata", type:'link', icon: "business", text: this.$filter('translate')('views.main.properties'), defaultActive: false, permission: AccessConstants.CATEGORIES_ADMIN});
                links.push({sref: "registered-templates",type:'link', icon: "layers", text: this.$filter('translate')('views.main.templates'), defaultActive: false, permission: AccessConstants.TEMPLATES_ACCESS});
                links.push({sref: "users",type:'link', icon: "account_box", text: this.$filter('translate')('views.main.users'), defaultActive: false, permission: AccessConstants.USERS_ACCESS});
                links.push({sref: "groups",type:'link', icon: "group", text: this.$filter('translate')('views.main.groups'), defaultActive: false, permission: AccessConstants.GROUP_ACCESS});
                links.push({sref: "sla-email-templates",type:'link', icon: "email", text: this.$filter('translate')('views.main.sla-email'), defaultActive: false, permission: AccessConstants.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE});
                addExtensionLinks(MENU_KEY.ADMIN, links);
                    let menu = ({type:'toggle', 
                            text: this.$filter('translate')('views.main.admin'),
                            narrowText:this.$filter('translate')('views.main.admin-narrow'),
                            expanded:false,
                            links:links,
                            });
                menu.links = links;
                menuMap[MENU_KEY.ADMIN] = menu;
                return menu
            }

    let buildSideNavMenu: any =()=> {
        var menu = [];

        //identify any additional menu items
        extensionsMenus = this.AngularModuleExtensionService.getNavigationMenu();

        menu.push(buildOpsManagerMenu());
        menu.push(buildFeedManagerMenu());
        menu.push(buildAdminMenu());
        buildExtensionsMenu(menu);
        buildMenuStateMap(menu);

        toggleSections = _.filter(menu,(item: any)=>{
            return item.type == 'toggle';
        });

        //clear the binding
        $scope.menu.length = 0;
        //readd in the values
        _.each(menu,(item: any)=>{
            $scope.menu.push(item);
        })
    };
  
            $scope.sideNavService = this.SideNavService;
            /**
             * The menu
             * @type {Array}
             */
            $scope.menu = [];
            /**
             * The selected menu item
             * @type {null}
             */
            $scope.selectedMenuItem = null;
            let selectMenuItem=($event: any, menuItem: any)=> {
                $scope.selectedMenuItem = menuItem;
                closeSideNavList();
            }
            
            let expandCollapseSideNavList=()=> {
                if($scope.collapsed){
                    expand();
                }
                else {
                    collapse();
                }
            }
            /**
             * Function to call when selecting an item on the left nav
             * @type {selectMenuItem}
             */
            $scope.selectMenuItem = selectMenuItem;
            $scope.collapsed = false;
            $scope.expandCollapseSideNavList = expandCollapseSideNavList;
            $scope.adminTitle = this.$filter('translate')('views.main.adminTitle');
            $scope.feedManagerTitle = this.$filter('translate')('views.main.feedManagerTitle');
            $scope.opsManagerTitle = this.$filter('translate')('views.main.opsManagerTitle');

            $scope.menuTitle = '';

   /**
     * A map with the moduleName
     * @type {{}}
     */
    let menuMap: any = {};
    
    let MENU_KEY: any = {"OPS_MGR":"OPS_MGR","FEED_MGR":"FEED_MGR","ADMIN":"ADMIN"}
    let extensionsMenus: any = {};  
    /**
     * a pointer to the highlighted menu item
     * @type {null}
     */
    let currentMenuLink: any = null;            
    /**
     * A map with the sref,parent menu toggle text
     * this is used to determine what Accordion group should be open given the current state
     * @type {{}}
     */
    let menuStateToMenuToggleMap: any = {};
    /**
     * Map of the state (sref) to the menu item
     * @type {{}}
     */
    let menuStateMap: any = {};
    /**
     * Array of the top level accordion toggle menu items (i.e. Feed Manager, Operations, Admin)
     * @type {Array}
     */
    var toggleSections: any = [];
    /**
     * the <accordion-menu> html $element
     * @type {null}
     */
    let $accordionElement: any = null;
    function updateMenuText() {
        var toggleItems = _.filter($scope.menu,(item: any)=>{
            return item.type == 'toggle';
        });
        _.each(toggleItems,(item: any)=>{
            if(item.origText == undefined) {
                item.origText = item.text;
            }
            item.collapsed = $scope.collapsed;
            if($scope.collapsed){
                item.text = item.narrowText;

            }
            else {
                item.text = item.origText;
            }
        })
        }

        function collapse(){
                $scope.collapsed = true;
                // angular.element('md-sidenav > md-content >div:first').css('overflow-','hidden')
                    angular.element('md-sidenav.site-sidenav').css('overflow','hidden')
                    angular.element('md-sidenav.site-sidenav > md-content').css('overflow','hidden')
                    angular.element('md-sidenav.site-sidenav').addClass('collapsed');
                updateMenuText();
            }

            function expand(){
                $scope.collapsed = false;
                //  angular.element('md-sidenav > md-content >div:first').css('overflow-y','auto')
                    angular.element('md-sidenav.site-sidenav').css('overflow','auto')
                    angular.element('md-sidenav.site-sidenav > md-content').css('overflow','auto')
                    angular.element('md-sidenav.site-sidenav').removeClass('collapsed');
                updateMenuText();
            }

   

            /**
             * Check if the Side Nav is hidden or not
             * @returns {*|boolean}
             */
            /* $mdMedia: any;
            $q: any;*/
            let isSideNavHidden=()=> { //To Be Revised // SMB
                return ($scope.$mdMedia('gt-md') && this.SideNavService.isLockOpen);
            };

            let toggleSideNavList=()=> {
                $scope.$q.when(true).then( ()=> {
                    this.$mdSidenav('left').toggle();
                });
            }
            /**
             * Builds additional menu items
             * @param rootMenu
             */
            let buildExtensionsMenu: any=(rootMenu: any)=>{
                var additionalKeys = _.keys(extensionsMenus);
                additionalKeys = _.filter(additionalKeys, (key: any)=> {return MENU_KEY[key] == undefined });
                if(additionalKeys.length >0){
                    _.each(additionalKeys,(key: any)=>{
                        var menu = extensionsMenus[key];
                        //ensure this is a toggle type
                        menu.type = 'toggle';
                        _.each(menu.links,(link: any)=> {
                            //ensure we set this type to be a child
                            link.type = 'link';
                        });
                        menuMap[key] = menu;
                        rootMenu.push(menu);
                    })
                }
            }

        let addExtensionLinks: any =(menuName: any, links: any)=>{
            var extensionLinks = extensionsMenus[menuName];
            if(extensionLinks && extensionLinks.links){
                _.each(extensionLinks.links,(link: any)=>{
                    //ensure we set this type to be a child
                    link.type = 'link';
                    links.push(link);
                })
            }
        }

        let closeSideNavList=()=>
        {
            this.$mdSidenav('left').close();
        }

        let accessDeniedDialog: any =(title: any,content: any)=>{
            this.$mdDialog.show(
                this.$mdDialog.alert()
                                .clickOutsideToClose(true)
                                .title(title)
                                .textContent(content)
                                .ariaLabel(this.$filter('translate')('views.main.access-denied'))
                                .ok(this.$filter('translate')('views.main.got-it'))
            );
        }

        function joinArray(mainArr: any, joiningArr: any): any{
                    _.each(joiningArr,(item: any)=>{
                        mainArr.push(item);
                    })
            return mainArr;
        }

        let buildMenuStateMap: any=(menu: any)=>{
            menuStateToMenuToggleMap = {};
            menuStateMap = {};
            _.each(menu,(menuToggle: any)=> {
                _.each(menuToggle.links,(item: any)=>{
                    menuStateToMenuToggleMap[item.sref] = menuToggle;
                    menuStateMap[item.sref] = item;
                });
            });
        }

        function menuToggleItemForModuleName(moduleName: any): any{
            if(moduleName.indexOf('opsmgr') >=0){
                return menuMap[MENU_KEY.OPS_MGR];
            }
            else if(moduleName.indexOf('feedmgr') >=0 &&
                    moduleName != 'kylo.feedmgr.datasources' && 
                    moduleName != "kylo.feedmgr.domain-types" && 
                    moduleName != 'kylo.feedmgr.templates'){
                return menuMap[MENU_KEY.FEED_MGR];
            }
            else if(moduleName.indexOf('auth') >=0 || 
                    moduleName == 'kylo.feedmgr.datasources' ||
                    moduleName == "kylo.feedmgr.domain-types" || 
                    moduleName == 'kylo.feedmgr.templates'){
                return menuMap[MENU_KEY.ADMIN];
            }
            else {
                return null;
            }
        } 
           let onAngularExtensionsInitialized: any=()=>{
                buildSideNavMenu();
            }

           this.AngularModuleExtensionService.onInitialized(onAngularExtensionsInitialized);
 
        
            if(this.AngularModuleExtensionService.isInitialized()){
                buildSideNavMenu();
            }

            this.$transitions.onSuccess({},(transition: any)=>{
            var currentState = transition.to();
            var parentMenu = menuStateToMenuToggleMap[currentState.name];
            var menuLink = menuStateMap[currentState.name];
            if(menuLink != undefined ){
                if(currentMenuLink != null && currentMenuLink != menuLink) {
                    currentMenuLink.selected = false;
                }
                currentMenuLink = menuLink;
                currentMenuLink.selected = true;
            }

            if($accordionElement == null){
                $accordionElement = $element.find('accordion-menu');
            }
            if(parentMenu == undefined && currentState != undefined && currentState.data != undefined) {
                //attempt to locate the menu based upon the moduleName defined on the state
                var moduleName = currentState.data.module;
                if(moduleName != undefined) {
                    var menuToggle = menuToggleItemForModuleName(moduleName);
                    if(menuToggle != null){
                        parentMenu = menuToggle;
                    }
                }
            }
            
            if(parentMenu != undefined && 
                $accordionElement != null && 
                $accordionElement != undefined){
                if(!parentMenu.expanded){
                    this.AccordionMenuService.openToggleItem(parentMenu,
                                                            $accordionElement,
                                                            false,
                                                            toggleSections);
                }
               }
            });
    
   
    } // end of this.link


    }
 angular.module(moduleName).directive('kyloSideNav',directive.factory());