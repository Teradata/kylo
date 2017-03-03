
define(['angular','common/module-name'], function (angular,moduleName) {

    angular.module('templates.arrow.html', []).run(['$templateCache', function ($templateCache) {
        'use strict';
        $templateCache.put('templates.arrow.html',
            '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18"><path d="M3,9 L4.06,10.06 L8.25,5.87 L8.25,15 L9.75,15 L9.75,5.87 L13.94,10.06 L15,9 L9,3 L3,9 L3,9 Z"/></svg>');
    }]);

    angular.module('templates.navigate-before.html', []).run(['$templateCache', function ($templateCache) {
        'use strict';
        $templateCache.put('templates.navigate-before.html',
            '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>\n' +
            '');
    }]);

    angular.module('templates.navigate-first.html', []).run(['$templateCache', function ($templateCache) {
        'use strict';
        $templateCache.put('templates.navigate-first.html',
            '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M7 6 v12 h2 v-12 h-2z M17.41 7.41L16 6l-6 6 6 6 1.41-1.41L12.83 12z"/></svg>\n' +
            '');
    }]);

    angular.module('templates.navigate-last.html', []).run(['$templateCache', function ($templateCache) {
        'use strict';
        $templateCache.put('templates.navigate-last.html',
            '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M15 6 v12 h2 v-12 h-2z M8 6L6.59 7.41 11.17 12l-4.58 4.59L8 18l6-6z"/></svg>\n' +
            '');
    }]);

    angular.module('templates.navigate-next.html', []).run(['$templateCache', function ($templateCache) {
        'use strict';
        $templateCache.put('templates.navigate-next.html',
            '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/></svg>\n' +
            '');
    }]);
});