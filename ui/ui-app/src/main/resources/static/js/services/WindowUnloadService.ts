import {TransitionService} from "@uirouter/core/lib";
import * as angular from "angular";

export class WindowUnloadService {

    /**
     * Confirmation text to be displayed on an unload event.
     */
    private text_: string | null;

    static readonly $inject = ["$transitions", "$window"];

    constructor(private $transitions: TransitionService, $window: Window) {
        // Setup event listeners
        $transitions.onBefore({}, () => this.shouldChangeState());
        $window.onbeforeunload = (e: BeforeUnloadEvent) => this.onBeforeUnload(e);
    }

    /**
     * Disables the confirmation dialog.
     */
    clear() {
        this.text_ = null;
    }

    /**
     * Called to setup the confirmation dialog on an unload event.
     *
     * @param e - the unload event
     * @returns the dialog text to be displayed, or {@code null} to allow the event
     */
    onBeforeUnload(e: BeforeUnloadEvent): string | null {
        if (this.text_ !== null) {
            e.returnValue = this.text_;
        }
        return this.text_;
    }

    /**
     * Enables the confirmation dialog and sets the dialog text.
     *
     * @param text - the dialog text
     */
    setText(text: string) {
        this.text_ = text;
    }

    /**
     * Called when changing states.
     */
    shouldChangeState() {
        if (this.text_ === null || confirm(this.text_)) {
            this.clear();
            return true;
        } else {
            return false;
        }
    }
}

angular.module(require("services/module-name")).service("WindowUnloadService", WindowUnloadService);
