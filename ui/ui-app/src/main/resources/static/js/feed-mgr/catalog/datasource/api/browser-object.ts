import {BrowserColumn} from './browser-column';

export class BrowserObject {
    name: string;
    path: string;

    /**
     * Whether or not this object can be parent, i.e. have children.
     * This determines whether user should be able to click on this object and navigate to its children
     * @returns {boolean}
     */
    canBeParent(): boolean {
        return false;
    }

    /**
     * @param {BrowserColumn} column so that different icons can be returned for different columns
     * @returns {string} icon name
     */
    getIcon(column: BrowserColumn): string {
        return '';
    }
}