import {StateService, Transition} from "@uirouter/angular";
import {Injectable} from "@angular/core";
import {StateDeclaration, TransitionPromise} from "@uirouter/core";

export interface VisitedState {
    state: StateDeclaration;
    params: any;
}

/**
 * Service that allows you to easily go back/forward between ui-router transitions.
 * the routes.ts class subscribes to the transition hook to save the transitions.
 * @TODO when ng2 is fully ported we should move that transition hook logic to this service
 */
@Injectable()
export class KyloRouterService {

    private states: VisitedState[] = []

    private backStates: VisitedState[] = []

    private transitions: Transition[] = []
    private maxSize: number = 10;


    private backTransitions: Transition[] = []

    constructor(private stateService: StateService) {

    }

    private _ensureCollectionLimit(collection: any[]) {
        if (collection.length > this.maxSize) {
            collection.splice(0, 1)
        }
    }

    /**
     * Save the transition in the history map to allow for going forward/back
     * @param {Transition} trans
     */
    saveTransition(trans: Transition) {
        const states = this.states;
        let visitedState = {state: trans.to(), params: trans.params()};
        this._saveState(visitedState)
    }

    private _saveState(visitedState: VisitedState, isBack: boolean = false) {
        const collection = isBack ? this.backStates : this.states;
        this._ensureCollectionLimit(collection)
        collection.push(visitedState);
    }

    /**
     * Go back to the previous transition state
     * @param {string} defaultState
     * @param defaultParams
     * @return {TransitionPromise<any> | null}
     */
    back(defaultState?: string, defaultParams?: any): (TransitionPromise | null) {
        if (this.states.length > 1) {
            //the last trans is the current page
            //pop that off, save it and then go to the prev one
            const state = this.states.pop();
            this._saveState(state, true)
            const prevState = this.states[this.states.length - 1];
            return this.stateService.go(prevState.state, prevState.params)
        }
        else {
            if (defaultState) {
                return this.stateService.go(defaultState, defaultParams)
            }
            else {
                return null;
            }
        }

    }

    /**
     * Go forward to the next transition
     * @return {TransitionPromise<any> | null}
     */
    forward(): (TransitionPromise | null) {
        if (this.backStates.length > 0) {
            const state = this.backStates.pop()
            return this.stateService.go(state.state, state.params)
        }
        else {
            return null;
        }
    }


}