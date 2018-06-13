import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import {KyloOptionsComponent} from "./kylo-options.component";

angular.module(moduleName)
    .directive("kyloOptions", downgradeComponent({component: KyloOptionsComponent}) as angular.IDirectiveFactory);
