var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Thrown to indicate that the abstract syntax tree could not be parsed.
     */
    var ParseException = /** @class */ (function (_super) {
        __extends(ParseException, _super);
        /**
         * Constructs a {@code ParseException}.
         *
         * @param message - the error message
         * @param col - the column number
         */
        function ParseException(message, col) {
            if (col === void 0) { col = null; }
            var _this = _super.call(this, message + (col !== null ? " at column number " + col : "")) || this;
            _this.name = "ParseException";
            return _this;
        }
        return ParseException;
    }(Error));
    exports.ParseException = ParseException;
});
//# sourceMappingURL=parse-exception.js.map