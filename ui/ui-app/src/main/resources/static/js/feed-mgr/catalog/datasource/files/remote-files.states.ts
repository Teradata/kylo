import {Ng2StateDeclaration} from "@uirouter/angular";

import {RemoteFilesComponent} from "./remote-files.component";
import {StateService} from "@uirouter/angular";

export const remoteFileStates: Ng2StateDeclaration[] = [
    {
        name: "catalog.datasource.browse",
        url: "/browse?path=:browseLocation",
        component: RemoteFilesComponent,
        resolve: [
            {
                token: "path",
                deps: [StateService],
                resolveFn: (state: StateService) => {
                    let browseLocation = state.transition.params().path;
                    if(browseLocation != undefined) {
                        return decodeURIComponent(browseLocation);
                    }else {
                        return browseLocation;
                    }
                }
            }
        ]
    }
];
