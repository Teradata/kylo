import {Inject, NgModule} from "@angular/core";

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
    constructor() {
        // Load CodeMirror
        const $window: any = window;
        $window.CodeMirror = require('../../../../../../node_modules/codemirror/lib/codemirror');
    }
}
