import * as angular from 'angular';
import * as _ from "underscore";
import { moduleName } from "../../module-name";
import { RegisterTemplateServiceFactory } from '../../../services/RegisterTemplateServiceFactory';
import {AccessControlService} from '../../../../services/AccessControlService';
import { EntityAccessControlService } from '../../../shared/entity-access-control/EntityAccessControlService';


export class TemplateAccessControlController {


    /**
    * ref back to this controller
    * @type {TemplateAccessControlController}
    */
    templateAccessControlForm: any = {};
    model: any;
    allowEdit: boolean = false;

    static readonly $inject = ["$scope", "RegisterTemplateService", "AccessControlService", "EntityAccessControlService"];
    constructor(private $scope: IScope, private registerTemplateService: RegisterTemplateServiceFactory, private accessControlService: AccessControlService, private entityAccessControlService: EntityAccessControlService) {

        this.model = this.registerTemplateService.model;


        //allow edit if the user has ability to change permissions on the entity if its an existing registered template, or if it is new
        if (this.model.id && this.registerTemplateService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.TEMPLATE.CHANGE_TEMPLATE_PERMISSIONS)) {
            this.allowEdit = true;
        }
        else {
            this.allowEdit = this.model.id == undefined;
        }
    }
}
angular.module(moduleName).component("thinkbigTemplateAccessControl", {
    bindings: {
        stepIndex: '@'
    },
    controllerAs: 'vm',
    templateUrl: './template-access-control.html',
    controller: TemplateAccessControlController,

});

