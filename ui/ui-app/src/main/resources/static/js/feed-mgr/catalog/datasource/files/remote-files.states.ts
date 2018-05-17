import {Ng2StateDeclaration} from "@uirouter/angular";

import {RemoteFilesComponent} from "./remote-files.component";

export const remoteFileStates: Ng2StateDeclaration[] = [
    {
        name: "catalog.datasource.browse",
        url: "/browse",
        component: RemoteFilesComponent
    }
];
