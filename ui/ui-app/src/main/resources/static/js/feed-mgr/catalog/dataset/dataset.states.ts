import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {catchError} from "rxjs/operators/catchError";
import {finalize} from "rxjs/operators/finalize";
import {TdLoadingService} from '@covalent/core/loading';
import {CatalogService} from '../api/services/catalog.service';
import {DatasetComponent} from './dataset.component';

export function resolveDataset(catalog: CatalogService, state: StateService, loading: TdLoadingService) {
    loading.register(DatasetComponent.LOADER);
    let id = state.transition.params().datasetId;
    return catalog.getDataset(id)
        .pipe(finalize(() => loading.resolve(DatasetComponent.LOADER)))
        .pipe(catchError(() => {
            return state.go("catalog")
        }))
        .toPromise();
}

export const datasetStates: Ng2StateDeclaration[] = [
    {
        name: "catalog.dataset",
        url: "/dataset/:datasetId",
        component: DatasetComponent,
        resolve: [
            {
                token: "dataset",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: resolveDataset
            },
        ],
        data:{
            permissionsKey:"DATASOURCES"
        }
    },
];
