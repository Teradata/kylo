angular.module(MODULE_FEED_MGR).factory("WindowUnloadService", function($rootScope, $window) {
    /**
     * Listens and responds to window unload events.
     *
     * @readonly
     * @type {Object}
     */
    var WindowUnloadService = {
        /**
         * Confirmation text to be displayed on an unload event.
         *
         * @private
         * @type {string|null}
         */
        text_: null,

        /**
         * Disables the confirmation dialog.
         */
        clear: function() {
            this.text_ = null;
        },

        /**
         * Called to setup the confirmation dialog on an unload event.
         *
         * @param {Event} e the unload event
         * @returns {string|null} the dialog text to be displayed, or {@code null} to allow the event
         */
        onBeforeUnload: function(e) {
            if (this.text_ !== null) {
                e.returnValue = this.text_;
            }
            return this.text_;
        },

        /**
         * Enables the confirmation dialog and sets the dialog text.
         *
         * @param {string} text the dialog text
         */
        setText: function(text) {
            this.text_ = text;
        },

        /**
         * Called when changing states.
         *
         * @param {Event} e the state change event
         */
        shouldChangeState: function(e) {
            if (this.text_ === null || confirm(this.text_)) {
                this.clear();
            } else {
                e.preventDefault();
            }
        }
    };

    // Setup event listeners
    $rootScope.$on("$stateChangeStart", angular.bind(WindowUnloadService, WindowUnloadService.shouldChangeState));
    $window.onbeforeunload = angular.bind(WindowUnloadService, WindowUnloadService.onBeforeUnload);

    // Return service
    return WindowUnloadService;
});
