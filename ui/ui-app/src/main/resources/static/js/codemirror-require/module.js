define(['angular','codemirror-require/module-name','kylo-utils/LazyLoadUtil', 'codemirror', 'angular-ui-codemirror'], function (angular,moduleName,lazyLoadUtil,CodeMirror) {
    window.CodeMirror = CodeMirror;
    var module = angular.module(moduleName, []);

    module.run(['$ocLazyLoad',function($ocLazyLoad){
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
    }])












    return module;

});

