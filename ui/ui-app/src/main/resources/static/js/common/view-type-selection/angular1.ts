import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import {ViewTypeSelectionComponent} from "./view-type-selection.component";

angular.module(moduleName)
    .directive("tbaViewTypeSelection", downgradeComponent({component: ViewTypeSelectionComponent}) as angular.IDirectiveFactory);
