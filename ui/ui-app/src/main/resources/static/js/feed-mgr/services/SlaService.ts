import * as angular from 'angular';
import * as _ from "underscore";
import {moduleName} from "../module-name";
import {SlaService} from "./sla.service";
import { downgradeInjectable } from '@angular/upgrade/static';

angular.module(moduleName).service('SlaService', downgradeInjectable(SlaService));
