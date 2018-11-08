import * as angular from "angular";
import {moduleName} from "../module-name";
import {TableController} from "../../feed-mgr/tables/TableController";


export class LoadingDialogController {

    constructor(private $scope: any,
                private $mdDialog: any) {

    }
}

export class LoadingDialogService {

    constructor(private $mdDialog) {}

    dialogRef:any;
    showDialog(){
        this.dialogRef =this.$mdDialog.show({
            controller: 'LoadingDialog',
            templateUrl: './loading-dialog.html',
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            fullscreen: true
        })
    }

    hideDialog(){
        if(this.dialogRef){
            this.$mdDialog.hide(this.dialogRef);
        }
    }


}

angular.module(moduleName).controller('LoadingDialog',["$scope","$mdDialog",LoadingDialogController]);

angular.module(moduleName).service('LoadingDialogService',["$mdDialog",LoadingDialogService]);