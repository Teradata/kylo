import {ITdDataTableColumn} from '@covalent/core/data-table';

/**
 * Browser column description
 */
export interface BrowserColumn extends ITdDataTableColumn {
    /**
     * Whether or not an icon should be displayed
     */
    icon?: boolean;
}