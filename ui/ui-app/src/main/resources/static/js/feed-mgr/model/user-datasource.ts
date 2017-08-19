/**
 * A data source created and managed by a Kylo user.
 */
export interface UserDatasource {

    /**
     * Unique id for this data source.
     */
    id: string;

    /**
     * Human-readable name.
     */
    name: string;

    /**
     * Description of this data source.
     */
    description: string;

    /**
     * Type name of this data source.
     */
    type: string;
}
