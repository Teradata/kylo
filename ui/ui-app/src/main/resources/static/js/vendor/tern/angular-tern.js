define(["angular","ocLazyLoad","underscore", "vendor/tern/plugin/acorn"], function () {

    angular.module("tern", ['oc.lazyLoad'])
        .run(['$ocLazyLoad', function ($ocLazyLoad) {
/*            $ocLazyLoad.load({serie:true,files:[
                              "vendor/tern/plugin/acorn",
                              "vendor/tern/plugin/acorn_loose",
                              "vendor/tern/plugin/walk",
                              "vendor/tern/plugin/polyfill",
                              "vendor/tern/lib/signal",
                              "vendor/tern/lib/tern",
                              "vendor/tern/lib/def",
                              "vendor/tern/lib/comment",
                              "vendor/tern/lib/infer",
                              "vendor/tern/plugin/doc_comment"]});
*/
        }]);
});
