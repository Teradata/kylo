import {Ng2StateDeclaration} from "@uirouter/angular";

import {JdbcComponent} from "./jdbc.component";
import {StateService} from "@uirouter/angular";

export const jdbcStates: Ng2StateDeclaration[] = [
    {
        name: "catalog.datasource.connection",
        url: "/tables",
        component: JdbcComponent,
        // resolve: [
        //     {
        //         token: "path",
        //         deps: [StateService],
        //         resolveFn: (state: StateService) => {
        //             let browseLocation = state.transition.params().path;
        //             return decodeURIComponent(browseLocation);
        //         }
        //     }
        // ]
    }
];
