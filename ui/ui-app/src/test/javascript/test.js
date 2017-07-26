require.config({
    waitSeconds: 0,
    baseUrl: "js",
    paths: {
        "angular": "../bower_components/angular/angular.min",
        "angular-drag-and-drop-lists": "../bower_components/angular-drag-and-drop-lists/angular-drag-and-drop-lists.min",
        "angular-material-data-table": "../bower_components/angular-material-data-table/dist/md-data-table.min",
        "angular-material-expansion-panel": "../bower_components/angular-material-expansion-panel/dist/md-expansion-panel.min",
        "angular-material-icons": "vendor/angular-material-icons/angular-material-icons",
        "angularMocks": "../spec/vendor/angular/angular-mocks",
        "angular-nvd3": "../bower_components/angular-nvd3/dist/angular-nvd3.min",
        "angular-sanitize": "../bower_components/angular-sanitize/angular-sanitize.min",
        "angular-ui-codemirror": "../bower_components/angular-ui-codemirror/ui-codemirror.min",
        "angular-ui-grid": "../bower_components/angular-ui-grid/ui-grid.min",
        "angular-ui-router": "../bower_components/angular-ui-router/release/angular-ui-router.min",
        "angular-visjs": "../bower_components/angular-visjs/angular-vis",
        "angularAnimate": "../bower_components/angular-animate/angular-animate.min",
        "angularAria": "../bower_components/angular-aria/angular-aria.min",
        "angularLocalStorage": "../bower_components/angularLocalStorage/dist/angularLocalStorage.min",
        "angularMaterial": "../bower_components/angular-material/angular-material.min",
        "angularMessages": "../bower_components/angular-messages/angular-messages.min",
        "app": "app",
        "c3": "../bower_components/c3/c3.min",
        "cardLayout": "common/card-layout/card-layout",
        "d3": "../bower_components/d3/d3.min",
        "dirPagination": "vendor/dirPagination/dirPagination",
        "draggabilly": "../bower_components/draggabilly/dist/draggabilly.pkgd.min",
        "fattable": "../bower_components/fattable/fattable",
        "gsap": "../bower_components/gsap/src/uncompressed/TweenMax",
        "jquery": "../bower_components/jquery/dist/jquery.min",
        "jquery-ui": "../bower_components/jquery-ui/jquery-ui.min",
        "kylo-common": "common/module-require",
        "kylo-common-module": "common/module",
        "kylo-feedmgr": "feed-mgr/module-require",
        "kylo-feedmgr-module": "feed-mgr/module",
        "kylo-opsmgr": "ops-mgr/module-require",
        "kylo-opsmgr-module": "ops-mgr/module",
        "kylo-services": "services/module-require",
        "kylo-services-module": "services/module",
        "kylo-side-nav": "side-nav/module-require",
        "kylo-side-nav-module": "side-nav/module",
        "kyloTimer": "common/timer/kylo-timer",
        "lz-string": "../bower_components/lz-string/libs/lz-string.min",
        "ment-io": "vendor/ment.io/mentio",
        "moment": "../bower_components/moment/min/moment.min",
        'ng-fx': "../bower_components/ngFx/dist/ngFx.min",
        "ng-text-truncate": "vendor/ng-text-truncate/ng-text-truncate",
        "nvd3": "../bower_components/nvd3/build/nv.d3.min",
        "ocLazyLoad": "../bower_components/oclazyload/dist/ocLazyLoad.require.min",
        "pivottable": "../bower_components/pivottable/dist/pivot.min",
        "pivottable-c3-renderers": "../bower_components/pivottable/dist/c3_renderers.min",
        "requirejs": "../bower_components/requirejs/require",
        "routes": "routes",
        "svg-morpheus": "../bower_components/svg-morpheus/compile/unminified/svg-morpheus",
        "underscore": "../bower_components/underscore/underscore-min",
        "vis": "../bower_components/vis/dist/vis.min"
    },
    packages: [{
        name: "codemirror",
        location: "../bower_components/codemirror",
        main: "lib/codemirror"
    }],
    shim: {
        "angular": {deps: ["jquery"], exports: "angular"},
        "angular-drag-and-drop-lists": ["angular"],
        'angular-ui-router': ['angular'],
        'angularAria': ['angular'],
        'angularMessages': ['angular'],
        'angularAnimate': ['angular'],
        'angularMaterial': ['angular', 'angularAnimate', 'angularAria', 'angularMessages'],
        'angular-material-expansion-panel': ['angular'],
        'angular-material-icons': ['angular'],
        'angular-material-data-table': ['angular'],
        "angularMocks": {deps: ["angular"], "exports": "angular.mock"},
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
        'd3': {exports: 'd3'},
        'dirPagination': ['angular'],
        "jquery-ui": ["jquery"],
        'ocLazyLoad': ['angular'],
        'kylo-services-module': ['angular', 'jquery'],
        'kylo-services': ['angular', 'kylo-services-module', 'jquery'],
        'kylo-common-module': ['angular', 'jquery'],
        'kylo-common': {deps: ['angular', 'kylo-services', 'kylo-common-module', 'jquery', 'angular-material-icons'], exports: 'kylo-common'},
        'kylo-feedmgr-module': ['angular', 'jquery'],
        'kylo-feedmgr': ['angular', 'kylo-services', 'kylo-common', 'kylo-feedmgr-module'],
        'kylo-opsmgr-module': ['angular', 'jquery'],
        'kylo-opsmgr': ['angular', 'kylo-services', 'kylo-common', 'kylo-opsmgr-module'],
        'kylo-side-nav-module': ['angular', 'jquery'],
        'kylo-side-nav': {deps: ['angular', 'kylo-services', 'jquery', 'angular-material-icons', 'kylo-side-nav-module'], exports: 'kylo-side-nav'},
        'ment-io': ['angular'],
        "ng-fx": {deps: ["gsap"]},
        "ng-text-truncate": ["angular"],
        'nvd3': {deps: ['d3'], exports: 'nv'},
        'pivottable': {deps: ['c3', 'jquery']},
        'vis': {exports: "vis"},
        'app': {deps: ['ocLazyLoad', 'underscore', 'angularMaterial', 'jquery', 'angular-sanitize', 'ng-text-truncate'], exports: 'app'},
        'routes': {deps: ['app'], exports: 'routes'},

        "feed-mgr/module-require": ["feed-mgr/module"],
        "feed-mgr/visual-query/module-require": ["feed-mgr/visual-query/module"]
    },
    deps: ["routes", "../spec/common/utils/StringUtils.spec.js",
           // "../spec/feed-mgr/services/DomainTypesServiceTest",
           "../spec/feed-mgr/visual-query/services/SparkDatasourceServiceTest",
           "../spec/feed-mgr/visual-query/services/SparkShellServiceTest",
           "../spec/feed-mgr/visual-query/VisualQueryBuilderControllerTest",
           "../spec/feed-mgr/visual-query/VisualQueryColumnDelegateTest"],
    callback: jasmine.boot
});

/*-
 * #%L
 * kylo-ui-app
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
