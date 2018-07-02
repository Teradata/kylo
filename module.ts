import * as angular from 'angular';
import {moduleName} from "./module-name";
import "kylo-services";
import "./dir-pagination/dirPagination-arrows";
import "angular-nvd3";
import  "/common.module";

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName, ['kylo.services','templates.navigate-before.html', 'templates.navigate-first.html', 'templates.navigate-last.html', 'templates.navigate-next.html']);
        this.module.config(['$compileProvider',this.configFn.bind(this)]);
    }
    
   //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
    configFn($compileProvider: any) {
        //pre-assign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);
    }
} 

const module = new ModuleFactory();
export default module;