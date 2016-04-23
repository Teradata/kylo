/*
 * install node
 * install gulp (follow steps 1 and 2 here: https://github.com/gulpjs/gulp/blob/master/docs/getting-started.md)
 * install angular-templatecache
 *    npm install gulp-angular-templatecache --save-dev
 * run >  /pipeline-controller-ui-common/gulp
 */
var COMMON_APP_MODULE_NAME = "datalakeui.common";

var gulp = require('gulp'),
debug = require('gulp-debug');
var templateCache = require('gulp-angular-templatecache');

gulp.task('prepare:templates', function () {
    return gulp.src(['src/main/resources/static/**/*.html','!src/main/resources/static/**/codemirror/**/*.html'])
        .pipe(templateCache({
            filename: 'templates.js',
            module: COMMON_APP_MODULE_NAME
        }))
        .pipe(debug())
        .pipe(gulp.dest('src/main/resources/static/js'));
});

gulp.task('default', ['prepare:templates']);