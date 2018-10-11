import * as angular from 'angular';
import "../kylo-utils/LazyLoadUtil";
const CodeMirror = require('../../../../../../node_modules/codemirror/lib/codemirror');
import "../../bower_components/angular-ui-codemirror/ui-codemirror.min.js";
import {moduleName} from "./module-name";

// import '../../js/vendor/tern/lib/tern';
//require('../../js/vendor/tern/lib/tern');

require('../../../../../../node_modules/codemirror/mode/pig/pig');
require('../../../../../../node_modules/codemirror/mode/properties/properties');
require('../../../../../../node_modules/codemirror/mode/python/python');
require('../../../../../../node_modules/codemirror/mode/velocity/velocity');
require('../../../../../../node_modules/codemirror/mode/xml/xml');
require('../../../../../../node_modules/codemirror/mode/shell/shell');
require('../../../../../../node_modules/codemirror/mode/javascript/javascript');
require('../../../../../../node_modules/codemirror/mode/sql/sql');
require('../../../../../../node_modules/codemirror/addon/tern/tern');
require('../../../../../../node_modules/codemirror/addon/hint/show-hint');
require('../../../../../../node_modules/codemirror/addon/hint/sql-hint');
require('../../../../../../node_modules/codemirror/addon/hint/xml-hint');
require('../../../../../../node_modules/codemirror/mode/groovy/groovy');
require('../../../../../../node_modules/codemirror/addon/dialog/dialog');

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
       (<any>window).CodeMirror = CodeMirror;
        this.module = angular.module(moduleName,['ui.codemirror']);

        // require('../../js/vendor/tern/lib/tern');

        // this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);
    }

    // runFn($ocLazyLoad: oc.ILazyLoad) {
    //     $ocLazyLoad.load({
    //         name: 'kylo', files: [
        //     ]
        // })
    // }
} 
const module = new ModuleFactory();
export default module;

