require.config({
    waitSeconds: 0,
    urlArgs: "ver=0.8.1",
    baseUrl: "login",
    paths: {
        "angular":"../bower_components/angular/angular",
        "angular-material-icons":"../bower_components/angular-material-icons/angular-material-icons",
        "angularAnimate":"../bower_components/angular-animate/angular-animate",
        "angularAria":"../bower_components/angular-aria/angular-aria",
        "angularLocalStorage": "../bower_components/angularLocalStorage/dist/angularLocalStorage.min",
        "angularMaterial":"../bower_components/angular-material/angular-material",
        "angularMessages":"../bower_components/angular-messages/angular-messages",
        "angularCookies": "../bower_components/angular-cookies/angular-cookies",
        "login-app":"login-app",
        "jquery":"../bower_components/jquery/dist/jquery",
        "lz-string": "../bower_components/lz-string/libs/lz-string.min",
        "ocLazyLoad":"../bower_components/oclazyload/dist/ocLazyLoad.require",
        "requirejs": "../bower_components/requirejs/require",
        "underscore":"../bower_components/underscore/underscore",
        "urlParams":"jquery.urlParam",
        "pascalprecht.translate":"../bower_components/angular-translate/angular-translate",
        "angular-translate-loader-static-files": "../bower_components/angular-translate-loader-static-files/angular-translate-loader-static-files.min",
        "angular-translate-storage-local": "../bower_components/angular-translate-storage-local/angular-translate-storage-local.min",
        "angular-translate-handler-log": "../bower_components/angular-translate-handler-log/angular-translate-handler-log.min",
        "angular-translate-storage-cookie": "../bower_components/angular-translate-storage-cookie/angular-translate-storage-cookie.min"
},
    shim: {
        "angular": {deps:["jquery"],exports: "angular"},
        'angularAria': ['angular'],
        'angularMessages': ['angular'],
        'angularAnimate': ['angular'],
        'angularMaterial': ['angular','angularAnimate','angularAria','angularMessages'],
        'angular-material-icons':['angular'],
        'ocLazyLoad':['angular'],
        'urlParams':['jquery'],
        'pascalprecht.translate':['angular'],
        'angular-translate-loader-static-files':['pascalprecht.translate'],
        'angular-translate-storage-local': {deps: ['pascalprecht.translate']},
        'angular-translate-handler-log': {deps: ['pascalprecht.translate']},
        'angular-translate-storage-cookie': {deps: ['pascalprecht.translate']},

        'login-app':{deps:['ocLazyLoad','underscore','angularMaterial','jquery'], exports:'login-app'}
    },
    deps: ['login-app']
});

// 'cardLayout' :'js/card-layout/card-layout-directive'
