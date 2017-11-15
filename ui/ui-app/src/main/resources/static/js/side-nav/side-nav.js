define(['angular','side-nav/module-name', 'constants/AccessConstants', 'side-nav/module','kylo-services'], function (angular,moduleName,AccessConstants) {

    var directive = function ($mdSidenav, $mdDialog,$rootScope,$transitions,$timeout, SideNavService, AccessControlService, StateService,AccordionMenuService, AngularModuleExtensionService) {
        return {
            restrict: "E",
            scope:{},
            templateUrl: 'js/side-nav/side-nav.html',
            link: function ($scope,$element) {
                $scope.sideNavService = SideNavService;

                /**
                 * a pointer to the highlighted menu item
                 * @type {null}
                 */
                var currentMenuLink = null;

                $scope.menuTitle = '';

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
                /**
                 * Function to call when selecting an item on the left nav
                 * @type {selectMenuItem}
                 */
                $scope.selectMenuItem = selectMenuItem;



                $scope.collapsed = false;

                $scope.expandCollapseSideNavList = expandCollapseSideNavList;

                $scope.adminTitle = "Admin";
                $scope.feedManagerTitle = "Feed Manager";
                $scope.opsManagerTitle = "Operations";

                /**
                 * A map with the sref,parent menu toggle text
                 * this is used to determine what Accordion group should be open given the current state
                 * @type {{}}
                 */
                var menuStateToMenuToggleMap = {};

                /**
                 * Map of the state (sref) to the menu item
                 * @type {{}}
                 */
                var menuStateMap = {};

                /**
                 * Array of the top level accordion toggle menu items (i.e. Feed Manager, Operations, Admin)
                 * @type {Array}
                 */
                var toggleSections = [];

                /**
                 * the <accordion-menu> html $element
                 * @type {null}
                 */
                var $accordionElement = null;

                /**
                 * A map with the moduleName
                 * @type {{}}
                 */
                var menuMap = {};

                var MENU_KEY = {"OPS_MGR":"OPS_MGR","FEED_MGR":"FEED_MGR","ADMIN":"ADMIN"}

                var extensionsMenus = {};

                AngularModuleExtensionService.onInitialized(onAngularExtensionsInitialized);

                /**
                 * Build the Feed Manager Left Nav
                 * @param allowed
                 */
                function buildFeedManagerMenu() {
                    var menu = ({type:'toggle', text: "Feed Manager",narrowText:'Feed',expanded:true,elementId:'toggle_feed_manager'});
                    var links = [];
                    links.push({sref: "feeds",type:'link', icon: "linear_scale", text: "Feeds", permission: AccessConstants.UI_STATES.FEEDS.permissions});
                    links.push({sref: "categories",type:'link', icon: "folder_special", text: "Categories", permission: AccessConstants.UI_STATES.CATEGORIES.permissions});
                    links.push({sref: "tables",type:'link', icon: "grid_on", text: "Tables", permission: AccessConstants.UI_STATES.TABLES.permissions});
                    links.push({sref: "service-level-agreements",type:'link', icon: "beenhere", text: "SLA", permission: AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.permissions});
                    links.push({sref: "visual-query",type:'link', icon: "transform", text: "Visual Query", fullscreen: true, permission:AccessConstants.UI_STATES.VISUAL_QUERY.permissions});
                    addExtensionLinks(MENU_KEY.FEED_MGR, links);
                    menu.links = links;
                    menuMap[MENU_KEY.FEED_MGR] = menu;
                 return menu;

                }

                /**
                 * Build the Ops Manager Left Nav
                 * TODO Switch Permissions to correct ones (i.e remove OPERATIONS_MANAGER_ACCESS, add in detailed permission AccessConstants.CHARTS_ACCESS)
                 * @param allowed
                 */
                function buildOpsManagerMenu() {
                       var menu = ({type:'toggle', text: "Operations",narrowText:'Ops',expanded:false});
                       var links = [];
                       links.push({sref: "dashboard",type:'link', icon: "dashboard", text: "Dashboard", defaultActive: false, permission: AccessConstants.UI_STATES.DASHBOARD.permissions});
                       links.push({sref: "service-health",type:'link', icon: "vector_triangle", text: "Services", defaultActive: false, permission: AccessConstants.UI_STATES.SERVICE_HEALTH.permissions});
                       links.push({sref: "jobs",type:'link', icon: "settings", text: "Jobs", defaultActive: false, permission: AccessConstants.UI_STATES.JOBS.permissions});
                       links.push({sref: "alerts", icon: "notifications", text: "Alerts", defaultActive: false, permission: AccessConstants.UI_STATES.ALERTS.permissions});
                       links.push({sref: "service-level-assessments",type:'link', icon: "work", text: "SLA", defaultActive: false, permission: AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENTS.permissions});
                       links.push({sref: "scheduler",type:'link', icon: "today", text: "Tasks", defaultActive: false, permission: AccessConstants.UI_STATES.SCHEDULER.permissions});
                       links.push({sref: "charts",type:'link', icon: "insert_chart", text: "Charts", defaultActive: false, permission: AccessConstants.UI_STATES.CHARTS.permissions});
                      addExtensionLinks(MENU_KEY.OPS_MGR, links);
                       menu.links = links;
                    menuMap[MENU_KEY.OPS_MGR] = menu;
                    return menu;
                }

                /**
                 * Build the Admin Menu
                 * @param allowed
                 */
                function buildAdminMenu() {

                    var menu = ({type:'toggle', text: "Admin",narrowText:'Admin',expanded:false});
                    var links = [];
                    links.push({sref: "datasources", type: "link", icon: "storage", text: "Data Sources", defaultActive: false, permission: AccessControlService.DATASOURCE_ACCESS});
                    links.push({sref: "domain-types", type: "link", icon: "local_offer", text: "Domain Types", defaultActive: false, permission: AccessControlService.FEEDS_ADMIN});
                    links.push({sref: "business-metadata", type:'link', icon: "business", text: "Properties", defaultActive: false, permission: AccessConstants.CATEGORIES_ADMIN});
                    links.push({sref: "registered-templates",type:'link', icon: "layers", text: "Templates", defaultActive: false, permission: AccessConstants.TEMPLATES_ACCESS});
                    links.push({sref: "users",type:'link', icon: "account_box", text: "Users", defaultActive: false, permission: AccessConstants.USERS_ACCESS});
                    links.push({sref: "groups",type:'link', icon: "group", text: "Groups", defaultActive: false, permission: AccessConstants.GROUP_ACCESS});
                    links.push({sref: "sla-email-templates",type:'link', icon: "email", text: "SLA Email", defaultActive: false, permission: AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES.permissions});
                    addExtensionLinks(MENU_KEY.ADMIN, links);
                    menu.links = links;
                    menuMap[MENU_KEY.ADMIN] = menu;
                  return menu
                }

                /**
                 * Builds additional menu items
                 * @param rootMenu
                 */
                function buildExtensionsMenu(rootMenu){
                    var additionalKeys = _.keys(extensionsMenus);
                    additionalKeys = _.filter(additionalKeys, function(key) {return MENU_KEY[key] == undefined });
                    if(additionalKeys.length >0){
                        _.each(additionalKeys,function(key){
                            var menu = extensionsMenus[key];
                            //ensure this is a toggle type
                            menu.type = 'toggle';
                            _.each(menu.links,function(link) {
                                //ensure we set this type to be a child
                                link.type = 'link';
                            });
                            menuMap[key] = menu;
                            rootMenu.push(menu);
                        })
                    }
                }

                function addExtensionLinks(menuName, links){
                    var extensionLinks = extensionsMenus[menuName];
                    if(extensionLinks && extensionLinks.links){
                        _.each(extensionLinks.links,function(link){
                            //ensure we set this type to be a child
                            link.type = 'link';
                            links.push(link);
                        })
                    }
                }

                /**
                 * Check if the Side Nav is hidden or not
                 * @returns {*|boolean}
                 */
                function isSideNavHidden() {
                    return ($mdMedia('gt-md') && SideNavService.isLockOpen)
                };

                function toggleSideNavList() {
                    $q.when(true).then(function () {
                        $mdSidenav('left').toggle();
                    });
                }

                function updateMenuText() {
                    var toggleItems = _.filter($scope.menu,function(item){
                        return item.type == 'toggle';
                    });
                    _.each(toggleItems,function(item){
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
                    angular.element('md-sidenav').css('overflow','hidden')
                    angular.element('md-sidenav > md-content').css('overflow','hidden')
                    angular.element('md-sidenav').addClass('collapsed');
                    updateMenuText();
                }

                function expand(){
                    $scope.collapsed = false;
                  //  angular.element('md-sidenav > md-content >div:first').css('overflow-y','auto')
                    angular.element('md-sidenav').css('overflow','auto')
                    angular.element('md-sidenav > md-content').css('overflow','auto')
                    angular.element('md-sidenav').removeClass('collapsed');
                    updateMenuText();
                }

                function expandCollapseSideNavList() {
                    if($scope.collapsed){
                        expand();
                    }
                    else {
                        collapse();
                    }
                }

                function closeSideNavList() {
                    $mdSidenav('left').close();
                }

                function selectMenuItem($event, menuItem) {
                    $scope.selectedMenuItem = menuItem;
                    closeSideNavList();
                }

                function accessDeniedDialog(title,content){
                    $mdDialog.show(
                        $mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title(title)
                            .textContent(content)
                            .ariaLabel('Access Deined')
                            .ok('Got it!')
                    );
                }

                function joinArray(mainArr,joiningArr){
                    _.each(joiningArr,function(item){
                        mainArr.push(item);
                    })
                    return mainArr;
                }

                function buildSideNavMenu() {
                    var menu = [];

                    //identify any additional menu items
                    extensionsMenus = AngularModuleExtensionService.getNavigationMenu();


                    menu.push(buildOpsManagerMenu());
                    menu.push(buildFeedManagerMenu());
                    menu.push(buildAdminMenu());
                    buildExtensionsMenu(menu);
                    buildMenuStateMap(menu);

                    toggleSections = _.filter(menu,function(item){
                        return item.type == 'toggle';
                    });

                    //clear the binding
                    $scope.menu.length = 0;
                    //readd in the values
                    _.each(menu,function(item){
                        $scope.menu.push(item);
                    })
                }

                function buildMenuStateMap(menu){
                    menuStateToMenuToggleMap = {};
                    menuStateMap = {}
                    _.each(menu,function(menuToggle) {
                        _.each(menuToggle.links,function(item){
                            menuStateToMenuToggleMap[item.sref] = menuToggle;
                            menuStateMap[item.sref] = item;
                        });
                    });
                }

                function onAngularExtensionsInitialized(){
                    buildSideNavMenu();
                }

                if(AngularModuleExtensionService.isInitialized()){
                    buildSideNavMenu();
                }



                function menuToggleItemForModuleName(moduleName){
                    if(moduleName.indexOf('opsmgr') >=0){
                        return menuMap[MENU_KEY.OPS_MGR];
                    }
                    else if(moduleName.indexOf('feedmgr') >=0 && moduleName != 'kylo.feedmgr.datasources' && moduleName != "kylo.feedmgr.domain-types" && moduleName != 'kylo.feedmgr.templates'){
                        return menuMap[MENU_KEY.FEED_MGR];
                    }
                    else if(moduleName.indexOf('auth') >=0 || moduleName == 'kylo.feedmgr.datasources' || moduleName == "kylo.feedmgr.domain-types" || moduleName == 'kylo.feedmgr.templates'){
                        return menuMap[MENU_KEY.ADMIN];
                    }
                    else {
                        return null;
                    }
                }



                $transitions.onSuccess({},function(transition){
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
                    if(parentMenu != undefined && $accordionElement != null && $accordionElement != undefined){
                        if(!parentMenu.expanded){
                            AccordionMenuService.openToggleItem(parentMenu,$accordionElement,false,toggleSections);
                        }
                    }
                });


            }
        }
    };

    angular.module(moduleName).directive('kyloSideNav', ['$mdSidenav','$mdDialog','$rootScope','$transitions','$timeout','SideNavService','AccessControlService','StateService','AccordionMenuService','AngularModuleExtensionService', directive]);
});
