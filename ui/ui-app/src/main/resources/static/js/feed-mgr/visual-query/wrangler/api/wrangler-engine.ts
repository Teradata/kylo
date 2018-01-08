import {Observable} from "rxjs/Observable";

import {SaveRequest, SaveResponse} from "./rest-model";

/**
 * Provides the ability to query and transform data.
 */
export interface WranglerEngine {

    /**
     * Saves the results to the specified destination.
     *
     * @param request - save target
     * @returns an observable tracking the save status
     */
    saveResults(request: SaveRequest): Observable<SaveResponse>;
}
