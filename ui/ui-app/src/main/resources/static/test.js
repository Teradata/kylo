// This file is required by karma.conf.js and loads recursively all the .spec and framework files
// "No stacktrace"" is usually best for app testing.
// (Error as any).stackTraceLimit = 0;
// Uncomment to get full stacktrace output. Sometimes helpful, usually not.
// Error.stackTraceLimit = Infinity; //
jasmine.DEFAULT_TIMEOUT_INTERVAL = 1000;
// builtPaths: root paths for output ("built") files
// get from karma.config.js, then prefix with '/base/' (default is 'src/')
var builtPaths = (__karma__.config.builtPaths || ['src/'])
    .map(function (p) { return '/base/' + p; });
// Prevent Karma from running prematurely.
__karma__.loaded = function () { };
function isJsFile(path) {
    return path.slice(-3) === '.js';
}
function isSpecFile(path) {
    return /\.spec\.(.*\.)?js$/.test(path);
}
// Is a "built" file if is JavaScript file in one of the "built" folders
function isBuiltFile(path) {
    return isJsFile(path) &&
        builtPaths.reduce(function (keep, bp) {
            return keep || (path.substr(0, bp.length) === bp);
        }, false);
}
var allSpecFiles = Object.keys(window.__karma__.files)
    .filter(isSpecFile)
    .filter(isBuiltFile);
System.config({
    // Base URL for System.js calls. 'base/' is where Karma serves files from.
    baseURL: 'base/src/main/resources/static/js',
    defaultJSExtensions: true,
    // Extend usual application package list with test folder
    packages: { 'testing': { main: 'index.js', defaultExtension: 'js' } },
    paths: {
        "angular-material-icons": "vendor/angular-material-icons/angular-material-icons",
        "bower:": "/base/src/main/resources/static/bower_components/",
        "dirPagination": "vendor/dirPagination/dirPagination",
        "kylo-common": "common/module-require",
        "kylo-common-module": "common/module",
        "kylo-feedmgr": "feed-mgr/module-require",
        "kylo-feedmgr-module": "feed-mgr/module",
        "kylo-services": "services/module-require",
        "kylo-services-module": "services/module",
        "kylo-side-nav": "side-nav/module-require",
        "kylo-side-nav-module": "side-nav/module",
        "npm:": "/base/node_modules/",
        "ng-text-truncate": "vendor/ng-text-truncate/ng-text-truncate",
    },
    // Assume npm: is set in `paths` in systemjs.config
    // Map the angular testing umd bundles
    map: {
        '@angular/animations': 'npm:@angular/animations/bundles/animations.umd.min',
        '@angular/animations/browser': 'npm:@angular/animations/bundles/animations-browser.umd.min',
        '@angular/cdk': 'npm:@angular/cdk/bundles/cdk.umd.js',
        '@angular/cdk/a11y': 'npm:@angular/cdk/bundles/cdk-a11y.umd.js',
        '@angular/cdk/accordion': 'npm:@angular/cdk/bundles/cdk-accordion.umd.js',
        '@angular/cdk/bidi': 'npm:@angular/cdk/bundles/cdk-bidi.umd.js',
        '@angular/cdk/coercion': 'npm:@angular/cdk/bundles/cdk-coercion.umd.js',
        '@angular/cdk/collections': 'npm:@angular/cdk/bundles/cdk-collections.umd.js',
        '@angular/cdk/keycodes': 'npm:@angular/cdk/bundles/cdk-keycodes.umd.js',
        '@angular/cdk/layout': 'npm:@angular/cdk/bundles/cdk-layout.umd.js',
        '@angular/cdk/observers': 'npm:@angular/cdk/bundles/cdk-observers.umd.js',
        '@angular/cdk/overlay': 'npm:@angular/cdk/bundles/cdk-overlay.umd.js',
        '@angular/cdk/platform': 'npm:@angular/cdk/bundles/cdk-platform.umd.js',
        '@angular/cdk/portal': 'npm:@angular/cdk/bundles/cdk-portal.umd.js',
        '@angular/cdk/scrolling': 'npm:@angular/cdk/bundles/cdk-scrolling.umd.js',
        '@angular/cdk/stepper': 'npm:@angular/cdk/bundles/cdk-stepper.umd.js',
        '@angular/cdk/table': 'npm:@angular/cdk/bundles/cdk-table.umd.js',
        '@angular/common': 'npm:@angular/common/bundles/common.umd.min',
        '@angular/common/http': 'npm:@angular/common/bundles/common-http.umd.min',
        '@angular/common/testing': 'npm:@angular/common/bundles/common-testing.umd.js',
        '@angular/compiler': 'npm:@angular/compiler/bundles/compiler.umd.min',
        '@angular/compiler/testing': 'npm:@angular/compiler/bundles/compiler-testing.umd.js',
        '@angular/core': 'npm:@angular/core/bundles/core.umd.min',
        '@angular/core/testing': 'npm:@angular/core/bundles/core-testing.umd.js',
        '@angular/forms': 'npm:@angular/forms/bundles/forms.umd.min',
        '@angular/forms/testing': 'npm:@angular/forms/bundles/forms-testing.umd.js',
        '@angular/http': 'npm:@angular/http/bundles/http.umd.min',
        '@angular/http/testing': 'npm:@angular/http/bundles/http-testing.umd.js',
        '@angular/material': 'npm:@angular/material/bundles/material.umd.min',
        '@angular/material/autocomplete': 'npm:@angular/material/bundles/material-autocomplete.umd.min',
        '@angular/material/button': 'npm:@angular/material/bundles/material-button.umd.min',
        '@angular/material/button-toggle': 'npm:@angular/material/bundles/material-button-toggle.umd.min',
        '@angular/material/card': 'npm:@angular/material/bundles/material-card.umd.min',
        '@angular/material/checkbox': 'npm:@angular/material/bundles/material-checkbox.umd.min',
        '@angular/material/chips': 'npm:@angular/material/bundles/material-chips.umd.min',
        '@angular/material/core': 'npm:@angular/material/bundles/material-core.umd.min',
        '@angular/material/datepicker': 'npm:@angular/material/bundles/material-datepicker.umd.min',
        '@angular/material/dialog': 'npm:@angular/material/bundles/material-dialog.umd.min',
        '@angular/material/expansion': 'npm:@angular/material/bundles/material-expansion.umd.min',
        '@angular/material/form-field': 'npm:@angular/material/bundles/material-form-field.umd.min',
        '@angular/material/grid-list': 'npm:@angular/material/bundles/material-grid-list.umd.min',
        '@angular/material/icon': 'npm:@angular/material/bundles/material-icon.umd.min',
        '@angular/material/input': 'npm:@angular/material/bundles/material-input.umd.min',
        '@angular/material/list': 'npm:@angular/material/bundles/material-list.umd.min',
        '@angular/material/menu': 'npm:@angular/material/bundles/material-menu.umd.min',
        '@angular/material/paginator': 'npm:@angular/material/bundles/material-paginator.umd.min',
        '@angular/material/progress-bar': 'npm:@angular/material/bundles/material-progress-bar.umd.min',
        '@angular/material/progress-spinner': 'npm:@angular/material/bundles/material-progress-spinner.umd.min',
        '@angular/material/radio': 'npm:@angular/material/bundles/material-radio.umd.min',
        '@angular/material/select': 'npm:@angular/material/bundles/material-select.umd.min',
        '@angular/material/sidenav': 'npm:@angular/material/bundles/material-sidenav.umd.min',
        '@angular/material/slide-toggle': 'npm:@angular/material/bundles/material-slide-toggle.umd.min',
        '@angular/material/slider': 'npm:@angular/material/bundles/material-slider.umd.min',
        '@angular/material/snack-bar': 'npm:@angular/material/bundles/material-snack-bar.umd.min',
        '@angular/material/sort': 'npm:@angular/material/bundles/material-sort.umd.min',
        '@angular/material/stepper': 'npm:@angular/material/bundles/material-stepper.umd.min',
        '@angular/material/table': 'npm:@angular/material/bundles/material-table.umd.min',
        '@angular/material/tabs': 'npm:@angular/material/bundles/material-tabs.umd.min',
        '@angular/material/toolbar': 'npm:@angular/material/bundles/material-toolbar.umd.min',
        '@angular/material/tooltip': 'npm:@angular/material/bundles/material-tooltip.umd.min',
        '@angular/platform-browser': 'npm:@angular/platform-browser/bundles/platform-browser.umd.min',
        '@angular/platform-browser/animations': 'npm:@angular/platform-browser/bundles/platform-browser-animations.umd.min',
        '@angular/platform-browser/testing': 'npm:@angular/platform-browser/bundles/platform-browser-testing.umd.js',
        '@angular/platform-browser-dynamic': 'npm:@angular/platform-browser-dynamic/bundles/platform-browser-dynamic.umd.min',
        '@angular/platform-browser-dynamic/testing': 'npm:@angular/platform-browser-dynamic/bundles/platform-browser-dynamic-testing.umd.js',
        '@angular/router': 'npm:@angular/router/bundles/router.umd.min',
        '@angular/router/testing': 'npm:@angular/router/bundles/router-testing.umd.js',
        '@angular/router/upgrade': 'npm:@angular/router/bundles/router-upgrade.umd.min',
        '@angular/upgrade': 'npm:@angular/upgrade/bundles/upgrade.umd',
        '@angular/upgrade/static': 'npm:@angular/upgrade/bundles/upgrade-static.umd',
        '@covalent/core': 'npm:@covalent/core/bundles/core.umd',
        '@kylo/wrangler': 'feed-mgr/visual-query/wrangler/index',
        '@kylo/wrangler/api': 'feed-mgr/visual-query/wrangler/api/index',
        '@uirouter/angular': 'npm:@uirouter/angular/_bundles/ui-router-ng2',
        '@uirouter/angular-hybrid': 'npm:@uirouter/angular-hybrid/_bundles/ui-router-angular-hybrid',
        '@uirouter/angularjs': 'npm:@uirouter/angularjs/release/angular-ui-router',
        '@uirouter/core': 'npm:@uirouter/core/_bundles/ui-router-core',
        '@uirouter/rx': 'npm:@uirouter/rx/_bundles/ui-router-rx',
        "angular": "bower:angular/angular.min",
        "angular-drag-and-drop-lists": "bower:angular-drag-and-drop-lists/angular-drag-and-drop-lists.min",
        "angular-material-data-table": "bower:angular-material-data-table/dist/md-data-table.min",
        "angular-material-expansion-panel": "bower:angular-material-expansion-panel/dist/md-expansion-panel.min",
        'angular-mocks': 'npm:angular-mocks/angular-mocks',
        "angular-nvd3": "bower:angular-nvd3/dist/angular-nvd3.min",
        "angular-sanitize": "bower:angular-sanitize/angular-sanitize.min",
        "angular-ui-codemirror": "bower:angular-ui-codemirror/ui-codemirror.min",
        "angular-ui-grid": "bower:angular-ui-grid/ui-grid.min",
        "angular-ui-router": "bower:angular-ui-router/release/angular-ui-router",
        "angular-visjs": "bower:angular-visjs/angular-vis",
        "angularAnimate": "bower:angular-animate/angular-animate.min",
        "angularAria": "bower:angular-aria/angular-aria.min",
        "angularLocalStorage": "bower:angularLocalStorage/dist/angularLocalStorage.min",
        "angularMaterial": "bower:angular-material/angular-material.min",
        "angularMessages": "bower:angular-messages/angular-messages.min",
        "c3": "bower:c3/c3.min",
        "codemirror": "bower:codemirror/lib/codemirror",
        "codemirror/addon/": "bower:codemirror/addon/",
        "codemirror/mode/": "bower:codemirror/mode/",
        "d3": "bower:d3/d3.min",
        "draggabilly": "bower:draggabilly/dist/draggabilly.pkgd.min",
        "fattable": "bower:fattable/fattable",
        "gsap": "bower:gsap/src/uncompressed/TweenMax",
        "jquery": "bower:jquery/dist/jquery.min",
        "jquery-ui": "bower:jquery-ui/jquery-ui.min",
        "lz-string": "bower:lz-string/libs/lz-string.min",
        "moment": "bower:moment/min/moment.min",
        'ng-fx': "bower:ngFx/dist/ngFx.min",
        "nvd3": "bower:nvd3/build/nv.d3.min",
        "ocLazyLoad": "bower:oclazyload/dist/ocLazyLoad.require",
        "pivottable": "bower:pivottable/dist/pivot.min",
        "pivottable-c3-renderers": "bower:pivottable/dist/c3_renderers.min",
        "requirejs": "bower:requirejs/require",
        'rxjs': 'npm:rxjs',
        "svg-morpheus": "bower:svg-morpheus/compile/unminified/svg-morpheus",
        'tslib': 'npm:tslib/tslib',
        "ui-grid": "angular-ui-grid",
        "underscore": "bower:underscore/underscore-min",
        "vis": "bower:vis/dist/vis.min",
        "mdPickers": "bower:mdPickers/dist/mdPickers.min"
    },
    meta: {
        "@angular/core": { deps: ["angular"] },
        "angular": { deps: ["jquery"], exports: "angular" },
        'angular-ui-router': { deps: ['angular'] },
        'angularAria': ['angular'],
        'angularMessages': ['angular'],
        'angularAnimate': ['angular'],
        'angularMaterial': ['angular', 'angularAnimate', 'angularAria', 'angularMessages'],
        'angular-material-expansion-panel': ['angular'],
        'angular-material-icons': ['angular'],
        'angular-material-data-table': ['angular'],
        "angular-nvd3": ['angular', 'nvd3'],
        "angular-sanitize": ["angular"],
        'angular-ui-grid': ['angular', 'angularAnimate'],
        'angular-ui-codemirror': ['angular', 'codemirror'],
        'angular-visjs': ['angular', 'vis'],
        "codemirror-pig": ["codemirror"],
        "codemirror-properties": ["codemirror"],
        "codemirror-python": ["codemirror"],
        "codemirror-xml": ["codemirror"],
        "codemirror-shell": ["codemirror"],
        "codemirror-javascript": ["codemirror"],
        "codemirror-sql": ["codemirror"],
        "codemirror-show-hint": ["codemirror"],
        "codemirror-sql-hint": ["codemirror"],
        "codemirror-xml-hint": ["codemirror"],
        "codemirror-groovy": ["codemirror"],
        "codemirror-dialog": ["codemirror"],
        'd3': { exports: 'd3' },
        'dirPagination': ['angular'],
        "jquery-ui": ["jquery"],
        'ocLazyLoad': ['angular'],
        'kylo-services-module': { deps: ['angular', 'jquery'] },
        'kylo-services': { deps: ['angular', 'kylo-services-module', 'jquery'] },
        'kylo-common-module': { deps: ['angular', 'jquery'] },
        'kylo-common': { deps: ['angular', 'kylo-services', 'kylo-common-module', 'jquery', 'angular-material-icons'], exports: 'kylo-common', format: "amd" },
        'kylo-feedmgr-module': { deps: ['angular', 'jquery'] },
        'kylo-feedmgr': { deps: ['angular', 'kylo-services', 'kylo-common', 'kylo-feedmgr-module'] },
        'kylo-opsmgr-module': { deps: ['angular', 'jquery'] },
        'kylo-opsmgr': { deps: ['angular', 'kylo-services', 'kylo-common', 'kylo-opsmgr-module'] },
        'kylo-side-nav-module': { deps: ['angular', 'jquery'] },
        'kylo-side-nav': { deps: ['angular', 'kylo-services', 'jquery', 'angular-material-icons', 'kylo-side-nav-module'], exports: 'kylo-side-nav', format: "amd" },
        'ment-io': ['angular'],
        "ng-fx": { deps: ["gsap"] },
        "ng-text-truncate": ["angular"],
        'nvd3': { deps: ['d3'], exports: 'nv' },
        'pivottable': { deps: ['c3', 'jquery'] },
        "pivottable-c3-renderers": { deps: ['pivottable'] },
        'vis': { exports: "vis" },
        'app': { deps: ['ocLazyLoad', 'underscore', 'angularMaterial', 'jquery', 'angular-sanitize', 'ng-text-truncate'], exports: 'app', format: "amd" },
        'routes': { deps: ['app'], exports: 'routes', format: "amd" }
    }
});
initTestBed().then(initTesting);
function initTestBed() {
    return Promise.all([
        System.import('@angular/core/testing'),
        System.import('@angular/platform-browser-dynamic/testing'),
        System.import('@uirouter/angular-hybrid'),
        System.import('@uirouter/core')
    ])
        .then(function (providers) {
        var coreTesting = providers[0];
        var browserTesting = providers[1];
        var uiRouter = providers[3];
        // Fix @uirouter/core unable to load
        uiRouter.servicesPlugin(null);
        // Load test environment
        coreTesting.TestBed.initTestEnvironment(browserTesting.BrowserDynamicTestingModule, browserTesting.platformBrowserDynamicTesting());
    });
}
// Import all spec files and start karma
function initTesting() {
    return System.import('routes')
        .then(function () { return Promise.all(allSpecFiles.map(function (moduleName) { return System.import(moduleName); })); })
        .then(__karma__.start, __karma__.error);
}
//# sourceMappingURL=test.js.map