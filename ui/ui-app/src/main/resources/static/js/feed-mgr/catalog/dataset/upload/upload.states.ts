import {Ng2StateDeclaration} from "@uirouter/angular";

import {UploadComponent} from "./upload.component";

export const uploadStates: Ng2StateDeclaration[] = [
    {
        name: "catalog.dataset.upload",
        url: "/upload",
        component: UploadComponent
    }
];
