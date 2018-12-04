//based on https://github.com/TheLarkInn/angular2-template-loader/blob/master/index.js

var path = require("path");

// using: regex, capture groups, and capture group variables.
var templateUrlRegex = /templateUrl\s*:(\s*['"`](.*?)['"`]\s*([,}]))/gm;
var stylesRegex = /styleUrls *:(\s*\[[^\]]*?\])/g;
var stringRegex = /(['`"])((?:[^\\]\\\1|.)*?)\1/g;

function replaceStringsWithRequires(baseUrl, string) {
    return string.replace(stringRegex, function (match, quote, url) {
        if (url.startsWith("./") || url.startsWith("../")) {
            return "require('" + url + "')";
        } else {
            let relative = path.relative(baseUrl, url);
            let relativePath = relative.substring('../'.length, relative.length);
            if (relativePath.charAt(0) !== ".") {
                relativePath = "./" + relativePath;
            }
            console.log(baseUrl + ": "  + url + " -> " + relativePath);
            return "require('" + relativePath + "')";
        }
    });
}

module.exports = function(source, sourcemap) {

    // console.log("webpack.angular.template.loader source,sourcemap", source, sourcemap);
    // console.log('this.query ', this.query);
    // console.log('this.resourcePath', this.resourcePath);

    var styleProperty = 'styles';
    var templateProperty = 'template';
    var baseUrl = '';

    // console.log("this.query", this.query);
    // console.log("this.query.basePath: " + this.query.basePath);

    if (this.query.baseUrl !== undefined) {
        baseUrl = this.query.baseUrl;
        // styleProperty = 'styleUrls';
        // templateProperty = 'templateUrl';
    }

    if (baseUrl.charAt(baseUrl.length) !== "/") {
        baseUrl = baseUrl + "/";
    }

    var resourcePathRelativeToBaseUrl = this.resourcePath.substring(this.resourcePath.indexOf(baseUrl) + baseUrl.length, this.resourcePath.length);
    // console.log('relative to base: ' + resourcePathRelativeToBaseUrl);

    // Not cacheable during unit tests;
    this.cacheable && this.cacheable();

    var newSource = source.replace(templateUrlRegex, function (match, url) {
        // console.log('match: ' + match);
        // console.log('url: ' + url);
        // console.log('url = ' + url);

        if (url.indexOf("@") !== -1)  {
            //to allow for templateUrl: '@'
            // console.log('match = ' + match);
            return match;
        }

        let prop = templateProperty + ":" + replaceStringsWithRequires(resourcePathRelativeToBaseUrl, url);
        // console.log(resourcePathRelativeToBaseUrl + ": "  + url + " -> " + prop);
        return prop;
    })
        .replace(stylesRegex, function (match, urls) {
            let prop = styleProperty + ":" + replaceStringsWithRequires(resourcePathRelativeToBaseUrl, urls);
            // console.log(urls + " -> " + prop);
            return prop;
        });

    // Support for tests
    if (this.callback) {
        this.callback(null, newSource, sourcemap)
    } else {
        return newSource;
    }
};