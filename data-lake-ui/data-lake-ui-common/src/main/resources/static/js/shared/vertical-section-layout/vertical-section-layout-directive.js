app.directive("verticalSectionLayout", function()  {
    return {
        restrict: 'E',
        scope: {showVerticalCheck:'=?',allowEdit:'=?',sectionTitle:'@',formName:'@',onDelete:'&?',allowDelete:'=?', onEdit:'&', onSaveEdit:'&',onCancelEdit:'&',editable:'=?'},
        transclude: {
            'readonly':'?readonlySection',
            'editable':'?editableSection'
        },
        templateUrl:'js/shared/vertical-section-layout/vertical-section-layout-template.html',
        link: function ($scope, iElem, iAttrs, ctrl, transcludeFn) {


             if($scope.editable == undefined ) {
                 $scope.editable = false;
            }

            if($scope.showVerticalCheck == undefined ){
                $scope.showVerticalCheck = true;
            }

            if($scope.allowEdit == undefined ){
                $scope.allowEdit = true;
            }



            $scope.edit = function(){
                $scope.editable = true;
                $scope.onEdit();
            }

            $scope.cancel = function(){
                $scope.onCancelEdit();
                $scope.editable = false;
            }

            $scope.save = function(){
                $scope.onSaveEdit();
                $scope.editable = false;
            }

            $scope.delete = function() {
                if($scope.onDelete){
                    $scope.onDelete();
                }
            }




        }
    };
});