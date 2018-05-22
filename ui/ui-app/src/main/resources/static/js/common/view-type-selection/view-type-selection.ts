import * as angular from "angular";
import {moduleName} from "../module-name";

export default class ViewTypeSelection {

    viewType: any;

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {
        this.$scope.viewTypeChanged = (viewType: any) => {
            this.viewType = viewType;
        }
    }

    static readonly $inject = ["$scope"];

    constructor(private $scope: IScope) {}
}

angular.module(moduleName).component("tbaViewTypeSelection",{
    controller: ViewTypeSelection,
    bindings: {
        viewType: '='
    },
    templateUrl: 'js/common/view-type-selection/view-type-selection-template.html'
});

// angular.module(moduleName).directive("tbaViewTypeSelection",
//   [ () => {
//           return {
//             restrict: 'E',
//             templateUrl: 'js/common/view-type-selection/view-type-selection-template.html',
//             scope: {
//                 viewType: '='
//             },
//             controller: ViewTypeSelection
//         }
//   }
//   ]);