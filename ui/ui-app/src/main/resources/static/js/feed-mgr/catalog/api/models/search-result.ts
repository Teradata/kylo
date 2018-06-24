/**
 * Searching/Filtering object to store the results and summary information about the resulting data
 */
export class SearchResult<T> {

    /**
     * Data from the search
     */
    data: T[];

    /**
     * Total record count (not filtered)
     */
    recordsTotal?: number;

    /**
     * Number of records from the recordsTotal that exist in the data as a result of the search/filter
     */
    recordsFiltered?: number;

    /**
     * Any error string message if an error was found
     */
    error?: string;
}
