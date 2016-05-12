angular.module(MODULE_FEED_MGR).factory('VisualQueryService', function ($http, $mdToast) {


    var data = {

        model : {},
        resetModel:function(){
            this.model = {};
        }

};
return data;

});