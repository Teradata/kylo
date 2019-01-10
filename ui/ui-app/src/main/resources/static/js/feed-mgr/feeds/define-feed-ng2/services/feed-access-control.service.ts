import {Feed, FeedAccessControl} from "../../../model/feed/feed.model";
import {EntityAccessControlService} from "../../../shared/entity-access-control/EntityAccessControlService";

import {Observable} from "rxjs/Observable";
import {ReplaySubject} from "rxjs/ReplaySubject";
import {Inject, Injectable} from "@angular/core";
import {AccessControlService} from "../../../../services/AccessControlService";
import {SparkDataSet} from "../../../model/spark-data-set.model";
import * as _ from "underscore";


@Injectable()
export class FeedAccessControlService {
    


constructor(@Inject("AccessControlService") private accessControlService:AccessControlService) {}

    /**
     * check if the user has access on an entity
     * @param permissionsToCheck an Array or a single string of a permission/action to check against this entity and current user
     * @param entity the entity to check. if its undefined it will use the current feed in the model
     * @returns {*} a promise, or a true/false.  be sure to wrap this with a $q().when()
     */
    hasEntityAccess(permissionsToCheck: any, feed:Feed){
        return this.accessControlService.hasEntityAccess(permissionsToCheck, feed, EntityAccessControlService.entityRoleTypes.FEED);
    }

    
    accessControlCheck(feed:Feed) :Observable<FeedAccessControl> {

            let subject = new ReplaySubject<FeedAccessControl>(1);
            var entityAccessControlled = this.accessControlService.isEntityAccessControlled();


            // Fetch allowed permissions
            this.accessControlService.getUserAllowedActions()
                .then((actionSet: any) => {

                    let entityEditAccess = !entityAccessControlled || this.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS,feed)
                    let entityExportAccess = !entityAccessControlled || this.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EXPORT,feed)
                    let entityStartAccess = !entityAccessControlled || this.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.START,feed)
                    let entityPermissionAccess = !entityAccessControlled || this.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.CHANGE_FEED_PERMISSIONS,feed)

                    let allowDatasourceAccess =  this.accessControlService.hasAction(AccessControlService.DATASOURCE_ACCESS, actionSet.actions);
                    let allowEditAccess =  this.accessControlService.hasAction(AccessControlService.FEEDS_EDIT, actionSet.actions);
                    let allowAdminAccess =  this.accessControlService.hasAction(AccessControlService.FEEDS_ADMIN, actionSet.actions);
                    let slaAccess =  this.accessControlService.hasAction(AccessControlService.SLA_ACCESS, actionSet.actions);
                    let allowExport = this.accessControlService.hasAction(AccessControlService.FEEDS_EXPORT, actionSet.actions);
                    let allowStart = allowEditAccess;
                    let datasourceAccess = true;
                    if(Array.isArray(feed.sourceDataSets) && feed.sourceDataSets.length > 0) {
                        datasourceAccess = _.every(feed.sourceDataSets, (ds:SparkDataSet) => !_.isUndefined(ds.dataSource) && !_.isUndefined(ds.dataSource.id)) && allowDatasourceAccess;
                    }

                    let accessMessage = "";
                    if(entityEditAccess && !datasourceAccess){
                       accessMessage = "FeedDefinition.AccessControl.EditNoDatasourceAccess";
                    }

                    subject.next( new FeedAccessControl({
                        accessMessage:accessMessage,
                    datasourceAccess:datasourceAccess,
                    allowEdit : entityEditAccess && (allowEditAccess || allowAdminAccess) && datasourceAccess,
                    allowChangePermissions : entityAccessControlled && entityPermissionAccess && (allowEditAccess || allowAdminAccess),
                    allowAdmin : allowAdminAccess,
                    allowSlaAccess : slaAccess,
                    allowExport : entityExportAccess && allowExport,
                    allowStart : entityStartAccess && allowStart
                    }));

               });
            return subject.asObservable();
    }
}
