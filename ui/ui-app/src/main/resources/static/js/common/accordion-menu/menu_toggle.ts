import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from "underscore";

 angular.module(moduleName)
        .run(['$templateCache', ($templateCache: any)=> {
            $templateCache.put('menu-toggle.tmpl.html',
            '<div class="collapsible-item" ng-class="{open: section.expanded}" ng-if="section.hidden == false" id="{{section.elementId}}">'
            + '<div class="title" ng-class="{disabled: section.disabled}" ng-click="toggle()" flex layout-align="start start" layout="row">'
            + '   <span flex>{{section.text}}</span>'
            + '   <ng-md-icon md-icon icon="{{section.expandIcon}}" ng-if="!isCollapsed()"></ng-md-icon>'
            + '</div>'
            + ' <div class="accordion-body">'
            + ' <md-list id="menu-{{section.text}}" class="accordion-list">\n'
            + '  <md-list-item ng-repeat="item in section.links" ng-if-permission="{{item.permission}}">\n'
            + '    <menu-link section="item"></menu-link>\n'
            + '  </md-list-item>\n'
            + '</md-list> '
            + ' </div>'
            + '</div>');
        }])
        .directive('menuToggle', ['$timeout','AccessControlService', ($timeout: any,AccessControlService: any)=> {
            return {
                scope: {
                    section: '='
                },
                require: '^accordionMenu',
                templateUrl: 'menu-toggle.tmpl.html',
                link: function (scope: any, element: any,attrs: any,controller: any) {
                    scope.section.hidden = true;

                    scope.isOpened = scope.section.expanded;


                    if(scope.isOpened) {
                        scope.section.expandIcon = 'expand_less';
                    }
                    else {
                        scope.section.expandIcon = 'expand_more';
                    }


                    scope.isCollapsed= controller.isCollapsed;

                    scope.$watch('section.collapsed',(newVal: any,oldVal: any)=>{
                        if(newVal == true){
                            element.find('.toggle-label').addClass('collapsed-toggle-header').removeClass('layout-padding-left-8');
                            element.find('.menu-link').removeClass('layout-padding-left-8');
                        }
                        else {
                            element.find('.toggle-label').removeClass('collapsed-toggle-header').addClass('layout-padding-left-8')
                            element.find('.menu-link').addClass('layout-padding-left-8');
                        }
                    })

                    var checkPermissions = ()=>{
                        AccessControlService.doesUserHavePermission(getTogglePermissions()).then((allowed: any)=>{
                            //if not allowed, remove the links;
                            if(!allowed){
                                scope.section.links = [];
                            }

                            scope.section.hidden = !allowed;
                        })
                    }


                    var getTogglePermissions = ()=>{
                            var allPermissions: any[] = [];
                            _.each(scope.section.links,(item: any)=>{
                                var permissionStr = item.permission;
                                if(permissionStr != undefined) {
                                    var arr = [];
                                    if(angular.isArray(permissionStr)) {
                                        arr = permissionStr;
                                        //the directive template uses the permission key to check.  it needs to be a string.
                                        item.permission = arr.join(",")
                                    }
                                    else {
                                        arr = permissionStr.split(',');
                                    }
                                    allPermissions = _.union(allPermissions, arr);
                                }
                            });
                            return allPermissions;
                    }
                    checkPermissions();
                    scope.toggle = ()=> {
                        if(!scope.section.expanded) {
                            controller.openToggleItem(scope.section);
                        }
                        else {
                            scope.section.expanded = false;
                            scope.section.expandIcon = 'expand_more';
                        }
                    };

                }
            };
        }]);
