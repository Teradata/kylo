import * as angular from "angular";
import {moduleName} from "../module-name";

angular.module(moduleName).directive("tbaViewTypeSelection",
  [ () => {
          return {
            restrict: 'E',
            templateUrl: 'js/common/view-type-selection/view-type-selection-template.html',
            scope: {
                viewType: '='
            },
            link: function ($scope: any, elem: any, attr: any) {

                $scope.viewTypeChanged = function (viewType: any) {
                    $scope.viewType = viewType;
                }

            }
        }
  }
  ]);