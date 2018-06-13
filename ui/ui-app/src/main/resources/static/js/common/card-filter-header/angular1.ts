import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import {CardFilterHeaderComponent} from "./card-filter-header.component";

angular.module(moduleName)
    .directive("tbaCardFilterHeader", downgradeComponent({component: CardFilterHeaderComponent}) as angular.IDirectiveFactory);
