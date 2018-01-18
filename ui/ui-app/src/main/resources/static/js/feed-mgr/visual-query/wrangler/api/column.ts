/**
 * Handles user interactions with a column.
 */
export interface ColumnDelegate {

    /**
     * Casts this column to the specified type.
     */
    castTo(dataType: DataType): void;

    /**
     * Gets the target data types supported for casting this column.
     */
    getAvailableCasts(): DataType[];
}

/**
 * Indicates a data type supported by a Wrangler engine.
 */
export class DataType {

    /**
     * Constructs a DataType.
     *
     * @param _value - internal identifier for this data type
     * @param _name - a human-readable name
     * @param _icon - name of the icon
     */
    constructor(private _value: string, private _name: string, private _icon: string = null) {
    }

    /**
     * Gets the name.
     */
    get icon() {
        return this._icon;
    }

    /**
     * A human-readable name.
     */
    get name() {
        return this._name;
    }

    /**
     * Identifier for this data type.
     */
    get value() {
        return this._value;
    }
}
