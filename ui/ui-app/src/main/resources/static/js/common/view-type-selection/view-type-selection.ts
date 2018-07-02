import * as angular from "angular";
import {moduleName} from "../module-name";

export default class ViewTypeSelection {

    viewType: any;

    viewTypeChanged(viewType: any) {
        this.viewType = viewType;
    }
}

angular.module(moduleName).component("tbaViewTypeSelection",{
    controller: ViewTypeSelection,
    bindings: {
        viewType: '='
    },
    templateUrl: 'js/common/view-type-selection/view-type-selection-template.html'
});
