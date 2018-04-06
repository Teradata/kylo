import * as angular from 'angular';
//const lazyLoadUtil = require('../kylo-utils/LazyLoadUtil');
import lazyLoadUtil from "../kylo-utils/LazyLoadUtil";
const CodeMirror = require('../../bower_components/codemirror/lib/codemirror');
//import * as CodeMirror from "../../bower_components/codemirror/lib/codemirror";
import "../../bower_components/angular-ui-codemirror/ui-codemirror";
//const ui_code_mirror = require('../../bower_components/angular-ui-codemirror/ui-codemirror');
//const moduleName =require("./module-name");
import {moduleName} from "./module-name";

/*declare global {
    interface Window { CodeMirror: any; }
}
*/
class ModuleFactory  {
    module: ng.IModule;
   // codeMirror: CodeMirror;
    constructor () {
       (<any>window).CodeMirror = CodeMirror;
       // Window.CodeMirror = CodeMirror;
        this.module = angular.module(moduleName,[]); 
        this.module.run(['$ocLazyLoad', this.runFn.bind(this)]); 
    }
    runFn($ocLazyLoad: any){
         $ocLazyLoad.load({name:'kylo',files:[
                                             'bower_components/codemirror/lib/codemirror.css',
                                             'bower_components/codemirror/addon/hint/show-hint.css',
                                             'bower_components/codemirror/addon/dialog/dialog.css',
                                             'bower_components/codemirror/addon/tern/tern.css',
                                             'codemirror/mode/pig/pig',
                                             'codemirror/mode/properties/properties',
                                             'codemirror/mode/python/python',
                                             'codemirror/mode/velocity/velocity',
                                             'codemirror/mode/xml/xml',
                                             'codemirror/mode/shell/shell',
                                             'codemirror/mode/javascript/javascript',
                                             'codemirror/mode/sql/sql',
                                             'codemirror/addon/tern/tern',
                                             'codemirror/addon/hint/show-hint',
                                             'codemirror/addon/hint/sql-hint',
                                             'codemirror/addon/hint/xml-hint',
                                             'codemirror/mode/groovy/groovy',
                                             'codemirror/addon/dialog/dialog'
        ]})
    }
} 
const module = new ModuleFactory();
export default module;

