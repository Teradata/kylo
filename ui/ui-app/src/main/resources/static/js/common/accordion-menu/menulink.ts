import * as angular from "angular";
import {moduleName} from "../module-name";
'use strict';
angular.module(moduleName).run(['$templateCache',  ($templateCache: any)=>{
            $templateCache.put('menu-link.tmpl.html',
            '<md-button flex ui-sref="{{section.sref}}" ng-click="selectMenuItem()" class="nav-btn" \n ' +
            'ng-class="{\'selected\' : section.selected,\'md-icon-button\': controller.collapsed}"  > \n' +
            '<div class="layout-padding-left-8 menu-link"><ng-md-icon md-icon icon="{{section.icon}}" class="nav-btn" ng-class="{\'selected\' : section.selected }" ></ng-md-icon> \n '+
            '<md-tooltip md-direction="right" ng-if="controller.collapsed" >{{section.text}}</md-tooltip>'+
            '<span style="padding-left:10px;" ng-if="!controller.collapsed">{{section.text}}</span> '
            + '</div>'
            + '</md-button>\n '+
                '');
        }])
        .directive('menuLink', ()=> {
            return {
                scope: {
                    section: '='
                },
                require: '^accordionMenu',
                templateUrl: 'menu-link.tmpl.html',
                link: function ($scope: any, $element: any,attrs: any,controller: any) {

                    $scope.controller = controller;

                    $scope.selectMenuItem = function () {
                        // set flag to be used later when
                        // $locationChangeSuccess calls openPage()
                        controller.autoFocusContent = true;

                    };
                    $scope.isCollapsed = function(){
                        return controller.isCollapsed();
                    }
                    $scope.sectionClass = function() {
                        if($scope.section == controller.currentSection ) {
                            return 'selected';
                        }
                        else {
                            return 'nav-btn';
                        }
                    }
                }
            };
        })
