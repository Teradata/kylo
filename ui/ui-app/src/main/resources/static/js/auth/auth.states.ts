import AccessConstants from "../constants/AccessConstants";
import UsersTableComponent from "./users/UsersTableComponent";
import { Ng2StateDeclaration } from "@uirouter/angular";
import UserDetailsComponent from "./users/user-details/UserDetailsComponent";
import GroupsTableComponent from "./groups/GroupsTableComponent";
import GroupDetailsComponent from "./groups/group-details/GroupDetailsComponent";

export const authStates: Ng2StateDeclaration[] = [
    {
        name: AccessConstants.UI_STATES.USERS.state,
        url: "/users",
        views: {
            "content": {
                component: UsersTableComponent
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Users",
            permissions: AccessConstants.UI_STATES.USERS.permissions
        }
    },
    {
        name: AccessConstants.UI_STATES.USERS_DETAILS.state,
        url: "/user-details/{user-id}",
        views: {
            "content": {
                component: UserDetailsComponent
            }
        },
        data: {
            breadcrumbRoot: false,
            displayName: "User Details",
            permissions: AccessConstants.UI_STATES.USERS_DETAILS.permissions
        },
        params: {
            userId: null
        }
    },
    {
        name: AccessConstants.UI_STATES.GROUPS.state,
        url: "/groups",
        views: {
            "content": {
                component: GroupsTableComponent
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Groups",
            permissions: AccessConstants.UI_STATES.GROUPS.permissions
        }
    },
    {
        name: AccessConstants.UI_STATES.GROUP_DETAILS.state,
        url: "/group-details/{group-id}",
        views: {
            "content": {
                component: GroupDetailsComponent
            }
        },
        data: {
            breadcrumbRoot: false,
            displayName: "User Details",
            permissions: AccessConstants.UI_STATES.GROUP_DETAILS.permissions
        },
        params: {
            groupId: null
        }
    }
]; 