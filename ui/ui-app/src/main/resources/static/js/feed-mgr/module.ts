
import * as angular from 'angular';
import {moduleName} from "./module-name";
import "kylo-common";
import "kylo-services";
import "codemirror-require/module";
import "jquery";
import  'angular-drag-and-drop-lists';
import 'fattable';
import './catalog/catalog.module';
import './shared/apply-domain-type/apply-table-domain-types.component.scss';

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]);
        this.module.config(['$compileProvider',this.configFn.bind(this)]);
        this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);
    }
    configFn($compileProvider:angular.ICompileProvider) {
        //pre-assign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);
    }
    runFn($ocLazyLoad: any){
        $ocLazyLoad.load({
            name: 'kylo',
            files: [
            ], serie: true
        })

    }

}

const module = new ModuleFactory();
export default module;

