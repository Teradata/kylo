/*
 Module for popup menu
 */
(function () {

    var directive = function ($mdDialog,AboutKyloService) {
        return {
            restrict: "E",
            scope: {
                selectedOption: "&?",
                openedMenu:"&?",
                menuIcon: "@?"
            },
            templateUrl: 'js/shared/kylo-options/kylo-options.html',
            link: function ($scope, element, attrs) {

                //default the icon to be more_vert
                if(!angular.isDefined($scope.menuIcon)){
                    $scope.menuIcon = 'more_vert';
                }

                $scope.openMenu = function($mdOpenMenu, ev) {
                    //callback
                    if($scope.openedMenu) {
                        $scope.openedMenu();
                    }

                    $mdOpenMenu(ev);
                };

                $scope.aboutKylo = function() {
                    AboutKyloService.showAboutDialog();
                    if($scope.selectedOption) {
                        $scope.selectedOption()('aboutKylo');
                    }
                }

                $scope.$on('$destroy', function () {

                });
            }
        }
    };
    angular.module(COMMON_APP_MODULE_NAME)
        .directive('kyloOptions', ['$mdDialog','AboutKyloService',directive]);
}());
