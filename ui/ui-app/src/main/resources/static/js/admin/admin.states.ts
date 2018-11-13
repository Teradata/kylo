import { Ng2StateDeclaration } from "@uirouter/angular";
import AccessConstants from "../constants/AccessConstants";
import {JcrQueryComponent} from './jcr/jcr-query.component';
import {ClusterComponent} from './cluster/cluster.component';

export const states: Ng2StateDeclaration[] = [
    {
        name: 'jcr-query',
        url: "/admin/jcr-query",
        views: {
            "content": {
                component: JcrQueryComponent
            }
        },
        data: {
            breadcrumbRoot:false,
            displayName:'JCR Admin',
            permissions:AccessConstants.UI_STATES.JCR_ADMIN.permissions
        }
    },
    {
        name: 'cluster',
        url: "/admin/cluster",
        views: {
            "content": {
                component: ClusterComponent
            }
        },
        data: {
            breadcrumbRoot:false,
            displayName:'Kylo Cluster',
            permissions:[]
        }
    }
];