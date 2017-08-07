var proxy = require("http-proxy-middleware");
var proxyFilter = function (pathName, req) {
    return pathName.match(/^\/api|^\/login$|^\/logout$|^\/proxy/);
};

module.exports = {
    server: {
        baseDir: "src/main/resources/static",
        middleware: [
            proxy(proxyFilter, {target: "http://localhost:8400/"})
        ]
    }
};
