import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from "angular";

import {moduleName} from "../module-name";
import {VerticalSectionLayoutComponent} from "./vertical-section-layout.component";

angular.module(moduleName)
    .directive("verticalSectionLayout", downgradeComponent({component: VerticalSectionLayoutComponent}) as angular.IDirectiveFactory);
