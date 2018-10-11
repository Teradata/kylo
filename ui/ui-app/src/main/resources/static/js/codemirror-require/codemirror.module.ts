import {Inject, NgModule} from "@angular/core";
import {ILazyLoad} from "ocLazyLoad";

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

@NgModule()
export class KyloCodeMirrorModule {
    constructor(@Inject("$ocLazyLoad") $ocLazyLoad: ILazyLoad) {
        // Load CodeMirror
        const $window: any = window;
        // if ($window.CodeMirror == null) {
            $window.CodeMirror = require('../../../../../../node_modules/codemirror/lib/codemirror');
            // require('../../js/vendor/tern/lib/tern');
        // }

        // Load CodeMirror plugins
        // $ocLazyLoad.load([
        //     '../../bower_components/codemirror/mode/pig/pig',
        //     '../../bower_components/codemirror/mode/properties/properties',
        //     '../../bower_components/codemirror/mode/python/python',
        //     '../../bower_components/codemirror/mode/velocity/velocity',
        //     '../../bower_components/codemirror/mode/xml/xml',
        //     '../../bower_components/codemirror/mode/shell/shell',
        //     '../../bower_components/codemirror/mode/javascript/javascript',
        //     '../../bower_components/codemirror/mode/sql/sql',
        //     '../../bower_components/codemirror/addon/tern/tern',
        //     '../../bower_components/codemirror/addon/hint/show-hint',
        //     '../../bower_components/codemirror/addon/hint/sql-hint',
        //     '../../bower_components/codemirror/addon/hint/xml-hint',
        //     '../../bower_components/codemirror/mode/groovy/groovy',
        //     '../../bower_components/codemirror/addon/dialog/dialog']);
    }
}
