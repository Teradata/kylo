
angular.module(COMMON_APP_MODULE_NAME).directive("cardLayout", function($compile)  {
    return {
        scope: {headerCss:"@",bodyCss:"@",cardCss:'@'},
        transclude: {
            'header1':'?headerSection',
            'body1':'?bodySection'
        },
        templateUrl:'js/shared/card-layout/card-layout-template.html',
    link: function (scope, iElem, iAttrs, ctrl, transcludeFn) {

    }
};
});