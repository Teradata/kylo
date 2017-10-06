/**
 * Thrown to indicate that the abstract syntax tree could not be parsed.
 */
export class ParseException extends Error {

    /**
     * Constructs a {@code ParseException}.
     *
     * @param message - the error message
     * @param col - the column number
     */
    constructor(message: string, col: number = null) {
        super(message + (col !== null ? " at column number " + col : ""));
        this.name = "ParseException";
    }
}
