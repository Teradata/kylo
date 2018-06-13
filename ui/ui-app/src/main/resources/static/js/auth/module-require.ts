import * as angular from 'angular';
import {moduleName} from "./module-name";
import "../services/services.module";
import "../common/common.module";
import "./services/UserService";
import "./shared/permissions-table/permissions-table";

export  let moduleRequire = angular.module(moduleName);