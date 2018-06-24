import {Ng2StateDeclaration} from "@uirouter/angular";

import {RemoteFilesComponent} from "./remote-files.component";
import {StateService} from "@uirouter/angular";

export const remoteFileStates: Ng2StateDeclaration[] = [
    {
        name: "catalog.datasource.browse",
        url: "/browse?path=:p",
        component: RemoteFilesComponent,
        resolve: [
            {
                token: "params",
                deps: [StateService],
                resolveFn: (state: StateService) => {
                    return {path: state.transition.params().path};
                }
            }
        ]
    }
];
