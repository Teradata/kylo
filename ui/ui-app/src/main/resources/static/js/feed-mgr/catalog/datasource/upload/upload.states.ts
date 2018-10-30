import {Ng2StateDeclaration, StateService} from "@uirouter/angular";

import {UploadComponent} from "./upload.component";

export const uploadStates: Ng2StateDeclaration[] = [
    {
        name: "catalog.datasource.upload",
        url: "/upload",
        component: UploadComponent,
        params:{renderContinueButton:true},
        resolve: [
            {
                token: "renderContinueButton",
                resolveFn: resolveTrue
            }
        ]
    }
];

export function resolveTrue() {
    return true;
}