var MODULE_FEED_MGR = "datalakeui.feedmgr";


var app = angular.module(MODULE_FEED_MGR, [
    'nvd3',
    'ngMaterial',
    'md.data.table',
    'ngMdIcons',
    'ngSanitize',
    'ui.router',
    'mentio',
    'ui.codemirror',
    'ngMessages',
    'angularUtils.directives.dirPagination',
    'ngCookies',
    'flowChart',
    'ui.grid',
    'ui.grid.resizeColumns',
    'ui.grid.autoResize',
    'ui.grid.moveColumns',
    'ui.grid.pagination',
    'templates.navigate-before.html', 'templates.navigate-first.html', 'templates.navigate-last.html', 'templates.navigate-next.html',
    'datalakeui.common'
    ]);

app.config(function($mdThemingProvider, $mdIconProvider) {
    var thinkBigBlue = $mdThemingProvider.extendPalette('blue', {
        '500': '3483BA',
        '900':'2B6C9A'
    });

    var accentOrange = $mdThemingProvider.extendPalette('orange', {
        'A200': 'F08C38'
    });

    // Register the new color palette map with the name <code>neonRed</code>
    $mdThemingProvider.definePalette('thinkBigBlue', thinkBigBlue);
    $mdThemingProvider.definePalette('accentOrange', accentOrange);

    $mdThemingProvider.theme('default')
        .primaryPalette('thinkBigBlue', {
        'hue-2':'900'
    })
        .accentPalette('accentOrange');

/*
    $mdThemingProvider.theme('default')
        .primaryPalette('blue')
        .accentPalette('orange');
*/
   //$mdIconProvider
       //.defaultIconSet('js/vendor/material-design-icons/fonts/Material-Design-Iconic-Font.svg');
    //.iconSet...
});
app.config(function($stateProvider,$urlRouterProvider) {
    // Redirect to /home for any unmatched url
    $urlRouterProvider.otherwise("/");

    // Set url states
    $stateProvider.state("home", {
        url: "/",
        views: {
            "content": {
                templateUrl: "js/main/home.html"
            }
        },
        data:{
            breadcrumbRoot: true,
            displayName: "Home"
        }
    }).state('registered-templates',{
        url:'/registered-templates',
        params: {
        },
        views: {
            'content': {
                templateUrl: 'js/register-template/registered-templates.html'
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Registered Templates'
        }
    }).state('register-template',{
        url:'/register-template',
        views: {
            'content': {
                templateUrl: 'js/register-template/register-template.html'
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Register Template'
        }
    }).state('import-template',{
        url:'/import-template',
        params: {
        },
        views: {
            'content': {
                templateUrl: 'js/register-template/import-template.html'
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Template Manager'
        }
    }).state('register-nifi-template',{
        url:'/register-nifi-template',
        params: {
            registeredTemplateId: null,
            nifiTemplateId:null
        },
        views: {
            'content': {
                templateUrl: 'js/register-template/register-nifi-template.html'
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Register Template'
        }
    }).state('define-feed',{
        url:'/define-feed',
        params: {
            templateId: null
        },
        views: {
            'content': {
                templateUrl: 'js/define-feed/define-feed.html'
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Define Feed'
        }
    }).state('import-feed',{
        url:'/import-feed',
        params: {
        },
        views: {
            'content': {
                templateUrl: 'js/define-feed/import-feed.html'
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Import Feed'
        }
    }).state('define-feed-complete',{
        url:'/define-feed-complete',
        params: {
            feedModel: null,
            error:null
        },
        views: {
            'content': {
                templateUrl: 'js/define-feed/feed-details/define-feed-complete.html'
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Define Feed'
        }
    }).state('feeds',{
        url:'/feeds',
        params: {
            tab: null
        },
        views: {
            'content': {
                templateUrl: 'js/feeds/feeds-table.html'
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Feeds'
        }
    }).state('feed-details',{
        url:'/feed-details/{feedId}',
        params: {
            feedId: null,
            tabIndex: 0
        },
        views: {
            'content': {
                templateUrl: 'js/feed-details/feed-details.html'
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Edit Feed'
        }
    }).state('service-level-agreements',{
            url:'/service-level-agreements',
            params: {
            },
            views: {
                'content': {
                    templateUrl: 'js/sla/service-level-agreements-view.html'
                }
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Service Level Agreements'
            }
    }).state('categories',{
        url:'/categories',
        params: {
        },
        views: {
            'content': {
                templateUrl: 'js/categories/categories.html'
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Categories'
        }
    }).state('category-details',{
        url:'/category-details/{categoryId}',
        params: {
            categoryId:null
        },
        views: {
            'content': {
                templateUrl: 'js/categories/category-details.html'
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Category Details'
        }
    }).state('tables',{
        url:'/tables',
        params: {
        },
        views: {
            'content': {
                templateUrl: 'js/tables/tables.html'
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Tables'
        }
    }).state('table',{
        url:'/tables/{schema}/{tableName}',
        params: {
            schema:null,
            tableName:null
        },
        views: {
            'content': {
                templateUrl: 'js/tables/table.html'
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Table Details'
        }
    }).state('search',{
        url:'/search',
        params: {
        },
        views: {
            'content': {
                templateUrl: 'js/elastic-search/elastic-search.html'
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Search'
        }
    }).state('hql-editor',{
        url:'/hql-editor',
        params: {
        },
        views: {
            'content': {
                templateUrl: 'js/hql-editor/hql-editor.html'
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'HQL Editor'
        }
    }).state('visual-query',{
        url:'/visual-query',
        params: {
        },
        views: {
            'content': {
                templateUrl: 'js/visual-query/visual-query.html'
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Visual Query'
        }
    }).state('admin-modeshape-migration', {
        url: '/admin-modeshape-migration',
        params: {},
        views: {
            'content': {
                templateUrl: 'js/admin/modeshape-migration.html'
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: 'Modeshape Migration'
        }
    }).state('edit-feed',{
        url:'/edit-feed/{feedId}',
        params: {
            feedId: null
        },
        views: {
            'content': {
                templateUrl: 'js/edit-feed/edit-feed.html'
            }
        },
        data:{
            breadcrumbRoot: false,
            displayName: 'Edit Feed'
        }
    }).state('business-metadata', {
        url:'/business-metadata',
        params: {},
        views: {
            'content': {
                templateUrl: 'js/business-metadata/business-metadata.html'
            }
        },
        data: {
            breadcrumbRoot: false,
            displayName: 'Business Metadata'
        }
    }).state("users", {
        url: "/users",
        params: {},
        views: {
            "content": {
                templateUrl: "js/users/users-table.html"
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Users"
        }
    }).state("user-details", {
        url: "/user-details/{userId}",
        params: {
            userId: null
        },
        views: {
            "content": {
                templateUrl: "js/user-details/user-details.html"
            }
        },
        data: {
            breadcrumbRoot: false,
            displayName: "User Details"
        }
    }).state("groups", {
        url: "/groups",
        params: {},
        views: {
            "content": {
                templateUrl: "js/groups/groups-table.html"
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Groups"
        }
    }).state("group-details", {
        url: "/group-details/{groupId}",
        params: {
            groupId: null
        },
        views: {
            "content": {
                templateUrl: "js/group-details/group-details.html"
            }
        },
        data: {
            breadcrumbRoot: false,
            displayName: "Group Details"
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
    ngMdIconService.addShape('number1','<path d="M0 0h24v24H0V0z" fill="none"/><path  d="M0.640623,7.5 C0.640623,3.67715 3.73965,0.578125 7.5625,0.578125 C11.3853,0.578125 14.4844,3.67716 14.4844,7.5 C14.4844,11.3228 11.3853,14.4219 7.5625,14.4219 C3.73965,14.4219 0.640623,11.3228 0.640623,7.5 M8.61426,10.5469 L7.20313,10.5469 L7.20313,5.10742 L5.51856,5.62988 L5.51856,4.48242 L8.46289,3.42774 L8.61426,3.42774 L8.61426,10.5469 Z" style="stroke:none;fill-rule:evenodd;fill-opacity:1;"/>');
    ngMdIconService.addShape('number2','<path d="M0 0h24v24H0V0z" fill="none"/><path d="M0.640623,7.5 C0.640623,3.67715 3.73965,0.578125 7.5625,0.578125 C11.3853,0.578125 14.4844,3.67716 14.4844,7.5 C14.4844,11.3228 11.3853,14.4219 7.5625,14.4219 C3.73965,14.4219 0.640623,11.3228 0.640623,7.5 M10.0596,10.5469 L5.18652,10.5469 L5.18652,9.58008 L7.48633,7.12891 C7.80208,6.78386 8.03565,6.48275 8.18701,6.22559 C8.33838,5.96843 8.41406,5.72429 8.41406,5.49317 C8.41406,5.17741 8.33431,4.9292 8.17481,4.74854 C8.0153,4.56787 7.78744,4.47754 7.49121,4.47754 C7.1722,4.47754 6.92074,4.5874 6.73682,4.80713 C6.5529,5.02686 6.46094,5.31576 6.46094,5.67383 L5.04492,5.67383 C5.04492,5.24089 5.14827,4.84538 5.35498,4.48731 C5.56169,4.12923 5.85384,3.84847 6.23145,3.64502 C6.60905,3.44157 7.03711,3.33985 7.51563,3.33985 C8.24805,3.33985 8.8169,3.51563 9.22217,3.86719 C9.62744,4.21875 9.83008,4.71517 9.83008,5.35645 C9.83008,5.70801 9.73893,6.06608 9.55664,6.43067 C9.37435,6.79525 9.06185,7.22005 8.61914,7.70508 L7.00293,9.40918 L10.0596,9.40918 L10.0596,10.5469 Z" style="stroke:none;fill-rule:evenodd;fill-opacity:1;" />');
    ngMdIconService.addShape('number3','<path d="M0 0h24v24H0V0z" fill="none"/><path  d="M0.640623,7.5 C0.640623,3.67715 3.73965,0.578125 7.5625,0.578125 C11.3853,0.578125 14.4844,3.67716 14.4844,7.5 C14.4844,11.3228 11.3853,14.4219 7.5625,14.4219 C3.73965,14.4219 0.640623,11.3228 0.640623,7.5 M6.62207,6.36719 L7.37402,6.36719 C7.7321,6.36719 7.9974,6.27767 8.16992,6.09864 C8.34245,5.9196 8.42871,5.68197 8.42871,5.38574 C8.42871,5.09929 8.34326,4.8763 8.17236,4.7168 C8.00147,4.55729 7.76628,4.47754 7.4668,4.47754 C7.19662,4.47754 6.97038,4.5516 6.78809,4.69971 C6.6058,4.84782 6.51465,5.04069 6.51465,5.27832 L5.10352,5.27832 C5.10352,4.90723 5.20361,4.57438 5.40381,4.27979 C5.604,3.98519 5.88395,3.75488 6.24365,3.58887 C6.60335,3.42285 6.99968,3.33985 7.43262,3.33985 C8.18457,3.33985 8.77376,3.5197 9.2002,3.8794 C9.62663,4.2391 9.83984,4.7347 9.83984,5.36621 C9.83984,5.69173 9.74056,5.99121 9.54199,6.26465 C9.34343,6.53809 9.08301,6.74805 8.76074,6.89453 C9.16113,7.03776 9.4598,7.25261 9.65674,7.53906 C9.85368,7.82552 9.95215,8.16407 9.95215,8.55469 C9.95215,9.1862 9.72184,9.69239 9.26123,10.0732 C8.80062,10.4541 8.19108,10.6445 7.43262,10.6445 C6.72298,10.6445 6.14274,10.4574 5.6919,10.083 C5.24105,9.70866 5.01563,9.21387 5.01563,8.59864 L6.42676,8.59864 C6.42676,8.86556 6.52686,9.08366 6.72705,9.25293 C6.92725,9.4222 7.17383,9.50684 7.4668,9.50684 C7.80208,9.50684 8.06494,9.41813 8.25537,9.24073 C8.4458,9.06332 8.54102,8.82813 8.54102,8.53516 C8.54102,7.82552 8.15039,7.47071 7.36914,7.47071 L6.62207,7.47071 L6.62207,6.36719 Z" style="stroke:none;fill-rule:evenodd;fill-opacity:1;"/>');
    ngMdIconService.addShape('number4','<path d="M0 0h24v24H0V0z" fill="none"/><path  d="M0.640623,7.5 C0.640623,3.67715 3.73965,0.578125 7.5625,0.578125 C11.3853,0.578125 14.4844,3.67716 14.4844,7.5 C14.4844,11.3228 11.3853,14.4219 7.5625,14.4219 C3.73965,14.4219 0.640623,11.3228 0.640623,7.5 M9.36133,7.8711 L10.167,7.8711 L10.167,9.00879 L9.36133,9.00879 L9.36133,10.5469 L7.9502,10.5469 L7.9502,9.00879 L5.03516,9.00879 L4.97168,8.12012 L7.93555,3.4375 L9.36133,3.4375 L9.36133,7.8711 M6.37793,7.8711 L7.9502,7.8711 L7.9502,5.36133 L7.85742,5.52246 L6.37793,7.8711 Z" style="stroke:none;fill-rule:evenodd;fill-opacity:1;"/>');
    ngMdIconService.addShape('number5','<path d="M0 0h24v24H0V0z" fill="none"/><path  d="M0.578123,7.5 C0.578123,3.67715 3.67715,0.578123 7.5,0.578123 C11.3228,0.578123 14.4219,3.67715 14.4219,7.5 C14.4219,11.3228 11.3228,14.4219 7.5,14.4219 C3.67715,14.4219 0.578123,11.3228 0.578123,7.5 M5.33838,7.05078 L5.74853,3.4375 L9.73291,3.4375 L9.73291,4.61426 L6.90576,4.61426 L6.72998,6.14258 C7.06527,5.96354 7.42171,5.87403 7.79932,5.87403 C8.4764,5.87403 9.007,6.08399 9.39111,6.50391 C9.77523,6.92383 9.96729,7.5114 9.96729,8.2666 C9.96729,8.72559 9.87044,9.13656 9.67676,9.49951 C9.48307,9.86247 9.20557,10.144 8.84424,10.3442 C8.48291,10.5444 8.05648,10.6445 7.56494,10.6445 C7.13525,10.6445 6.73649,10.5575 6.36865,10.3833 C6.00081,10.2092 5.71029,9.9642 5.49707,9.64844 C5.28385,9.33269 5.17073,8.97299 5.15771,8.56934 L6.5542,8.56934 C6.5835,8.86556 6.68685,9.09587 6.86426,9.26026 C7.04167,9.42465 7.2736,9.50684 7.56006,9.50684 C7.87907,9.50684 8.12484,9.39209 8.29736,9.1626 C8.46989,8.93311 8.55615,8.6084 8.55615,8.18848 C8.55615,7.78483 8.45687,7.47559 8.2583,7.26075 C8.05973,7.0459 7.77816,6.93848 7.41357,6.93848 C7.07829,6.93848 6.80648,7.02637 6.59814,7.20215 L6.46143,7.3291 L5.33838,7.05078 Z" style="stroke:none;fill-rule:evenodd;fill-opacity:1;"/>');
    ngMdIconService.addShape('number6','<path d="M0 0h24v24H0V0z" fill="none"/><path  d="M0.578123,7.5 C0.578123,3.67715 3.67715,0.578123 7.5,0.578123 C11.3228,0.578123 14.4219,3.67715 14.4219,7.5 C14.4219,11.3228 11.3228,14.4219 7.5,14.4219 C3.67715,14.4219 0.578123,11.3228 0.578123,7.5 M8.95654,3.36426 L8.95654,4.52637 L8.81982,4.52637 C8.1818,4.53614 7.66829,4.70215 7.2793,5.02442 C6.8903,5.34668 6.65674,5.79427 6.57861,6.36719 C6.95622,5.98308 7.43311,5.79102 8.00928,5.79102 C8.62777,5.79102 9.1193,6.01237 9.48389,6.45508 C9.84847,6.89779 10.0308,7.48047 10.0308,8.20313 C10.0308,8.66537 9.93066,9.08366 9.73047,9.45801 C9.53027,9.83236 9.24707,10.1237 8.88086,10.332 C8.51465,10.5404 8.10042,10.6445 7.63818,10.6445 C6.88949,10.6445 6.28483,10.3841 5.82422,9.86328 C5.36361,9.34245 5.1333,8.64746 5.1333,7.77832 L5.1333,7.27051 C5.1333,6.49903 5.27897,5.81787 5.57031,5.22705 C5.86165,4.63623 6.27995,4.17888 6.8252,3.85498 C7.37044,3.53109 8.00277,3.36752 8.72217,3.36426 L8.95654,3.36426 M7.57959,6.92383 C7.35173,6.92383 7.14502,6.98324 6.95947,7.10205 C6.77393,7.22087 6.63721,7.37793 6.54932,7.57325 L6.54932,8.00293 C6.54932,8.47494 6.64209,8.84359 6.82764,9.10889 C7.01318,9.37419 7.2736,9.50684 7.60889,9.50684 C7.91162,9.50684 8.15658,9.38721 8.34375,9.14795 C8.53092,8.90869 8.62451,8.59864 8.62451,8.21778 C8.62451,7.83041 8.53011,7.51791 8.34131,7.28028 C8.15251,7.04265 7.8986,6.92383 7.57959,6.92383 Z" style="stroke:none;fill-rule:evenodd;fill-opacity:1;"/>');
    ngMdIconService.addShape('number7','<path d="M0 0h24v24H0V0z" fill="none"/><path  d="M0.578123,7.5 C0.578123,3.67715 3.67715,0.578123 7.5,0.578123 C11.3228,0.578123 14.4219,3.67715 14.4219,7.5 C14.4219,11.3228 11.3228,14.4219 7.5,14.4219 C3.67715,14.4219 0.578123,11.3228 0.578123,7.5 M9.9624,4.22852 L7.21338,10.5469 L5.72412,10.5469 L8.47803,4.58008 L4.94287,4.58008 L4.94287,3.4375 L9.9624,3.4375 L9.9624,4.22852 Z" style="stroke:none;fill-rule:evenodd;fill-opacity:1;"/>');
    ngMdIconService.addShape('number8','<path d="M0 0h24v24H0V0z" fill="none"/><path  d="M0.578123,7.5 C0.578123,3.67715 3.67715,0.578123 7.5,0.578123 C11.3228,0.578123 14.4219,3.67715 14.4219,7.5 C14.4219,11.3228 11.3228,14.4219 7.5,14.4219 C3.67715,14.4219 0.578123,11.3228 0.578123,7.5 M9.76221,5.32715 C9.76221,5.6722 9.67594,5.97819 9.50342,6.24512 C9.33089,6.51205 9.09326,6.72526 8.79053,6.88477 C9.13558,7.05078 9.40902,7.27946 9.61084,7.5708 C9.81266,7.86215 9.91357,8.20476 9.91357,8.59864 C9.91357,9.23015 9.69873,9.72901 9.26904,10.0952 C8.83936,10.4614 8.25505,10.6445 7.51611,10.6445 C6.77718,10.6445 6.19124,10.4606 5.7583,10.0928 C5.32536,9.72494 5.10889,9.22689 5.10889,8.59864 C5.10889,8.20476 5.2098,7.86133 5.41162,7.56836 C5.61344,7.27539 5.88525,7.04753 6.22705,6.88477 C5.92432,6.72526 5.6875,6.51205 5.5166,6.24512 C5.3457,5.97819 5.26025,5.6722 5.26025,5.32715 C5.26025,4.72168 5.46208,4.2391 5.86572,3.8794 C6.26937,3.5197 6.81787,3.33985 7.51123,3.33985 C8.20133,3.33985 8.74902,3.51807 9.1543,3.87451 C9.55957,4.23096 9.76221,4.71517 9.76221,5.32715 M8.49756,8.4961 C8.49756,8.18685 8.40804,7.93946 8.229,7.75391 C8.04997,7.56836 7.80908,7.47559 7.50635,7.47559 C7.20687,7.47559 6.96761,7.56755 6.78857,7.75147 C6.60954,7.93539 6.52002,8.1836 6.52002,8.4961 C6.52002,8.79883 6.60791,9.04297 6.78369,9.22852 C6.95947,9.41407 7.20361,9.50684 7.51611,9.50684 C7.8221,9.50684 8.06217,9.41732 8.23633,9.23828 C8.41048,9.05925 8.49756,8.81185 8.49756,8.4961 M8.35107,5.39551 C8.35107,5.11882 8.27783,4.89665 8.13135,4.72901 C7.98486,4.56136 7.77816,4.47754 7.51123,4.47754 C7.24756,4.47754 7.04248,4.55892 6.896,4.72168 C6.74951,4.88444 6.67627,5.10905 6.67627,5.39551 C6.67627,5.67871 6.74951,5.90658 6.896,6.0791 C7.04248,6.25163 7.24919,6.33789 7.51611,6.33789 C7.78304,6.33789 7.98893,6.25163 8.13379,6.0791 C8.27865,5.90658 8.35107,5.67871 8.35107,5.39551 Z" style="stroke:none;fill-rule:evenodd;fill-opacity:1;"/>');
    ngMdIconService.addShape('number9','<path d="M0 0h24v24H0V0z" fill="none"/><path  d="M0.578123,7.5 C0.578123,3.67715 3.67715,0.578123 7.5,0.578123 C11.3228,0.578123 14.4219,3.67715 14.4219,7.5 C14.4219,11.3228 11.3228,14.4219 7.5,14.4219 C3.67715,14.4219 0.578123,11.3228 0.578123,7.5 M8.4292,7.70508 C8.06136,8.06641 7.63167,8.24707 7.14014,8.24707 C6.51188,8.24707 6.00895,8.03142 5.63135,7.6001 C5.25374,7.16879 5.06494,6.58692 5.06494,5.8545 C5.06494,5.389 5.16667,4.96175 5.37012,4.57276 C5.57357,4.18376 5.85758,3.88103 6.22217,3.66455 C6.58675,3.44808 6.99691,3.33985 7.45264,3.33985 C7.92139,3.33985 8.33805,3.45703 8.70264,3.69141 C9.06722,3.92578 9.35042,4.2627 9.55225,4.70215 C9.75407,5.1416 9.85661,5.64453 9.85986,6.21094 L9.85986,6.7334 C9.85986,7.9183 9.56527,8.84929 8.97607,9.52637 C8.38688,10.2035 7.55192,10.5648 6.47119,10.6104 L6.12451,10.6152 L6.12451,9.43848 L6.43701,9.4336 C7.66423,9.37826 8.32829,8.80209 8.4292,7.70508 M7.48682,7.16797 C7.71468,7.16797 7.91081,7.10938 8.0752,6.99219 C8.23958,6.875 8.36409,6.7334 8.44873,6.56739 L8.44873,5.98633 C8.44873,5.50782 8.35758,5.13672 8.17529,4.87305 C7.993,4.60938 7.74886,4.47754 7.44287,4.47754 C7.15967,4.47754 6.92692,4.60694 6.74463,4.86573 C6.56234,5.12451 6.47119,5.44922 6.47119,5.83985 C6.47119,6.22722 6.5599,6.54541 6.7373,6.79444 C6.91471,7.04346 7.16455,7.16797 7.48682,7.16797 Z" style="stroke:none;fill-rule:evenodd;fill-opacity:1;"/>');
    ngMdIconService.addShape('number10','<path d="M0 0h24v24H0V0z" fill="none"/><path  d="M0.578123,7.5 C0.578123,3.67715 3.67715,0.578123 7.5,0.578123 C11.3228,0.578123 14.4219,3.67715 14.4219,7.5 C14.4219,11.3228 11.3228,14.4219 7.5,14.4219 C3.67715,14.4219 0.578123,11.3228 0.578123,7.5 M5.68115,10.5469 L4.27002,10.5469 L4.27002,5.10742 L2.58545,5.62989 L2.58545,4.48242 L5.52978,3.42774 L5.68115,3.42774 L5.68115,10.5469 M12.773,7.60743 C12.773,8.5905 12.5695,9.34245 12.1626,9.86328 C11.7557,10.3841 11.16,10.6445 10.3755,10.6445 C9.60075,10.6445 9.0083,10.389 8.59814,9.87793 C8.18799,9.36687 7.97803,8.63444 7.96826,7.68067 L7.96826,6.37207 C7.96826,5.37923 8.17415,4.62565 8.58594,4.11133 C8.99772,3.59701 9.59098,3.33985 10.3657,3.33985 C11.1405,3.33985 11.7329,3.59457 12.1431,4.10401 C12.5532,4.61345 12.7632,5.34506 12.773,6.29883 L12.773,7.60743 M11.3618,6.17188 C11.3618,5.58268 11.2812,5.15381 11.1201,4.88526 C10.959,4.6167 10.7075,4.48242 10.3657,4.48242 C10.0337,4.48242 9.78874,4.61019 9.63086,4.86573 C9.47298,5.12126 9.38916,5.52084 9.37939,6.06446 L9.37939,7.79297 C9.37939,8.3724 9.45833,8.8029 9.61621,9.08448 C9.77409,9.36605 10.0272,9.50684 10.3755,9.50684 C10.7205,9.50684 10.9696,9.37175 11.1226,9.10157 C11.2756,8.83138 11.3553,8.41797 11.3618,7.86133 L11.3618,6.17188 Z" style="stroke:none;fill-rule:evenodd;fill-opacity:1;"/>');
    ngMdIconService.addShape('playlist_play','<path d="M0 0h24v24H0V0z" fill="none"/><path d="M19 9H2v2h17V9zm0-4H2v2h17V5zM2 15h13v-2H2v2zm15-2v6l5-3-5-3z"/>');
    ngMdIconService.addShape('table_grid','<path d="M0 0h24v24H0V0z" fill="none"/><path d="M23.0141,2.78435 C22.5743,2.34479 22.0461,2.12513 21.4285,2.12513 L2.5716,2.12513 C1.95427,2.12513 1.42575,2.34479 0.98607,2.78435 C0.546492,3.22402 0.326678,3.75221 0.326678,4.36953 L0.326678,19.6308 C0.326678,20.2482 0.546492,20.776 0.98607,21.2158 C1.42575,21.6555 1.95422,21.8749 2.5716,21.8749 L21.4285,21.8749 C22.046,21.8749 22.5743,21.6555 23.0141,21.2158 C23.4537,20.776 23.6733,20.2479 23.6733,19.6308 L23.6733,4.36953 C23.6733,3.75221 23.4537,3.22387 23.0141,2.78435 M7.51045,19.6303 C7.51045,19.7617 7.46824,19.8689 7.38396,19.9532 C7.29983,20.0372 7.19236,20.079 7.06124,20.079 L2.5716,20.079 C2.44078,20.079 2.33311,20.0372 2.24888,19.9532 C2.1647,19.8689 2.12268,19.7617 2.12268,19.6303 L2.12268,16.937 C2.12268,16.8059 2.1647,16.6986 2.24888,16.6147 C2.33311,16.5303 2.44078,16.4883 2.5716,16.4883 L7.06139,16.4883 C7.19245,16.4883 7.30013,16.5303 7.38411,16.6147 C7.46819,16.6986 7.51041,16.8059 7.51041,16.937 L7.51041,19.6303 L7.51045,19.6303 M7.51045,14.2443 C7.51045,14.3751 7.46824,14.4828 7.38396,14.5668 C7.29983,14.651 7.19236,14.693 7.06124,14.693 L2.5716,14.693 C2.44078,14.693 2.33311,14.651 2.24888,14.5668 C2.1647,14.4827 2.12268,14.3751 2.12268,14.2443 L2.12268,11.551 C2.12268,11.4201 2.1647,11.3126 2.24888,11.2284 C2.33311,11.1444 2.44078,11.1024 2.5716,11.1024 L7.06139,11.1024 C7.19245,11.1024 7.29993,11.1444 7.38411,11.2284 C7.46819,11.3126 7.51041,11.4201 7.51041,11.551 L7.51041,14.2443 L7.51045,14.2443 M7.51045,8.85798 C7.51045,8.98896 7.46824,9.09641 7.38396,9.18062 C7.29983,9.26458 7.19236,9.30678 7.06124,9.30678 L2.5716,9.30678 C2.44078,9.30678 2.33311,9.26458 2.24888,9.18062 C2.1647,9.09636 2.12268,8.98896 2.12268,8.85798 L2.12268,6.16483 C2.12268,6.03385 2.1647,5.92626 2.24888,5.84219 C2.33311,5.75803 2.44078,5.71603 2.5716,5.71603 L7.06139,5.71603 C7.19245,5.71603 7.29993,5.75803 7.38411,5.84219 C7.46819,5.92626 7.51041,6.03385 7.51041,6.16483 L7.51041,8.85798 L7.51045,8.85798 M14.694,19.6303 C14.694,19.7617 14.652,19.8689 14.5681,19.9532 C14.484,20.0372 14.3764,20.079 14.2452,20.079 L9.75513,20.079 C9.62426,20.079 9.51664,20.0372 9.43241,19.9532 C9.34823,19.8689 9.30621,19.7617 9.30621,19.6303 L9.30621,16.937 C9.30621,16.8059 9.34823,16.6986 9.43241,16.6147 C9.51664,16.5303 9.62426,16.4883 9.75513,16.4883 L14.245,16.4883 C14.3762,16.4883 14.4838,16.5303 14.5679,16.6147 C14.6516,16.6986 14.6937,16.8059 14.6937,16.937 L14.6937,19.6303 L14.694,19.6303 L14.694,19.6303 M14.694,14.2443 C14.694,14.3751 14.652,14.4828 14.5681,14.5668 C14.484,14.651 14.3764,14.693 14.2452,14.693 L9.75513,14.693 C9.62426,14.693 9.51664,14.651 9.43241,14.5668 C9.34823,14.4827 9.30621,14.3751 9.30621,14.2443 L9.30621,11.551 C9.30621,11.4201 9.34823,11.3126 9.43241,11.2284 C9.51664,11.1444 9.62426,11.1024 9.75513,11.1024 L14.245,11.1024 C14.3762,11.1024 14.4838,11.1444 14.5679,11.2284 C14.6516,11.3126 14.6937,11.4201 14.6937,11.551 L14.6937,14.2443 L14.694,14.2443 M14.694,8.85798 C14.694,8.98896 14.652,9.09641 14.5681,9.18062 C14.484,9.26458 14.3764,9.30678 14.2452,9.30678 L9.75513,9.30678 C9.62426,9.30678 9.51664,9.26458 9.43241,9.18062 C9.34823,9.09636 9.30621,8.98896 9.30621,8.85798 L9.30621,6.16483 C9.30621,6.03385 9.34823,5.92626 9.43241,5.84219 C9.51664,5.75803 9.62426,5.71603 9.75513,5.71603 L14.245,5.71603 C14.3762,5.71603 14.4838,5.75803 14.5679,5.84219 C14.6516,5.92626 14.6937,6.03385 14.6937,6.16483 L14.6937,8.85798 L14.694,8.85798 M21.8774,19.6303 C21.8774,19.7617 21.8354,19.8689 21.7515,19.9532 C21.667,20.0372 21.5594,20.079 21.4286,20.079 L16.9392,20.079 C16.8078,20.079 16.7003,20.0372 16.6163,19.9532 C16.5323,19.8689 16.4905,19.7617 16.4905,19.6303 L16.4905,16.937 C16.4905,16.8059 16.5323,16.6986 16.6163,16.6147 C16.7003,16.5303 16.8078,16.4883 16.9392,16.4883 L21.4286,16.4883 C21.5594,16.4883 21.6671,16.5303 21.7515,16.6147 C21.8354,16.6986 21.8774,16.8059 21.8774,16.937 L21.8774,19.6303 M21.8774,14.2443 C21.8774,14.3751 21.8354,14.4828 21.7515,14.5668 C21.667,14.651 21.5594,14.693 21.4286,14.693 L16.9392,14.693 C16.8078,14.693 16.7003,14.651 16.6163,14.5668 C16.5323,14.4827 16.4905,14.3751 16.4905,14.2443 L16.4905,11.551 C16.4905,11.4201 16.5323,11.3126 16.6163,11.2284 C16.7003,11.1444 16.8078,11.1024 16.9392,11.1024 L21.4286,11.1024 C21.5594,11.1024 21.6671,11.1444 21.7515,11.2284 C21.8354,11.3126 21.8774,11.4201 21.8774,11.551 L21.8774,14.2443 M21.8774,8.85798 C21.8774,8.98896 21.8354,9.09641 21.7515,9.18062 C21.667,9.26458 21.5594,9.30678 21.4286,9.30678 L16.9392,9.30678 C16.8078,9.30678 16.7003,9.26458 16.6163,9.18062 C16.5323,9.09636 16.4905,8.98896 16.4905,8.85798 L16.4905,6.16483 C16.4905,6.03385 16.5323,5.92626 16.6163,5.84219 C16.7003,5.75803 16.8078,5.71603 16.9392,5.71603 L21.4286,5.71603 C21.5594,5.71603 21.6671,5.75803 21.7515,5.84219 C21.8354,5.92626 21.8774,6.03385 21.8774,6.16483 L21.8774,8.85798 Z" />')




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

            $rootScope.typeOf = function(value) {
                return typeof value;
            };
        }
    ]
)
