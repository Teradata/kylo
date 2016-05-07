(function () {

    var controller = function($scope,$stateParams,$http,FeedService,RestUrlService, StateService){

        var self = this;


        self.registrationMethods = [{name:'Create from Nifi',description:'Register a new template that currently resides in Nifi',icon:'near_me',iconColor:'#3483BA',
            onClick:function(){
                StateService.navigateToRegisterNifiTemplate();
            }},
            {name:'Import from file',description:'Register a new template that you exported from a different Pipeline Controller Environment',icon:'file_upload',iconColor:'#F08C38',
            onClick:function(){
                StateService.navigateToImportTemplate();
            }}];



    };

    angular.module(MODULE_FEED_MGR).controller('RegisterNewTemplateController',controller);



}());