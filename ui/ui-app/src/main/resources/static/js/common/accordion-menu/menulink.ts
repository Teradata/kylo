import * as angular from "angular";
import {moduleName} from "../module-name";
'use strict';
angular.module(moduleName)
        .directive('menuLink', ()=> {
            return {
                scope: {
                    section: '='
                },
                require: '^accordionMenu',
                templateUrl: './menu-link.tmpl.html',
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
