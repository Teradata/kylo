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
    "urlParams":"jquery.urlParam"},
    shim: {
        "angular": {deps:["jquery"],exports: "angular"},
        'angularAria': ['angular'],
        'angularMessages': ['angular'],
        'angularAnimate': ['angular'],
        'angularMaterial': ['angular','angularAnimate','angularAria','angularMessages'],
        'angular-material-icons':['angular'],
        'ocLazyLoad':['angular'],
        'urlParams':['jquery'],
        'login-app':{deps:['ocLazyLoad','underscore','angularMaterial','jquery'], exports:'login-app'}
    },
    deps: ['login-app']
});

// 'cardLayout' :'js/card-layout/card-layout-directive'