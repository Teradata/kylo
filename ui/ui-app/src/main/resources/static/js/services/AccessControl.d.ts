import * as angular from "angular";
declare namespace AccessControl {

    export interface EntityAccessCheck{
        allowEdit:boolean;
        allowAdmin:boolean;
        isValid:boolean;
        allowAccessControl:boolean
    }
}