import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { DatasourcesTableComponent } from "./DatasourcesTableComponent";
import { DatasourcesDetailsComponent } from "./DatasourcesDetailsComponent";

export const datasourcesStates: Ng2StateDeclaration[] = [
    {
        name: AccessConstants.UI_STATES.DATASOURCES.state,
        url: "/datasources",
        views: {
            "content": {
                component: DatasourcesTableComponent
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Data Sources",
            permissions: AccessConstants.UI_STATES.DATASOURCES.permissions
        }
    },
    {
        name: AccessConstants.UI_STATES.DATASOURCE_DETAILS.state,
        url: "/datasource-details/{datasourceId}",
        views: {
            "content": {
                component: DatasourcesDetailsComponent
            }
        },
        data: {
            breadcrumbRoot: false,
            displayName: "Data Source Details",
            permissions: AccessConstants.UI_STATES.DATASOURCE_DETAILS.permissions
        },
        params: {
            datasourceId: null
        }
    }
]; 