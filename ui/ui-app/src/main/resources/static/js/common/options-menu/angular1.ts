import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import {OptionsMenuComponent} from "./OptionsMenu.component";

angular.module(moduleName)
    .directive("tbaOptionsMenu", downgradeComponent({component: OptionsMenuComponent}) as angular.IDirectiveFactory);
