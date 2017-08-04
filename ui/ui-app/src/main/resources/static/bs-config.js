var proxy = require("http-proxy-middleware");

module.exports = {
    server: {
        baseDir: "src/main/resources/static",
        middleware: [
            proxy("/api", {target: "http://localhost:8400/"}),
            proxy("/proxy", {target: "http://localhost:8400/"})
        ]
    }
};
