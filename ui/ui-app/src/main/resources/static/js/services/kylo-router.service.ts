import {StateService, Transition} from "@uirouter/angular";
import {Injectable} from "@angular/core";
import {TransitionPromise} from "@uirouter/core";

/**
 * Service that allows you to easily go back/forward between ui-router transitions.
 * the routes.ts class subscribes to the transition hook to save the transitions.
 * @TODO when ng2 is fully ported we should move that transition hook logic to this service
 */
@Injectable()
export class KyloRouterService {


    private transitions: Transition[] = []
    private maxSize: number = 10;


    private backTransitions: Transition[] = []

    constructor(private stateService: StateService) {

    }

    /**
     * Save the transition in the history map to allow for going forward/back
     * @param {Transition} trans
     */
    saveTransition(trans: Transition, isBack: boolean = false) {
        const collection = isBack ? this.backTransitions : this.transitions;

        if (collection.length > this.maxSize) {
            collection.splice(0, 1)
        }
        collection.push(trans);
    }

    /**
     * Go back to the previous transition state
     * @param {string} defaultState
     * @param defaultParams
     * @return {TransitionPromise<any> | null}
     */
    back(defaultState?: string, defaultParams?: any): (TransitionPromise | null) {
        if (this.transitions.length > 1) {
            //the last trans is the current page
            //pop that off, save it and then go to the prev one
            const trans = this.transitions.pop();
            this.saveTransition(trans, true);

            const lastTrans = this.transitions[this.transitions.length - 1];
            return this.stateService.go(lastTrans.from(), lastTrans.params())
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
        if (this.backTransitions.length > 0) {
            const trans = this.backTransitions.pop()
            return this.stateService.go(trans.from(), trans.params())
        }
        else {
            return null;
        }
    }


}