define(['angular'], function (angular) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                mode: '@',
                processor: '=',
                theForm: '='
            },
            controllerAs: 'ctl',
            scope: {},
            templateUrl: 'js/plugin/processor-templates/GetFile/get-file.html',
            controller: "GetFileProcessorController",
            link: function ($scope, element, attrs, controllers) {
            }

        };

    }
    var controller =  function($scope,$q,$http,$mdToast, $interval) {

        /**
         * A list of icons to cycle through
         * @type {[*]}
         */
        this.icons = ['event','build','bug_report','backup','book','dashboard'];
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
         * point back to this controller
         * @type {controller}
         */
        var self = this;

        /**
         * When a property changes respond and make a toast
         * @param property
         */
        this.onPropertyChange = function(property){
            $mdToast.show(
                $mdToast.simple()
                    .textContent('Property '+property.key+" Changed ")
                    .hideDelay(3000)
            );
        }


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

    }


var moduleName = "kylo.plugin.processor-template.get-file";
angular.module(moduleName, [])
angular.module(moduleName).controller('GetFileProcessorController',["$scope","$q","$http","$mdToast","$interval", controller]);

angular.module(moduleName)
    .directive('kyloGetFileProcessor', directive);

});

