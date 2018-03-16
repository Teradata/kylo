import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from "underscore";
'use strict';

angular.module(moduleName).run(['$templateCache', ($templateCache: any)=>{
        $templateCache.put('accordion-menu.tmpl.html',
            ' <md-list class="side-menu " > \n' +
            ' <md-list-item ng-repeat="section in vm.menu" flex layout-fill ng-if="!section.hidden"> \n' +
        ' <menu-link section="section" ng-if="section.type === \'link\'" style="width:100%"></menu-link>\n' +
        ' <menu-toggle section="section" ng-if="section.type === \'toggle\' " style="width:100%"></menu-toggle>\n' +
            '<md-divider></md-divider>' +
        ' </md-list-item>\n'
            + '</md-list>' + '');
        }])
        .directive('accordionMenu', ['$location','$timeout','AccordionMenuService',
        ($location: any,$timeout: any,AccordionMenuService: any)=> {
            return {
                bindToController: {
                    menu: '=',
                    collapsed: '=',
                    allowMultipleOpen:'=?'
                },
                controllerAs: 'vm',
                templateUrl: 'accordion-menu.tmpl.html',
                link: function ($scope: any, $element: any) {

                },
                controller: function($scope: any,$element: any){
                    /**
                     * Pointer to the current section that is open
                     * @type {null}
                     */
                    this.openedSection = null;

                    this.autoFocusContent = false;
                    /**
                     * Allow for multiple open sections
                     * @type {boolean}
                     */
                    this.allowMultipleOpen = angular.isDefined(this.allowMultipleOpen) ? this.allowMultipleOpen : false;

                    /**
                     * list all toggle sections
                     * @type {Array}
                     */
                    this.toggleSections = []


                    /**
                     * Initially open those sections that are set to be expanded
                     */
                    var setToggleSections = ()=>{
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

                    setToggleSections();

                    $scope.$watchCollection(
                        ()=>{ return this.menu},
                        ( newValue: any, oldValue: any) =>{
                         setToggleSections();
                        }
                    );



                    this.focusSection = ()=>{
                        // set flag to be used later when
                        // $locationChangeSuccess calls openPage()
                        $scope.autoFocusContent = true;
                    };

                    /**
                     * is the menu collapsed
                     * @returns {boolean}
                     */
                    this.isCollapsed = ()=>{
                        return this.collapsed == true;
                    }

                    /**
                     * open the menu item
                     * @param section
                     */
                    this.openToggleItem = (section: any)=>{
                        AccordionMenuService.openToggleItem(section,$element,this.allowMultipleOpen,this.toggleSections);

                    }

                }
            };
        }])
