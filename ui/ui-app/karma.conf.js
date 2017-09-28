module.exports = function(config) {

    var appBase    = 'src/main/resources/static/js/';       // transpiled app JS and map files

    config.set({
        basePath: '',
        frameworks: ['jasmine'],

        plugins: [
            require('karma-jasmine'),
            require('karma-chrome-launcher'),
            require('karma-jasmine-html-reporter')
        ],

        client: {
            builtPaths: [appBase], // add more spec base paths as needed
            clearContext: false // leave Jasmine Spec Runner output visible in browser
        },

        customLaunchers: {
            // From the CLI. Not used here but interesting
            // chrome setup for travis CI using chromium
            Chrome_travis_ci: {
                base: 'Chrome',
                flags: ['--no-sandbox']
            }
        },

        files: [
            // Tern
            appBase + "vendor/tern/plugin/acorn.js",
            appBase + "vendor/tern/plugin/acorn_loose.js",
            appBase + "vendor/tern/plugin/walk.js",
            appBase + "vendor/tern/plugin/polyfill.js",
            appBase + "vendor/tern/lib/signal.js",
            appBase + "vendor/tern/lib/tern.js",
            appBase + "vendor/tern/lib/def.js",
            appBase + "vendor/tern/lib/comment.js",
            appBase + "vendor/tern/lib/infer.js",
            appBase + "vendor/tern/plugin/doc_comment.js",

            // System.js for module loading
            'node_modules/systemjs/dist/system.src.js',

            // Polyfills
            'node_modules/core-js/client/shim.js',

            // zone.js
            'node_modules/zone.js/dist/zone.js',
            'node_modules/zone.js/dist/long-stack-trace-zone.js',
            'node_modules/zone.js/dist/proxy.js',
            'node_modules/zone.js/dist/sync-test.js',
            'node_modules/zone.js/dist/jasmine-patch.js',
            'node_modules/zone.js/dist/async-test.js',
            'node_modules/zone.js/dist/fake-async-test.js',

            // RxJs
            { pattern: 'node_modules/rxjs/**/*.js', included: false, watched: false },
            { pattern: 'node_modules/rxjs/**/*.js.map', included: false, watched: false },

            // Paths loaded via module imports:
            // Angular itself
            { pattern: 'node_modules/@angular/**/*.js', included: false, watched: false },
            { pattern: 'node_modules/@angular/**/*.js.map', included: false, watched: false },
            { pattern: 'node_modules/@uirouter/**/*.js', included: false, watched: false },
            { pattern: 'node_modules/@uirouter/**/*.js.map', included: false, watched: false },
            { pattern: 'node_modules/angular-mocks/angular-mocks.js', included: false, watched: false },

            { pattern: 'src/main/resources/static/bower_components/**/*.js', included: false, watched: false },
            { pattern: appBase + '/systemjs.config.js', included: false, watched: false },
            'src/main/resources/static/test.js', // optionally extend SystemJS mapping e.g., with barrels

            // transpiled application & spec code paths loaded via module imports
            { pattern: appBase + '**/*.js', included: false, watched: true },


            // Asset (HTML & CSS) paths loaded via Angular's component compiler
            // (these paths need to be rewritten, see proxies section)
            { pattern: appBase + '**/*.html', included: false, watched: true },
            { pattern: appBase + '**/*.css', included: false, watched: true },

            // Paths for debugging with source maps in dev tools
            { pattern: appBase + '**/*.ts', included: false, watched: false },
            { pattern: appBase + '**/*.js.map', included: false, watched: false }
        ],

        // Proxied base paths for loading assets
        proxies: {
            // required for modules fetched by SystemJS
            '/base/src/node_modules/': '/base/node_modules/'
        },

        exclude: [],
        preprocessors: {},
        reporters: ['progress', 'kjhtml'],

        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO,
        autoWatch: true,
        browsers: ['Chrome'],
        singleRun: false
    })
};
