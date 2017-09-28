import * as angular from "angular";
import {Subject} from "rxjs/Subject";

import {WranglerEvent} from "./wrangler-event";
import {WranglerEventType} from "./wrangler-event-type";

const moduleName: string = require("feed-mgr/visual-query/module-name");

/**
 * Handles communication between UI components and the wrangler table.
 */
export class WranglerTableService {

    /**
     * Events for the data source or parent component.
     */
    private dataSource: Subject<WranglerEvent>;

    /**
     * Events for the wrangler table.
     */
    private tableSource: Subject<WranglerEvent>;

    constructor() {
        this.init();
    }

    /**
     * Refreshes the wrangler table.
     */
    refresh() {
        this.tableSource.next(new WranglerEvent(WranglerEventType.REFRESH));
    }

    /**
     * Registers a new wrangler table.
     */
    registerTable(cb: (value: WranglerEvent) => void) {
        this.tableSource.subscribe(cb);
    }

    /**
     * Subscribes a parent component or data source.
     */
    subscribe(cb: (value: WranglerEvent) => void) {
        this.dataSource.subscribe(cb);
    }

    /**
     * Removes all event listeners and resets this service.
     */
    unsubscribe() {
        this.dataSource.unsubscribe();
        this.tableSource.unsubscribe();
        this.init();
    }

    /**
     * Resets this service.
     */
    private init() {
        this.dataSource = new Subject();
        this.tableSource = new Subject();
    }
}

angular.module(moduleName).service("WranglerTableService", WranglerTableService);
