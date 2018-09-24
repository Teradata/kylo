import {Feed, FeedAccessControl} from "../../../model/feed/feed.model";
import {EntityAccessControlService} from "../../../shared/entity-access-control/EntityAccessControlService";

import {Observable} from "rxjs/Observable";
import {ReplaySubject} from "rxjs/ReplaySubject";
import {Inject, Injectable} from "@angular/core";
import AccessControlService from "../../../../services/AccessControlService";


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

                    let allowEditAccess =  this.accessControlService.hasAction(AccessControlService.FEEDS_EDIT, actionSet.actions);
                    let allowAdminAccess =  this.accessControlService.hasAction(AccessControlService.FEEDS_ADMIN, actionSet.actions);
                    let slaAccess =  this.accessControlService.hasAction(AccessControlService.SLA_ACCESS, actionSet.actions);
                    let allowExport = this.accessControlService.hasAction(AccessControlService.FEEDS_EXPORT, actionSet.actions);
                    let allowStart = allowEditAccess;


                    subject.next( new FeedAccessControl({
                        allowEdit : entityEditAccess && allowEditAccess,
                    allowChangePermissions : entityAccessControlled && entityPermissionAccess && allowEditAccess,
                    allowAdmin : allowAdminAccess,
                    allowSlaAccess : slaAccess,
                    allowExport : entityExportAccess && allowExport,
                    allowStart : entityStartAccess && allowStart
                    }));

               });
            return subject.asObservable();
    }
}