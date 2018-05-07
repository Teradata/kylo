import * as angular from "angular";
import {moduleName} from "../module-name";
const Draggabilly = require("../../../bower_components/draggabilly/dist/draggabilly.pkgd");

angular.module(moduleName).directive('ngDraggable', [()=> {
        return {
            restrict: 'A',
            scope: {
                dragOptions: '=ngDraggable'
            },
            link: function (scope: any, elem: any, attr: any) {
                if (scope.dragOptions == undefined) {
                    scope.dragOptions = {};
                }
                var draggable = new Draggabilly(elem[0], scope.dragOptions);
            }
        }
}]);