var MODULE_OPERATIONS = "datalakeui.opsManager";

var app = angular.module(MODULE_OPERATIONS, [
    'nvd3',
    'ngTextTruncate',
    'angularUtils.directives.dirPagination',
    'templates.navigate-before.html', 'templates.navigate-first.html', 'templates.navigate-last.html', 'templates.navigate-next.html',
    'ngMaterial',
    'md.data.table',
    'ngMdIcons',
    'md.data.table',
    'ui.router',
    'datalakeui.common'
  ]);

app.config(function($mdThemingProvider, $mdIconProvider) {
    var thinkBigBlue = $mdThemingProvider.extendPalette('blue', {
        '500': '3483BA',
        '900':'2B6C9A'
    });

    // Register the new color palette map with the name <code>neonRed</code>
    $mdThemingProvider.definePalette('thinkBigBlue', thinkBigBlue);

    $mdThemingProvider.theme('default')
        .primaryPalette('thinkBigBlue', {
        'hue-2':'900'
    })
        .accentPalette('grey');


   //$mdIconProvider
       //.defaultIconSet('js/vendor/material-design-icons/fonts/Material-Design-Iconic-Font.svg');
    //.iconSet...
});
app.config(function($stateProvider,$urlRouterProvider) {
    $urlRouterProvider
        .otherwise('/');
    $stateProvider.state('home', {
        url:'/',
        views:{
            'content': {
                templateUrl: 'js/overview/overview.html'
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Overview'
        }
    }).state('jobs',{
        url:'/jobs',
        params: {
            tab: null
        },
        views: {
            'content': {
                templateUrl: 'js/jobs/jobs.html'
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Jobs'
        }
    }).state('alerts', {
        url: '/alerts',
        params: {
            tab: null
        },
        views: {
            'content': {
                templateUrl: 'js/alerts/alerts-table.html'
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: 'Jobs'
        }
    })
        .state('scheduler', {
        url:'/scheduler',
        views: {
            'content': {
                templateUrl: 'js/scheduler/scheduler.html'
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Scheduler'
        }
    }).state('job-details',{
        url:'/job-details/:executionId',
        params: {
            executionId: null
        },
        views: {
            'content': {
                templateUrl: 'js/jobs/job-details.html'
            }
        },
        data:{
            displayName:'Job Details'
        }
    }).state('feed-stats', {
        url: '/feed-stats/:feedName',
        params: {
            executionId: null
        },
        views: {
            'content': {
                templateUrl: 'js/feeds/feed-stats/feed-stats.html'
            }
        },
        data: {
            displayName: 'Job Details'
        }
    })
        .state('feed-details', {
        url:'/feed-details/:feedName',
        params: {
            feedName: null
        },
        views: {
            'content': {
                templateUrl: 'js/feeds/feed-details.html'
            }
        },
        data:{
            displayName:'Feed Details'
        }
    }).state('service-health',{
            url:'/service-health',
            views: {
                'content': {
                    templateUrl: 'js/service-health/service-health.html'
                }
            },
        data:{
            breadcrumbRoot:true,
            displayName:'Service Health'
        }
    }).state('service-details',{
        url:'/service-details/:serviceName',
        params: {
            serviceName: null
        },
        views: {
            'content': {
                templateUrl: 'js/service-health/service-detail.html'
            }
        },
        data:{
            displayName:'Service Details'
        }
    }).state('service-component-details',{
        url:'/service-details/:serviceName/:componentName',
        params: {
            serviceName: null,
            componentName:null
        },
        views: {
            'content': {
                templateUrl: 'js/service-health/service-component-detail.html'
            }
        },
        data:{
            displayName:'Service Component'
        }
    }).state('charts',{
        url:'/charts',
        views: {
            'content': {
                templateUrl: 'js/charts/charts.html'
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Charts'
        }
    })


});

angular.module('ngMdIcons').config(['ngMdIconServiceProvider',function(ngMdIconService) {

    ngMdIconService.addShape('directions_run','<path d="M0 0h24v24H0z" fill="none"/><path d="M13.49 5.48c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm-3.6 13.9l1-4.4 2.1 2v6h2v-7.5l-2.1-2 .6-3c1.3 1.5 3.3 2.5 5.5 2.5v-2c-1.9 0-3.5-1-4.3-2.4l-1-1.6c-.4-.6-1-1-1.7-1-.3 0-.5.1-.8.1l-5.2 2.2v4.7h2v-3.4l1.8-.7-1.6 8.1-4.9-1-.4 2 7 1.4z"/>')
    ngMdIconService.addShape('mood','<path d="M0 0h24v24H0z" fill="none"/><path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm3.5-9c.83 0 1.5-.67 1.5-1.5S16.33 8 15.5 8 14 8.67 14 9.5s.67 1.5 1.5 1.5zm-7 0c.83 0 1.5-.67 1.5-1.5S9.33 8 8.5 8 7 8.67 7 9.5 7.67 11 8.5 11zm3.5 6.5c2.33 0 4.31-1.46 5.11-3.5H6.89c.8 2.04 2.78 3.5 5.11 3.5z"/>');
    ngMdIconService.addShape('mood_bad','<path d="M0 0h24v24H0V0z" fill="none"/><path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm3.5-9c.83 0 1.5-.67 1.5-1.5S16.33 8 15.5 8 14 8.67 14 9.5s.67 1.5 1.5 1.5zm-7 0c.83 0 1.5-.67 1.5-1.5S9.33 8 8.5 8 7 8.67 7 9.5 7.67 11 8.5 11zm3.5 3c-2.33 0-4.31 1.46-5.11 3.5h10.22c-.8-2.04-2.78-3.5-5.11-3.5z"/>');
    ngMdIconService.addShape('pan_tool',' <defs><path d="M0 0h24v24H0z" id="a"/></defs><clipPath id="b"><use overflow="visible" xlink:href="#a"/></clipPath><path clip-path="url(#b)" d="M23 5.5V20c0 2.2-1.8 4-4 4h-7.3c-1.08 0-2.1-.43-2.85-1.19L1 14.83s1.26-1.23 1.3-1.25c.22-.19.49-.29.79-.29.22 0 .42.06.6.16.04.01 4.31 2.46 4.31 2.46V4c0-.83.67-1.5 1.5-1.5S11 3.17 11 4v7h1V1.5c0-.83.67-1.5 1.5-1.5S15 .67 15 1.5V11h1V2.5c0-.83.67-1.5 1.5-1.5s1.5.67 1.5 1.5V11h1V5.5c0-.83.67-1.5 1.5-1.5s1.5.67 1.5 1.5z"/>');
    ngMdIconService.addShape('error_outline','<path d="M0 0h24v24H0V0z" fill="none"/><path d="M11 15h2v2h-2zm0-8h2v6h-2zm.99-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z"/>');
    ngMdIconService.addShape('vector_triangle','<path d="M9,3V9H9.73L5.79,16H2V22H8V20H16V22H22V16H18.21L14.27,9H15V3M11,5H13V7H11M12,9.04L16,16.15V18H8V16.15M4,18H6V20H4M18,18H20V20H18" />')
}]);



angular.module('templates.arrow.html', []).run(['$templateCache', function($templateCache) {
    'use strict';
    $templateCache.put('templates.arrow.html',
        '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"><path d="M3,9 L4.06,10.06 L8.25,5.87 L8.25,15 L9.75,15 L9.75,5.87 L13.94,10.06 L15,9 L9,3 L3,9 L3,9 Z"/></svg>');
}]);

angular.module('templates.navigate-before.html', []).run(['$templateCache', function($templateCache) {
    'use strict';
    $templateCache.put('templates.navigate-before.html',
        '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>\n' +
        '');
}]);

angular.module('templates.navigate-first.html', []).run(['$templateCache', function($templateCache) {
    'use strict';
    $templateCache.put('templates.navigate-first.html',
        '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M7 6 v12 h2 v-12 h-2z M17.41 7.41L16 6l-6 6 6 6 1.41-1.41L12.83 12z"/></svg>\n' +
        '');
}]);

angular.module('templates.navigate-last.html', []).run(['$templateCache', function($templateCache) {
    'use strict';
    $templateCache.put('templates.navigate-last.html',
        '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M15 6 v12 h2 v-12 h-2z M8 6L6.59 7.41 11.17 12l-4.58 4.59L8 18l6-6z"/></svg>\n' +
        '');
}]);

angular.module('templates.navigate-next.html', []).run(['$templateCache', function($templateCache) {
    'use strict';
    $templateCache.put('templates.navigate-next.html',
        '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/></svg>\n' +
        '');
}]);


app.run(
    [          '$rootScope', '$state', '$stateParams',
        function ($rootScope,   $state,   $stateParams) {

            // It's very handy to add references to $state and $stateParams to the $rootScope
            // so that you can access them from any scope within your applications.For example,
            // <li ng-class="{ active: $state.includes('contacts.list') }"> will set the <li>
            // to active whenever 'contacts.list' or one of its decendents is active.
            $rootScope.$state = $state;
            $rootScope.$stateParams = $stateParams;
        }
    ]
)