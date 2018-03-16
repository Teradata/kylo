import * as angular from 'angular';
import {moduleName} from "./module-name";
import {KyloServicesModule} from "../services/services.module";

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,['kylo.services']); 
        this.module.config(['$compileProvider',this.configFn.bind(this)]);
    }
    configFn($compileProvider:any) {
        //pre-assign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);
    }  
} 
const module = new ModuleFactory();
export default module;

