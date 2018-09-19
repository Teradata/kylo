import * as angular from "angular";
import {moduleName} from "../module-name";
import {Nvd3ChartService} from "../../services/nvd3-chart.service";
import {downgradeInjectable} from "@angular/upgrade/static";

angular.module(moduleName).service('Nvd3ChartService',downgradeInjectable(Nvd3ChartService));