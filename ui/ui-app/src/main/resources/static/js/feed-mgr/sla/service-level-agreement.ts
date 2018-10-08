import * as angular from 'angular';
import { moduleName } from './module-name';
import * as _ from 'underscore';
import AccessControlService from '../../services/AccessControlService';
import {StateService} from '../../services/StateService';
import { FeedService } from '../services/FeedService';
import { DefaultPaginationDataService } from '../../services/PaginationDataService';
import { DefaultTableOptionsService } from '../../services/TableOptionsService';
import AddButtonService from '../../services/AddButtonService';

export class ServiceLevelAgreementController implements ng.IComponentController {
    newSla: any;
    slaId: any;
    feed: any;
    view: any;
    editRule: any = null;
    loadAll: boolean = true;
    ngOnInit() {

        if(this.view == "all"){
            this.loadAll = true;
        }else{
            this.loadAll = false;   
        }
    }
    $onInit() {
        this.ngOnInit();
    }

    constructor() {
        //if the newSLA flag is tripped then show the new SLA form and then reset it 
        }
}

const module = angular.module(moduleName)
    .component('thinkbigServiceLevelAgreement',
            {
                bindings: {
                    feed: '=?',
                    newSla: '=?',
                    slaId: '=?',
                    view: '@'
                },
                controller: ServiceLevelAgreementController,
                controllerAs: 'vm',
                templateUrl: 'js/feed-mgr/sla/service-level-agreement.html',
            });
export default module;