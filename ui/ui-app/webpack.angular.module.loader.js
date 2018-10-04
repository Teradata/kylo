//based on https://github.com/TheLarkInn/angular2-template-loader/blob/master/index.js

var path = require("path");

var stringRegex = /(['`"])((?:[^\\]\\\1|.)*?)\1/g;

function replaceModuleReferences(baseUrl, string) {
    return string.replace(stringRegex, function (match, quote, url) {
        if (url.startsWith("./")) {
            return "'" + url + "'";
        } else {
            let relative = path.relative(baseUrl, url);
            let relativePath = relative.substring('../'.length, relative.length);
            if (relativePath.charAt(0) !== ".") {
                relativePath = "./" + relativePath;
            }
            return "'" + relativePath + "'";
        }
    });
}

module.exports = function(source, sourcemap) {

    var baseUrl = '';
    if (this.query.baseUrl !== undefined) {
        baseUrl = this.query.baseUrl;
    }
    if (baseUrl.charAt(baseUrl.length) !== "/") {
        baseUrl = baseUrl + "/";
    }

    var modules = [];
    if (this.query.modules !== undefined) {
        modules = this.query.modules;
        // console.log('module loader: modules ' + JSON.stringify(modules));
    }

    var resourcePathRelativeToBaseUrl = this.resourcePath.substring(this.resourcePath.indexOf(baseUrl) + baseUrl.length, this.resourcePath.length);

    // Not cacheable during unit tests;
    this.cacheable && this.cacheable();

    var arrayLength = modules.length;
    var newSource = source;
    for (var i = 0; i < arrayLength; i++) {
        var module = modules[i]; //e.g. feed-mgr
        // var moduleRegex = /(['"`]feed-mgr\/(.*?)['"`])/g
        var moduleRegex = new RegExp('([\'"`]' + module + "\\/(.*?)['\"`])", 'g');
        newSource = newSource.replace(moduleRegex, function (match, url) {
            let replaced = replaceModuleReferences(resourcePathRelativeToBaseUrl, url);
            console.log(resourcePathRelativeToBaseUrl + ": " + url + " -> " + replaced);
            return replaced;
        });
    }

    // Support for tests
    if (this.callback) {
        this.callback(null, newSource, sourcemap)
    } else {
        return newSource;
    }
};