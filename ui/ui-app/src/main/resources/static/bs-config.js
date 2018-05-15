var proxy = require("http-proxy-middleware");
var proxyFilter = function (pathName, req) {
    return pathName.match(/^\/api|^\/login$|^\/logout$|^\/proxy/);
};

module.exports = {
    server: {
        baseDir: ["src/main/resources/static", "target/classes/static"],
        middleware: [
            proxy(proxyFilter, {target: "http://kylo-demo:8400/"})
        ]
    }
};

