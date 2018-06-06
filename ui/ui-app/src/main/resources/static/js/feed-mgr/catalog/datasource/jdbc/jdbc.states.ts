import {Ng2StateDeclaration} from "@uirouter/angular";

import {JdbcComponent} from "./jdbc.component";
import {StateService} from "@uirouter/angular";

export const jdbcStates: Ng2StateDeclaration[] = [
    {
        name: "catalog.datasource.connection",
        url: "/tables?catalog=:c&schema=:s",
        component: JdbcComponent,
        resolve: [
            {
                token: "params",
                deps: [StateService],
                resolveFn: (state: StateService) => {
                    const params: any = {};
                    const transParams: any = state.transition.params();
                    if (transParams.catalog) {
                        params.catalog = transParams.catalog;
                    }
                    if (transParams.schema) {
                        params.schema = transParams.schema;
                    }
                    console.log('resolve table params', params);
                    return params;
                }
            }
        ]
    }
];
