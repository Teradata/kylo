import {Component, Inject, Input, OnInit} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {UserGroupService} from "../../../services/UserGroupService";
import {EntityAccessControlService} from "./EntityAccessControlService";
import {AccessControlService} from "../../../services/AccessControlService";
import {Observable} from 'rxjs/Observable';
import * as _ from "underscore";
import {of} from "rxjs/observable/of";
import {fromPromise} from "rxjs/observable/fromPromise";
import {map} from "rxjs/operators/map";
import {forkJoin} from "rxjs/observable/forkJoin";
import {UserPrincipal} from "../../model/UserPrincipal";
import {GroupPrincipal} from "../../model/GroupPrincipal";
import {FormGroup} from "@angular/forms";

@Component ( {
    selector: "entity-access-control-ng2",
    templateUrl:"./entity-access-control.component.html"
    }
)

export class EntityAccessControlComponent implements OnInit {
    @Input()
    entity:any;

    @Input('readonly')
    readOnly:boolean;

    @Input()
    entityType:string;

    @Input()
    public parentForm: FormGroup;

    public entityAccessControlForm: FormGroup;

    enabled: boolean = false;
    roleMembershipsProperty: string;
    allowOwnerChange: boolean;
    entityRoleMemberships: any;
    queryForEntityAccess: any;
    rolesInitialized: any;
    ownerAutoComplete: any;

    /**
     * Cache of the group names for filtering
     * @type {null}
     */
    allGroups: any = null;

    /**
     * Cache of the user names for filtering
     * @type {null}
     */
    allUsers: any = null;

    roles: any = null;

    accessControlService: AccessControlService;
    userGroupService: UserGroupService;
    entityAccessControlService: EntityAccessControlService;
    http: HttpClient;
    selectedRoles: Observable<any>;

    constructor(http: HttpClient,
                @Inject("UserGroupService") userGroupService: UserGroupService,
                @Inject("EntityAccessControlService") entityAccessControlService: EntityAccessControlService,
                @Inject("AccessControlService") accessControlService: AccessControlService) {
        this.userGroupService = userGroupService;
        this.entityAccessControlService = entityAccessControlService;
        this.accessControlService = accessControlService;
        this.entityAccessControlForm = new FormGroup({});
    }

    ngOnInit(): void {

        if (_.isUndefined(this.readOnly)) {
            this.readOnly = false;
        }

        if(_.isUndefined(this.roleMembershipsProperty)){
            this.roleMembershipsProperty = "roleMemberships";
        }

        if(_.isUndefined(this.allowOwnerChange)){
            this.allowOwnerChange = true;
        }

        this.checkEntityAccess();

        if (_.isUndefined(this.entity[this.roleMembershipsProperty])) {
            this.entity[this.roleMembershipsProperty] = [];
        }

        this.entityRoleMemberships = this.entity[this.roleMembershipsProperty];

        if (_.isUndefined(this.queryForEntityAccess)) {
            this.queryForEntityAccess = false;
        }

        if (_.isUndefined(this.entity.owner) || this.entity.owner == null) {
            this.entity.owner = null;

            //assign it the current user
            let requests = {
                currentUser: this.userGroupService.getCurrentUser(),
                allUsers: this.getAllUsers()
            };

            forkJoin([this.getAllUsers(), this.userGroupService.getCurrentUser()])
                .subscribe(([allUsers, currentUser]) => {
                    let matchingUsers = this.filterCollection(allUsers, currentUser.systemName, ['_lowerDisplayName', '_lowerSystemName']);
                    if (matchingUsers) {
                        this.entity.owner = matchingUsers[0];
                    }
                });
        }

        /**
         * Flag that the user has updated the role memberships
         * @type {boolean}
         */
        this.entity.roleMembershipsUpdated = _.isUndefined(this.entity.roleMembershipsUpdated) ? false : this.entity.roleMembershipsUpdated;

        /**
         * Flag to indicate we should query for the roles from the server.
         * If the entity has already been marked as being updated, then mark this as initialized so it doesn't lose the in-memory settings the user has applied
         * @type {boolean}
         */
        this.rolesInitialized = this.queryForEntityAccess == true ? false : (this.entity.roleMembershipsUpdated == true);

        /**
         * Owner autocomplete model
         * @type {
         *          {
         *              searchText: string,
         *              searchTextChanged: controller.ownerAutoComplete.searchTextChanged,
         *              selectedItemChange: controller.ownerAutoComplete.selectedItemChange
         *          }
         *      }
         */
        this.ownerAutoComplete = {
            searchText: '',
            searchTextChanged: (query: any) => { },
            selectedItemChange: (item: any) => {
                if (item != null && item != undefined) {
                    this.entity.owner = item;
                }
                else {
                    this.entity.owner = null;
                }

            }
        };

        this.init();
    }

    /**
     * Filter the groups or users based upon the supplied query
     * @param collection
     * @param query
     * @param keys
     * @returns {Array}
     */
    filterCollection = (collection: any, query: any, keys: any) => {
        return query ? _.filter(collection, (item) => {
            let lowercaseQuery = query.toLowerCase();
            let found = _.find(keys, (key: any) => {
                return (item[key].indexOf(lowercaseQuery) === 0);
            });
            return found != undefined;
        }) : [];
    };

    /**
     * If an attempt is made to remove a non-editable member of a role membership then
     * re-add that member to the membership set.
     */
    onRemovedMember = (member: any, members: any) => {
        if (member.editable == false) {
            members.unshift(member);
        }
    };


    queryUsersAndGroups(query: any):any {
        this.entity.roleMembershipsUpdated = true;
        var request = {
                        groups: this.getAllGroups(),
                        users: this.getAllUsers()
                    };
        this.selectedRoles = forkJoin([this.getAllGroups(), this.getAllUsers()])
            .pipe(map(([groups, users]) => {
                var matchingGroups = this.filterCollection(groups, query, ['_lowername']);
                var matchingUsers = this.filterCollection(users, query, ['_lowerDisplayName', '_lowerSystemName']);
                var arr = matchingGroups.concat(matchingUsers);
                return arr;
            }))
    }

    queryUsers (query: any) {
        this.getAllUsers().pipe(map((users: any) => {
            var matchingUsers = this.filterCollection(users, query, ['_lowerDisplayName', '_lowerSystemName']);
            return matchingUsers;
        }));
    }


    getAllGroups(): Observable<GroupPrincipal> {
        if (this.allGroups == null) {
            return fromPromise(
            this.userGroupService.getGroups()
            ).pipe(map((groups: any) => {
                this.allGroups = _.map(groups, (item: any) => {
                    item._lowername = (item.title == null || _.isUndefined(item.title)) ? item.systemName.toLowerCase() : item.title.toLowerCase();
                    item.type = 'group'
                    return item;
                });
                return this.allGroups;
            }))
        } else {
            return of (this.allGroups);
        }
    };

    getAllUsers(): Observable<UserPrincipal> {
        if (this.allUsers == null) {
            return fromPromise(
                this.userGroupService.getUsers()
            ).pipe(map((users: any) => {
                this.allUsers = _.map(users, (user: any) => {
                    var name = (_.isString(user.displayName) && user.displayName.length > 0) ? user.displayName : user.systemName;
                    user.name = name;
                    user.displayName = name;
                    user.title = name;
                    user.type = 'user';
                    user._lowername = name.toLowerCase();
                    user._lowerSystemName = user.systemName.toLowerCase()
                    user._lowerDisplayName = _.isString(user.displayName) ? user.displayName.toLowerCase() : '';
                    return user;
                });
                return this.allUsers;
            }))
        } else {
            return of(this.allUsers);
        }
    };

    init() {
        fromPromise(this.entityAccessControlService.mergeRoleAssignments(this.entity, this.entityType, this.entity[this.roleMembershipsProperty]))
            .subscribe(() => {
                this.rolesInitialized = true;
                this.entityRoleMemberships = this.entity[this.roleMembershipsProperty];
            });

        if (this.parentForm) {
            this.parentForm.registerControl("entityAccessControlForm", this.entityAccessControlForm);
        }
    };

    onSave() {
        //overriding
        this.entityRoleMemberships = this.entity[this.roleMembershipsProperty];
        return fromPromise(this.entityAccessControlService.saveRoleMemberships(this.entityType, this.entity.id, this.entity.roleMemberships));
    }

    checkEntityAccess() {
        let entityAccessControlCheck:Observable<boolean> = of(this.accessControlService.checkEntityAccessControlled());
        entityAccessControlCheck.subscribe((result: any) => {
            this.enabled = this.accessControlService.isEntityAccessControlled();
        }, (err: any) => {
            console.log("Error checking if entity access control is enabled");
        });
    }
}