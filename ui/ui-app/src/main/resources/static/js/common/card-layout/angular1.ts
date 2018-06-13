import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import {CardLayoutComponent} from "./card-layout.component";

angular.module(moduleName)
    .directive("cardLayout", downgradeComponent({component: CardLayoutComponent}) as angular.IDirectiveFactory);
