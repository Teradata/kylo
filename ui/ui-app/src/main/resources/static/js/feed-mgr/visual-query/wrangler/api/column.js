define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Indicates a data type supported by a Wrangler engine.
     */
    var DataType = /** @class */ (function () {
        /**
         * Constructs a DataType.
         *
         * @param _value - internal identifier for this data type
         * @param _name - a human-readable name
         * @param _icon - name of the icon
         */
        function DataType(_value, _name, _icon) {
            if (_icon === void 0) { _icon = null; }
            this._value = _value;
            this._name = _name;
            this._icon = _icon;
        }
        Object.defineProperty(DataType.prototype, "icon", {
            /**
             * Gets the name.
             */
            get: function () {
                return this._icon;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(DataType.prototype, "name", {
            /**
             * A human-readable name.
             */
            get: function () {
                return this._name;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(DataType.prototype, "value", {
            /**
             * Identifier for this data type.
             */
            get: function () {
                return this._value;
            },
            enumerable: true,
            configurable: true
        });
        return DataType;
    }());
    exports.DataType = DataType;
});
//# sourceMappingURL=column.js.map