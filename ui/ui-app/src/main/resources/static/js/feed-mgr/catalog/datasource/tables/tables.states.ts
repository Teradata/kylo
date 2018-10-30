import {Ng2StateDeclaration} from "@uirouter/angular";

import {TablesComponent} from "./tables.component";
import {StateService} from "@uirouter/angular";

export function resolveParams(state: StateService) {
    const params: any = {};
    const transParams: any = state.transition.params();
    if (transParams.catalog && transParams.catalog != "") {
        params.catalog = transParams.catalog;
    }
    if (transParams.schema && transParams.schema != "") {
        params.schema = transParams.schema;
    }
    return params;
}


export const tablesStates: Ng2StateDeclaration[] = [
    {
        name: "catalog.datasource.connection",
        url: "/tables?catalog=:c&schema=:s",
        component: TablesComponent,
        params:{"catalog":{type:"string",isOptional:true}, "schema":{type:"string",isOptional:true}},
        resolve: [
            {
                token: "params",
                deps: [StateService],
                resolveFn: resolveParams
            }
        ]
    }
];
