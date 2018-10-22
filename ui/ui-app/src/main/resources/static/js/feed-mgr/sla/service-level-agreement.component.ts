import * as _ from 'underscore';
import AccessControlService from '../../services/AccessControlService';
import StateService from '../../services/StateService';
import { FeedService } from '../services/FeedService';
import { DefaultPaginationDataService } from '../../services/PaginationDataService';
import { DefaultTableOptionsService } from '../../services/TableOptionsService';
import AddButtonService from '../../services/AddButtonService';
import { Input, Component } from '@angular/core';

@Component({
    selector: 'thinkbig-service-level-agreement',
    templateUrl: 'js/feed-mgr/sla/service-level-agreement.html'
})
export default class ServiceLevelAgreementController {
    
    @Input() newSla: any;
    @Input() slaId: any;
    @Input() feed: any;
    @Input() view: any;

    editRule: any = null;
    loadAll: boolean = true;

    ngOnInit() {

        if(this.view == "all"){
            this.loadAll = true;
        }else{
            this.loadAll = false;   
        }
    }
    
    constructor() {
        //if the newSLA flag is tripped then show the new SLA form and then reset it 
        }
}