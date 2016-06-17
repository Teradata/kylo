app.directive("verticalSectionLayout", function()  {
    return {
        restrict: 'E',
        scope: {showVerticalCheck:'=?',allowEdit:'=?',sectionTitle:'@',formName:'@',onDelete:'&?',allowDelete:'=?', onEdit:'&', onSaveEdit:'&',onCancelEdit:'&',editable:'=?',keepEditableAfterSave:'=?'},
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
            if($scope.keepEditableAfterSave == undefined){
                $scope.keepEditableAfterSave = false;
            }



            $scope.edit = function(ev){
                $scope.editable = true;
                $scope.onEdit(ev);
            }

            $scope.cancel = function(ev){
                $scope.onCancelEdit(ev);
                $scope.editable = false;
            }

            $scope.save = function(ev){
                $scope.onSaveEdit(ev);
                if(!$scope.keepEditableAfterSave) {
                    $scope.editable = false;
                }
            }

            $scope.delete = function(ev) {
                if($scope.onDelete){
                    $scope.onDelete(ev);
                }
            }




        }
    };
});