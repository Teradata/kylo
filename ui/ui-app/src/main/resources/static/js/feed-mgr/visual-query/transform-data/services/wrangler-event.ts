export class WranglerEvent {

    private type_: string;

    /**
     * Constructs a new WranglerEvent of the specified type.
     */
    constructor(type: string) {
        this.type_ = type;
    }

    /**
     * Event type
     */
    get type() {
        return this.type_;
    }
}
