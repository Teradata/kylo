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
define(["require", "exports", "angular", "../../wrangler/column-delegate", "fattable"], function (require, exports, angular, column_delegate_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Default font.
     */
    var DEFAULT_FONT = "10px sans-serif";
    /**
     * HTML template for header cells.
     */
    var HEADER_TEMPLATE = "js/feed-mgr/visual-query/transform-data/visual-query-table/visual-query-table-header.html";
    /**
     * Pixel unit.
     */
    var PIXELS = "px";
    var VisualQueryPainterService = /** @class */ (function (_super) {
        __extends(VisualQueryPainterService, _super);
        /**
         * Constructs a {@code VisualQueryPainterService}.
         */
        function VisualQueryPainterService($compile, $mdPanel, $scope, $templateCache, $templateRequest, $timeout, $window) {
            var _this = _super.call(this) || this;
            _this.$compile = $compile;
            _this.$mdPanel = $mdPanel;
            _this.$scope = $scope;
            _this.$templateCache = $templateCache;
            _this.$templateRequest = $templateRequest;
            _this.$timeout = $timeout;
            _this.$window = $window;
            /**
             * Indicates that the menu should be visible.
             */
            _this.menuVisible = false;
            /**
             * Indicates that the tooltip should be visible.
             */
            _this.tooltipVisible = false;
            $templateRequest(HEADER_TEMPLATE);
            // Hide tooltip on scroll. Skip Angular change detection.
            window.addEventListener("scroll", function () {
                if (_this.tooltipVisible) {
                    _this.hideTooltip();
                }
            }, true);
            // Create menu
            _this.menuPanel = $mdPanel.create({
                animation: _this.$mdPanel.newPanelAnimation().withAnimation({ open: 'md-active md-clickable', close: 'md-leave' }),
                attachTo: angular.element(document.body),
                clickOutsideToClose: true,
                escapeToClose: true,
                focusOnOpen: true,
                panelClass: "_md md-open-menu-container md-whiteframe-z2 visual-query-menu",
                templateUrl: "js/feed-mgr/visual-query/transform-data/visual-query-table/cell-menu.template.html"
            });
            _this.menuPanel.attach();
            // Create tooltip
            _this.tooltipPanel = $mdPanel.create({
                animation: _this.$mdPanel.newPanelAnimation().withAnimation({ open: "md-show", close: "md-hide" }),
                attachTo: angular.element(document.body),
                template: "{{value}}<ul><li ng-repeat=\"item in validation\">{{item.rule}}: {{item.reason}}</li></ul>",
                focusOnOpen: false,
                panelClass: "md-tooltip md-origin-bottom visual-query-tooltip",
                propagateContainerEvents: true,
                zIndex: 100
            });
            _this.tooltipPanel.attach();
            return _this;
        }
        Object.defineProperty(VisualQueryPainterService.prototype, "delegate", {
            /**
             * Gets the Visual Query Component for this painter.
             */
            get: function () {
                return this._delegate;
            },
            set: function (value) {
                this._delegate = value;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(VisualQueryPainterService.prototype, "domainTypes", {
            /**
             * Gets the list of available domain types.
             */
            get: function () {
                return this._domainTypes;
            },
            set: function (value) {
                this._domainTypes = value;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(VisualQueryPainterService.prototype, "headerFont", {
            /**
             * Gets the font for the header row.
             */
            get: function () {
                return (this._headerFont != null) ? this._headerFont : DEFAULT_FONT;
            },
            set: function (value) {
                this._headerFont = value;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(VisualQueryPainterService.prototype, "rowFont", {
            /**
             * Gets the font for the data rows.
             */
            get: function () {
                return (this._rowFont != null) ? this._rowFont : DEFAULT_FONT;
            },
            set: function (value) {
                this._rowFont = value;
            },
            enumerable: true,
            configurable: true
        });
        /**
         * Fills and style a cell div.
         *
         * @param {HTMLElement} cellDiv the cell <div> element
         * @param {VisualQueryTableCell|null} cell the cell object
         */
        VisualQueryPainterService.prototype.fillCell = function (cellDiv, cell) {
            // Adjust padding based on column number
            if (cell !== null && cell.column === 0) {
                cellDiv.style.paddingLeft = VisualQueryPainterService.COLUMN_PADDING_FIRST + PIXELS;
                cellDiv.style.paddingRight = 0 + PIXELS;
            }
            else {
                cellDiv.style.paddingLeft = VisualQueryPainterService.COLUMN_PADDING + PIXELS;
                cellDiv.style.paddingRight = VisualQueryPainterService.COLUMN_PADDING + PIXELS;
            }
            // Set style
            if (cell === null) {
                cellDiv.className = "";
            }
            else if (cell.validation) {
                cellDiv.className = "invalid";
            }
            else if (cell.value === null) {
                cellDiv.className = "null";
            }
            else {
                cellDiv.className = "";
            }
            // Set contents
            if (cell === null) {
                cellDiv.textContent = "";
            }
            else if (cell.value !== null && cell.value.sqltypeName && cell.value.sqltypeName.startsWith("PERIOD")) {
                cellDiv.textContent = "(" + cell.value.attributes.join(", ") + ")";
            }
            else {
                cellDiv.textContent = cell.value;
            }
            if (cell !== null) {
                angular.element(cellDiv)
                    .data("column", cell.column)
                    .data("validation", cell.validation);
            }
        };
        /**
         * Fills and style a column div.
         *
         * @param {HTMLElement} headerDiv the header <div> element
         * @param {VisualQueryTableHeader|null} header the column header
         */
        VisualQueryPainterService.prototype.fillHeader = function (headerDiv, header) {
            // Adjust padding based on column number
            if (header !== null && header.index === 0) {
                headerDiv.style.paddingLeft = VisualQueryPainterService.COLUMN_PADDING_FIRST + PIXELS;
                headerDiv.style.paddingRight = 0 + PIXELS;
            }
            else {
                headerDiv.style.paddingLeft = VisualQueryPainterService.COLUMN_PADDING + PIXELS;
                headerDiv.style.paddingRight = VisualQueryPainterService.COLUMN_PADDING + PIXELS;
            }
            // Update scope in a separate thread
            var $scope = angular.element(headerDiv).scope();
            if ($scope.header !== header) {
                $scope.availableDomainTypes = this.domainTypes;
                $scope.domainType = header.domainTypeId ? this.domainTypes.find(function (domainType) { return domainType.id === header.domainTypeId; }) : null;
                $scope.header = header;
                $scope.header.unsort = this.unsort.bind(this);
                $scope.table = this.delegate;
            }
        };
        /**
         * Hides the tooltip.
         */
        VisualQueryPainterService.prototype.hideTooltip = function () {
            var _this = this;
            this.tooltipVisible = false;
            this.$timeout(function () {
                if (_this.tooltipVisible === false) {
                    _this.tooltipPanel.hide();
                }
            }, 75);
        };
        /**
         * Setup method are called at the creation of the cells. That is during initialization and for all window resize event.
         *
         * Cells are recycled.
         *
         * @param {HTMLElement} cellDiv the cell <div> element
         */
        VisualQueryPainterService.prototype.setupCell = function (cellDiv) {
            var _this = this;
            angular.element(cellDiv)
                .on("contextmenu", function () { return false; })
                .on("mousedown", function () { return _this.setSelected(cellDiv); })
                .on("mouseenter", function () { return _this.showTooltip(cellDiv); })
                .on("mouseleave", function () { return _this.hideTooltip(); })
                .on("mouseup", function (event) { return _this.showMenu(cellDiv, event); });
            cellDiv.style.font = this.rowFont;
            cellDiv.style.lineHeight = VisualQueryPainterService.ROW_HEIGHT + PIXELS;
        };
        /**
         * Setup method are called at the creation of the column header. That is during initialization and for all window resize event.
         *
         * Columns are recycled.
         *
         * @param {HTMLElement} headerDiv the header <div> element
         */
        VisualQueryPainterService.prototype.setupHeader = function (headerDiv) {
            // Set style attributes
            headerDiv.style.font = this.headerFont;
            headerDiv.style.lineHeight = VisualQueryPainterService.HEADER_HEIGHT + PIXELS;
            // Load template
            headerDiv.innerHTML = this.$templateCache.get(HEADER_TEMPLATE);
            this.$compile(headerDiv)(this.$scope.$new(true));
        };
        /**
         * Cleanup any events attached to the header
         * @param headerDiv
         */
        VisualQueryPainterService.prototype.cleanUpHeader = function (headerDiv) {
            var scope = angular.element(headerDiv).scope();
            if (scope) {
                scope.$destroy();
            }
        };
        /**
         * Cleanup any events attached to the cell
         * @param cellDiv
         */
        VisualQueryPainterService.prototype.cleanUpCell = function (cellDiv) {
            angular.element(cellDiv).unbind();
        };
        /**
         * Called when the table is refreshed
         * This should cleanup any events/bindings/scopes created by the prior render of the table
         * @param table
         */
        VisualQueryPainterService.prototype.cleanUp = function (table) {
            _super.prototype.cleanUp.call(this, table);
            angular.element(table).unbind();
        };
        /**
         * Hides the cell menu.
         */
        VisualQueryPainterService.prototype.hideMenu = function () {
            var _this = this;
            this.menuVisible = false;
            this.$timeout(function () {
                if (_this.menuVisible === false) {
                    _this.menuPanel.close();
                }
            }, 75);
        };
        /**
         * Sets the currently selected cell.
         */
        VisualQueryPainterService.prototype.setSelected = function (cellDiv) {
            // Remove previous selection
            if (this.selectedCell) {
                angular.element(this.selectedCell).removeClass(VisualQueryPainterService.SELECTED_CLASS);
            }
            // Set new selection
            this.selectedCell = cellDiv;
            angular.element(this.selectedCell).addClass(VisualQueryPainterService.SELECTED_CLASS);
        };
        /**
         * Shows the cell menu on the specified cell.
         */
        VisualQueryPainterService.prototype.showMenu = function (cellDiv, event) {
            var _this = this;
            // Get column info
            var cell = angular.element(cellDiv);
            var column = cell.data("column");
            var header = this.delegate.columns[column];
            var isNull = cell.hasClass("null");
            var selection = this.$window.getSelection();
            if (this.selectedCell !== event.target || (selection.anchorNode !== null && selection.anchorNode !== selection.focusNode)) {
                return; // ignore dragging between elements
            }
            if (angular.element(document.body).children(".CodeMirror-hints").length > 0) {
                return; // ignore clicks when CodeMirror function list is active
            }
            else if (header.delegate.dataCategory === column_delegate_1.DataCategory.DATETIME || header.delegate.dataCategory === column_delegate_1.DataCategory.NUMERIC || header.delegate.dataCategory === column_delegate_1.DataCategory.STRING) {
                this.menuVisible = true;
            }
            else {
                return; // ignore clicks on columns with unsupported data types
            }
            // Update content
            var $scope = this.menuPanel.config.scope;
            $scope.DataCategory = column_delegate_1.DataCategory;
            $scope.header = header;
            $scope.selection = (header.delegate.dataCategory === column_delegate_1.DataCategory.STRING) ? selection.toString() : null;
            $scope.table = this.delegate;
            $scope.value = isNull ? null : cellDiv.innerText;
            // Update position
            this.menuPanel.updatePosition(this.$mdPanel.newPanelPosition()
                .left(event.clientX + PIXELS)
                .top(event.clientY + PIXELS));
            // Show menu
            this.menuPanel.open()
                .then(function () {
                // Add click listener
                _this.menuPanel.panelEl.on("click", "button", function () { return _this.hideMenu(); });
                // Calculate position
                var element = angular.element(_this.menuPanel.panelEl);
                var height = element.height();
                var offset = element.offset();
                var width = element.width();
                // Fix position if off screen
                var left = (offset.left + width > _this.$window.innerWidth) ? _this.$window.innerWidth - width - 8 : event.clientX;
                var top = (offset.top + height > _this.$window.innerHeight) ? _this.$window.innerHeight - height - 8 : event.clientY;
                if (left !== event.clientX || top !== event.clientY) {
                    _this.menuPanel.updatePosition(_this.$mdPanel.newPanelPosition()
                        .left(left + PIXELS)
                        .top(top + PIXELS));
                }
            });
        };
        /**
         * Shows the tooltip on the specified cell.
         */
        VisualQueryPainterService.prototype.showTooltip = function (cellDiv) {
            this.tooltipVisible = true;
            // Update content
            var $scope = this.tooltipPanel.panelEl.scope();
            $scope.validation = angular.element(cellDiv).data("validation");
            $scope.value = cellDiv.innerText;
            // Update position
            var cellOffset = angular.element(cellDiv).offset();
            var offsetY;
            var yPosition;
            if (cellOffset.top + VisualQueryPainterService.ROW_HEIGHT * 3 > this.$window.innerHeight) {
                offsetY = "-27" + PIXELS;
                yPosition = this.$mdPanel.yPosition.ABOVE;
            }
            else {
                offsetY = "0";
                yPosition = this.$mdPanel.yPosition.BELOW;
            }
            this.tooltipPanel.updatePosition(this.$mdPanel.newPanelPosition()
                .relativeTo(cellDiv)
                .addPanelPosition(this.$mdPanel.xPosition.ALIGN_START, yPosition)
                .withOffsetX("28px")
                .withOffsetY(offsetY));
            // Show tooltip
            this.tooltipPanel.open();
        };
        /**
         * Turns off sorting.
         */
        VisualQueryPainterService.prototype.unsort = function () {
            if (this.delegate) {
                this.delegate.unsort();
            }
        };
        /**
         * Left and right padding for normal columns.
         */
        VisualQueryPainterService.COLUMN_PADDING = 28;
        /**
         * Left padding for the first column.
         */
        VisualQueryPainterService.COLUMN_PADDING_FIRST = 24;
        /**
         * Height of header row.
         */
        VisualQueryPainterService.HEADER_HEIGHT = 56;
        /**
         * Height of data rows.
         */
        VisualQueryPainterService.ROW_HEIGHT = 48;
        /**
         * Class for selected cells.
         */
        VisualQueryPainterService.SELECTED_CLASS = "selected";
        VisualQueryPainterService.$inject = ["$compile", "$mdPanel", "$rootScope", "$templateCache", "$templateRequest", "$timeout", "$window"];
        return VisualQueryPainterService;
    }(fattable.Painter));
    exports.VisualQueryPainterService = VisualQueryPainterService;
    angular.module(require("feed-mgr/visual-query/module-name")).service("VisualQueryPainterService", VisualQueryPainterService);
});
//# sourceMappingURL=visual-query-painter.service.js.map