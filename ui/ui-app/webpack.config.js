"use strict";

const path = require("path");
const webpack = require("webpack");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const CleanWebpackPlugin = require("clean-webpack-plugin");
const writeFilePlugin = require('write-file-webpack-plugin');
const UglifyJSPlugin = require("uglifyjs-webpack-plugin");
const CopyWebpackPlugin = require('copy-webpack-plugin');
const BundleAnalyzerPlugin = require("webpack-bundle-analyzer").BundleAnalyzerPlugin;
const sass = require('sass');
const CompressionPlugin = require("compression-webpack-plugin");
const FriendlyErrorsWebpackPlugin = require('friendly-errors-webpack-plugin');
const ProgressPlugin = require('webpack/lib/ProgressPlugin');
const AngularCompilerPlugin = require('@ngtools/webpack').AngularCompilerPlugin;

const outputDir = path.resolve(__dirname, 'target/classes/static');
const nodeModulesDir = path.resolve(__dirname, 'node_modules');
const staticDir = path.resolve('./src/main/resources/static');
const staticJsDir = path.join(staticDir, 'js');
const staticNodeModules = path.join(staticDir, 'node_modules');
const staticBower = path.join(staticDir, 'bower_components');
const staticJsVendorDir = path.join(staticJsDir, 'vendor');
const tsConfigFile = path.join(staticDir, 'tsconfig.json');
const mainTsFile = path.join(staticJsDir, 'main.ts');

/**
 * Depends on nesting of scripts in package.json.
 * Looks up parameters passed like so: ./npm run start:webpack:dev -- --env.port=7000
 * However with deeply nested scripts need to add additional "--", e.g. ./npm start -- -- --env.port=7000
 */
function findParam(param){
    let found = false;
    let result = undefined;
    process.argv.forEach((argv)=>{
        console.log('parsing argv ' + argv);
        if(argv.indexOf("--env." + param) === -1) {
            return;
        }
        found = true;
        result = argv.split('=')[1];
    });
    console.log("found? " + found + " param " + param + ": " + result);
    return  result;
}

let port = findParam('port');
let host = findParam('host');
if (port === undefined) {
    port = 3000;
}
if (host === undefined) {
    host = "localhost";
}
console.log("port " + port);
console.log("host " + host);

const devServer = {
    contentBase: outputDir,
    hot: true,
    host: host,
    port: port,
    proxy: [{
        context: [
            '/api',
            '/login',
            '/logout',
            '/proxy',
            '/js/plugin',
            '/api-docs'
        ],
        target: 'http://localhost:8400',
        secure: false,
        changeOrigin: false,
        headers: {host: 'localhost:' + port}
    }]
};

const SourcePlugin =  new webpack.SourceMapDevToolPlugin({
    filename: "[file].map",
    exclude: ['entryPolyfills.bundle.js', 'common.js'],
});


const vendorExcludes = [
    nodeModulesDir,
    staticNodeModules,
    staticBower,
    staticJsVendorDir
];


const webpackConfig = (env) => {
    const config = {
        resolve: {
            extensions: ['.ts', '.js'],
            modules: [
                path.resolve(__dirname, 'src/main/resources/static/js/vendor'),
                path.resolve(__dirname, 'src/main/resources/static/node_modules'),
                path.resolve(__dirname, 'src/main/resources/static/bower_components'),
                path.resolve(__dirname, 'node_modules')
            ],
            alias: {
                'routes': path.join(staticJsDir, 'routes'),
                'app': path.join(staticJsDir, 'app'),
                'kylo-common': path.join(staticJsDir, 'common/module-require'),
                'kylo-common-module': path.join(staticJsDir, 'common/module'),
                'kylo-services': path.join(staticJsDir, 'services/module-require'),
                'kylo-services-module': path.join(staticJsDir, 'services/module'),
                'kylo-side-nav': path.join(staticJsDir, 'side-nav/module-require'),
                'kylo-feedmgr': path.join(staticJsDir, 'feed-mgr/module-require'),
                'kylo-opsmgr': path.join(staticJsDir, 'ops-mgr/module-require'),
                'codemirror-require/module': path.join(staticJsDir, 'codemirror-require/module'),
                'feed-mgr/catalog/catalog.module': path.join(staticJsDir, 'feed-mgr/catalog/catalog.module'),
                'dirPagination.tpl.html': path.join(staticJsDir, 'common/dir-pagination/dirPagination.tpl.html'),

                'constants/AccessConstants': path.join(staticJsDir, 'constants/AccessConstants'),
                'kylo-utils/LazyLoadUtil': path.join(staticJsDir, 'kylo-utils/LazyLoadUtil'),

                'angularCookies': path.join(staticBower, 'angular-cookies/angular-cookies.min.js'),
                'angular-cookies': path.join(staticBower, 'angular-cookies/angular-cookies.min.js'),
                'angular-material-data-table': path.join(staticBower, 'angular-material-data-table/dist/md-data-table.min'),
                'angular-material-expansion-panel': path.join(staticBower, 'angular-material-expansion-panel/dist/md-expansion-panel.min'),
                'angular-sanitize': path.join(staticBower, 'angular-sanitize/angular-sanitize.min'),
                'angular-translate-handler-log': path.join(staticBower, 'angular-translate-handler-log/angular-translate-handler-log.min.js'),
                'angular-translate-loader-static-files': path.join(staticBower, 'angular-translate-loader-static-files/angular-translate-loader-static-files.min.js'),
                'angular-translate-storage-cookie': path.join(staticBower, 'angular-translate-storage-cookie/angular-translate-storage-cookie.min.js'),
                'angular-translate-storage-local': path.join(staticBower, 'angular-translate-storage-local/angular-translate-storage-local.min.js'),
                'angular-ui-grid': path.join(staticBower, 'angular-ui-grid/ui-grid.min'),
                'angularAnimate': path.join(staticBower, 'angular-animate/angular-animate.min'),
                'angularAria': path.join(staticBower, 'angular-aria/angular-aria.min'),
                'angularMaterial': path.join(staticBower, 'angular-material/angular-material'),
                'angularMessages': path.join(staticBower, 'angular-messages/angular-messages.min'),
                'jquery': path.join(staticBower, 'jquery/dist/jquery.min'),
                'ng-fx': path.join(staticBower, 'ngFx/dist/ngFx.min'),
                'pascalprecht.translate': path.join(staticBower, 'angular-translate/angular-translate'),
                'tmh.dynamicLocale': path.join(staticBower, 'angular-dynamic-locale/dist/tmhDynamicLocale.min'),
                'underscore': path.join(staticBower, 'underscore/underscore-min'),
                'angular-drag-and-drop-lists': path.join(staticBower, 'angular-drag-and-drop-lists/angular-drag-and-drop-lists.min'),
                'fattable': path.join(staticBower, 'fattable/fattable'),
                'd3': path.join(staticBower, 'd3/d3.min'),
                'nvd3': path.join(staticBower, 'nvd3/build/nv.d3.min'),
                'angular-nvd3': path.join(staticBower, 'angular-nvd3/dist/angular-nvd3.min'),
                'gsap': path.join(staticBower, 'gsap/src/uncompressed/TweenMax'),
                'vis': path.join(staticBower, 'vis/dist/vis.min'),
                'angular-visjs': path.join(staticBower, 'angular-visjs/angular-vis'),
                'ocLazyLoad': path.join(staticBower, 'oclazyload/dist/ocLazyLoad'), //System.amdRequire with ocLazyLoad.require
                'jquery-ui': path.join(staticBower, 'jquery-ui/jquery-ui.min'),
                'pivottable': path.join(staticBower, 'pivottable/dist/pivot.min'),
                'pivottable-c3-renderers': path.join(staticBower, 'pivottable/dist/c3_renderers.min'),
                'c3': path.join(staticBower, 'c3/c3.min'),

                'angular-material-icons': path.join(staticJsVendorDir, 'angular-material-icons/angular-material-icons'),
                'dirPagination': path.join(staticJsVendorDir, 'dirPagination/dirPagination'),
                'ng-text-truncate': path.join(staticJsVendorDir, 'ng-text-truncate/ng-text-truncate'),
                'ment-io': path.join(staticJsVendorDir, 'ment.io/mentio'),

                'urlParams': path.join(staticDir, 'login/jquery.urlParam.js'),
            }
        },
        entry: {
            entryPolyfills: path.resolve('./src/main/resources/static/polyfills'),
            global: path.resolve('./src/main/resources/static/assets/global.scss'),
            app: mainTsFile,
        },
        output: {
            filename: 'js/[name].bundle.js',
            chunkFilename: 'js/[id].chunk.js',
            path: outputDir
        },
        module: {
            loaders: [
                {
                    test: /\.html$/,
                    loader: "raw-loader",
                    exclude: path.resolve("./src/main/resources/static/js/index.html")
                },
                {
                    test: /.(ttf|otf|eot|woff(2)?)(\?[a-z0-9]+)?$/,
                    use: [{
                        loader: 'file-loader',
                        options: {
                            name: '[path][name].[ext]',
                        }
                    }]
                }, {
                    test: /\.(jpe?g|png|svg|gif)/i,
                    loader: 'file-loader',
                    options: {
                        context: './src/main/resources/static',
                        name: '[path][name].[ext]'
                    }
                },
                {
                    test: /\.scss$/,
                    use: ['to-string-loader',
                        {
                            loader: 'css-loader',
                            options: {minimize: true}
                        },
                        {
                            loader: 'sass-loader',
                            options: {implementation: sass}
                        }
                    ],
                    exclude: /(theme\.scss|global\.scss)/
                },
                {
                    test: /(theme\.scss|global\.scss)/,
                    use: ['style-loader',
                        {
                            loader: 'css-loader',
                            options: {minimize: true}
                        },
                          'postcss-loader',
                        {
                            loader: 'sass-loader',
                            options: {implementation: sass}
                        }
                    ]
                },
                {
                    test: /\.css$/,
                    use: ['to-string-loader', 'css-loader']
                },
                {
                    test: /\.js$/,
                    use: [
                        'babel-loader',
                        {
                            loader: path.resolve('./webpack.angular.template.loader.js'),
                            options: {
                                baseUrl: "src/main/resources/static"
                            }
                        }
                        ],
                    include: [
                        staticDir
                    ],
                    exclude: vendorExcludes
                },
            ]
        },
        plugins: [
            new CopyWebpackPlugin([
                {from: './src/main/resources/static/assets/images/favicons', to: 'assets/images/favicons'},
                {from: './src/main/resources/static/locales/', to: 'locales'},
                ...indexPageDependencies,
                ...loginPageDependencies,
                ...templates,
                ...wranlgerDependencies,
            ]),
            new HtmlWebpackPlugin({
                filename: "index.html",
                template: path.resolve("./src/main/resources/static/index.html"),
                chunks: ['common', 'entryPolyfills', 'global', 'app'],
                chunksSortMode: 'manual',
                inject: 'body'
            }),

            new webpack.optimize.CommonsChunkPlugin({
                name: "common",
                filename: "common.js",
                minChunks: (module) => {
                    return module.context && (module.context.indexOf("node_modules") !== -1 || module.context.indexOf("bower_components") !== -1 || module.context.indexOf("vendor") !== -1)
                }
            }),

            new CleanWebpackPlugin([
                path.resolve(__dirname, "./target/classes/static/*.js"),
                path.resolve(__dirname, "./target/cache"),
                path.resolve(__dirname, "./target/aot")
            ]),

            new webpack.ProvidePlugin({
                "window.jQuery": "jquery", //https://webpack.js.org/plugins/provide-plugin/#usage-jquery-with-angular-1
                "$": "jquery",
                "d3": "d3",
                "window.vis": "vis",
            }),

            // new BundleAnalyzerPlugin(),

        ]
    };

    if (env && env.production) {
        config.devtool = 'source-map';
        config.module.loaders.push(
            {
                test: /(\.ts)$/,
                use: [
                    {
                        loader: path.resolve('./webpack.angular.template.loader.js'),
                        options: {
                            baseUrl: "src/main/resources/static"
                        }
                    },
                ],
                exclude: vendorExcludes
            },
            {
                test: /(?:\.ngfactory\.js|\.ngstyle\.js|\.ts)$/,
                use: [
                    "@ngtools/webpack",
                ],
                exclude: vendorExcludes
            },
        );
        config.plugins.push(
            new webpack.LoaderOptionsPlugin({
                htmlLoader: {
                    minimize: false // workaround for ng2
                }
            }),
            new webpack.NoEmitOnErrorsPlugin(),
            new webpack.DefinePlugin({
                'process.env': {
                    'ENV': JSON.stringify("production"),
                }
            }),
            new AngularCompilerPlugin({
                mainPath: 'src/main/resources/static/js/main.ts',
                tsConfigPath: tsConfigFile,
                entryModule: path.join(__dirname, 'src/main/resources/static/js/app.module#KyloModule'),
                sourceMap: true
            }),
            new writeFilePlugin(),
            new CompressionPlugin({
                cache: true,
                deleteOriginalAssets: false //false, otherwise resources are not found even though gzipped resources with the same name exist
            }),
            new UglifyJSPlugin({
                exclude: /\.min\.js$/, //only available in v2.0+
                cache: path.resolve(__dirname, './target/cache/uglifyjs-plugin'),
                parallel: require('os').cpus().length,
                sourceMap: false,
                uglifyOptions: {
                    compress: true,
                    mangle: false,
                    warnings: false
                }
            }),
        );
    } else {
        config.devServer = devServer;
        config.module.loaders.push(
            {
                test: /\.ts$/,
                use: [
                    {
                        loader: 'cache-loader',
                        options: {
                            cacheDirectory: path.resolve('target/cache/cache-loader')
                        }
                    },
                    {
                        loader: 'thread-loader',
                        options: {
                            workers: require('os').cpus().length
                        }
                    },
                    {
                        loader: 'ts-loader',
                        options: {
                            configFile: tsConfigFile,
                            transpileOnly: true,
                            happyPackMode: true
                        }
                    },
                    {
                        loader: path.resolve('./webpack.angular.template.loader.js'),
                        options: {
                            baseUrl: "src/main/resources/static"
                        }
                    },
                    'angular-router-loader'
                ],
                exclude: vendorExcludes
            },

        );
        config.plugins.push(
            new webpack.ContextReplacementPlugin(
                //https://github.com/angular/angular/issues/20357
                /angular(\\|\/)core(\\|\/)/,
                path.resolve(__dirname, './src/main/resources/static')
            ),
            new webpack.NamedModulesPlugin(),
            new webpack.HotModuleReplacementPlugin(),
            new FriendlyErrorsWebpackPlugin(),
            new ProgressPlugin(),
            // new writeFilePlugin(),
        );
        if (env && env.dev) { //i.e. we dont' want SourcePlugin if env is other than dev env, e.g. if env is dev-tool-cheap
            config.plugins.push(
                SourcePlugin //this plugin is faster than devtool=source-map because its excluding node_modules, bower_components and vendor dirs
            );
        }
    }

    return config;
};

const wranlgerDependencies = [
    {
        context: './src/main/resources/static',
        from: 'js/vendor/**/*.js',
        to: '[path][name].[ext]'
    },
    {
        context: './src/main/resources/static',
        from: './bower_components/angular-ui-grid/ui-grid.css',
        to: './bower_components/angular-ui-grid/ui-grid.css'
    },
    {
        context: './src/main/resources/static',
        from: './bower_components/angular-ui-grid/ui-grid.woff',
        to: './bower_components/angular-ui-grid/ui-grid.woff'
    },
    {
        context: './src/main/resources/static',
        from: './bower_components/angular-ui-grid/ui-grid.ttf',
        to: './bower_components/angular-ui-grid/ui-grid.ttf'
    }
];

const templates = [
    {
        context: './src/main/resources/static',
        from: 'js/common/dir-pagination/**/*.html',
        to: '[path][name].[ext]'
    },
    {
        context: './src/main/resources/static',
        from: 'js/feed-mgr/templates/template-stepper/register-template-stepper.html',
        to: 'js/feed-mgr/templates/template-stepper/register-template-stepper.html'
    },
    {
        context: './src/main/resources/static',
        from: 'js/feed-mgr/templates/template-stepper/processor-properties/expression-property-mentions.html',
        to: 'js/feed-mgr/templates/template-stepper/processor-properties/expression-property-mentions.html'
    },
    {
        context: './src/main/resources/static',
        from: 'js/feed-mgr/feeds/define-feed/define-feed-stepper.html',
        to: 'js/feed-mgr/feeds/define-feed/define-feed-stepper.html'
    },
    {
        context: './src/main/resources/static',
        from: 'js/ops-mgr/alerts/alerts-pagination.tpl.html',
        to: 'js/ops-mgr/alerts/alerts-pagination.tpl.html'
    },
    {
        context: './src/main/resources/static',
        from: 'js/ops-mgr/alerts/alert-type-filter-select.html',
        to: 'js/ops-mgr/alerts/alert-type-filter-select.html'
    },
    {
        context: './src/main/resources/static',
        from: 'js/feed-mgr/visual-query/transform-data/visual-query-table/visual-query-table-header.html',
        to: 'js/feed-mgr/visual-query/transform-data/visual-query-table/visual-query-table-header.html'
    },
    {
        context: './src/main/resources/static',
        from: 'js/common/ui-router-breadcrumbs/uiBreadcrumbs.tpl.html',
        to: 'js/common/ui-router-breadcrumbs/uiBreadcrumbs.tpl.html'
    }
];

const indexPageDependencies = [
    {
        context: './src/main/resources/static',
        from: 'node_modules/systemjs/**/*.js',
        to: '[path][name].[ext]'
    },
    {
        context: './src/main/resources/static',
        from: 'js/vendor/ment.io/styles.css',
        to: 'js/vendor/ment.io/styles.css'
    },
    {
        context: './src/main/resources/static',
        from: 'js/vendor/font-awesome/css/font-awesome.min.css',
        to: 'js/vendor/font-awesome/css/font-awesome.min.css'
    },
    {
        context: './src/main/resources/static',
        from: 'js/vendor/font-awesome/fonts/fontawesome-webfont.woff',
        to: 'js/vendor/font-awesome/fonts/fontawesome-webfont.woff'
    },
    {
        context: './src/main/resources/static',
        from: 'js/vendor/font-awesome/fonts/fontawesome-webfont.woff2',
        to: 'js/vendor/font-awesome/fonts/fontawesome-webfont.woff2'
    },
    {
        context: './src/main/resources/static',
        from: 'js/vendor/font-awesome/fonts/fontawesome-webfont.eot',
        to: 'js/vendor/font-awesome/fonts/fontawesome-webfont.eot'
    },
    {
        context: './src/main/resources/static',
        from: 'js/vendor/font-awesome/fonts/fontawesome-webfont.ttf',
        to: 'js/vendor/font-awesome/fonts/fontawesome-webfont.ttf'
    },
    {
        context: './src/main/resources/static',
        from: 'js/vendor/font-awesome/fonts/FontAwesome.otf',
        to: 'js/vendor/font-awesome/fonts/FontAwesome.otf'
    },
    {
        context: './src/main/resources/static',
        from: 'js/vendor/font-awesome/fonts/fontawesome-webfont.svg',
        to: 'js/vendor/font-awesome/fonts/fontawesome-webfont.svg'
    }
];

const loginPageDependencies = [
    {
        context: './src/main/resources/static',
        from: 'login',
        to: 'login'
    },
    {
        context: './src/main/resources/static',
        from: 'login.html',
        to: 'login.html'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/oclazyload/dist/ocLazyLoad.require.js',
        to: 'bower_components/oclazyload/dist/ocLazyLoad.require.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular-animate/angular-animate.js',
        to: 'bower_components/angular-animate/angular-animate.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular-aria/angular-aria.js',
        to: 'bower_components/angular-aria/angular-aria.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular-messages/angular-messages.js',
        to: 'bower_components/angular-messages/angular-messages.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/requirejs/require.js',
        to: 'bower_components/requirejs/require.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular/angular.js',
        to: 'bower_components/angular/angular.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/underscore/underscore.js',
        to: 'bower_components/underscore/underscore.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/jquery/dist/jquery.js',
        to: 'bower_components/jquery/dist/jquery.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular-material/angular-material.min.css',
        to: 'bower_components/angular-material/angular-material.min.css'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular-material/angular-material.js',
        to: 'bower_components/angular-material/angular-material.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular-cookies/angular-cookies.js',
        to: 'bower_components/angular-cookies/angular-cookies.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular-material-icons/angular-material-icons.js',
        to: 'bower_components/angular-material-icons/angular-material-icons.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular-translate/angular-translate.js',
        to: 'bower_components/angular-translate/angular-translate.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular-translate-loader-static-files/angular-translate-loader-static-files.min.js',
        to: 'bower_components/angular-translate-loader-static-files/angular-translate-loader-static-files.min.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular-translate-storage-local/angular-translate-storage-local.min.js',
        to: 'bower_components/angular-translate-storage-local/angular-translate-storage-local.min.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular-translate-handler-log/angular-translate-handler-log.min.js',
        to: 'bower_components/angular-translate-handler-log/angular-translate-handler-log.min.js'
    },
    {
        context: './src/main/resources/static',
        from: 'bower_components/angular-translate-storage-cookie/angular-translate-storage-cookie.min.js',
        to: 'bower_components/angular-translate-storage-cookie/angular-translate-storage-cookie.min.js'
    },
    {
        context: './src/main/resources/static',
        from: 'assets/env.js',
        to: 'assets/env.js'
    },
    {
        context: './src/main/resources/static',
        from: 'assets/login.css',
        to: 'assets/login.css'
    }
];


module.exports = webpackConfig;
