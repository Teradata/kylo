import * as angular from 'angular';
import {moduleName} from "./module-name";
export {KyloServicesModule} from "../services/services.module";
export {KyloCommonModule} from "../common/common.module";
export {UserService} from "./services/UserService";
export {PermissionsTableController} from "./shared/permissions-table/permissions-table";

export  let moduleRequire = angular.module(moduleName);