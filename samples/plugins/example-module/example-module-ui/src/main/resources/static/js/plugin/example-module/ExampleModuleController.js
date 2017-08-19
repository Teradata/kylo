define(['angular', 'plugin/example-module/module-name'], function (angular, moduleName) {

    var controller = function($transition$,$http,$interval,$timeout){
        var self = this;
        this.name = 'This is an example module.';

        /**
         * An array of foods for a sample selection
         * @type {Array}
         */
        this.foods = [];

        /**
         * The selected food
         * @type {string}
         */
        this.selectedFood = '';

        /**
         * A list of icons to cycle through
         * @type {[*]}
         */
        this.icons = ['free_breakfast','cake','local_dining','bug_report','mood','mood_bad','sentiment_satisfied','sentiment_neutral', 'sentiment_dissatisfied','fitness_center'];
        /**
         * a list of colors
         * @type {[*]}
         */
        this.colors= ['red','blue','green','grey','black'];
        /**
         * the selected icon
         * @type {*}
         */
        this.icon = this.icons[0];
        /**
         * the selected color
         * @type {*}
         */
        this.color = this.colors[0];


        /**
         * Get the next Icon in the list
         * @return {*}
         */
        var nextIcon = function(){
            var idx = _.indexOf(self.icons,self.icon);
            if(idx == self.icons.length -1) {
                idx = 0;
            }
            else {
                idx++;
            }
            return self.icons[idx];
        }

        /**
         * get a random color
         * @return {*}
         */
        var randomColor = function(){
            return self.colors[Math.floor(Math.random()*self.colors.length)];
        }

        /**
         * Every 3 seconds change the icon and color
         */
        $interval(function(){
            self.icon = nextIcon();
            self.color = randomColor();
        },3000)



        fetchFood();

        function fetchFood(){
            $http.get("/proxy/v1/example/module/food").then(function(response){
                if(response.data){
                    self.foods = response.data;
                }
            })
        }
    };



    angular.module(moduleName).controller('ExampleModuleController',['$transition$','$http','$interval','$timeout',controller]);

});
