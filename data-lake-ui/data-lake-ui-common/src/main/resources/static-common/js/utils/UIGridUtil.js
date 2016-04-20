var UIGridUtil = (function () {
    function UIGridUtil() {
    }

    function UIGridUtilTag() {
    }

    this.__tag = new UIGridUtilTag();

    UIGridUtil.newStatus = function () {
        return {
            loadingGrid: false, lastRefreshed: '',
            refreshComplete: function () {
                this.lastRefreshed = new Date();
                this.loadingGrid = false;
                this.isRefreshingGrid = false;
            },
            loadGrid: function () {
                this.refresh();
            },
            refresh: function () {
                this.loadingGrid = true;
                this.isRefreshingGrid = true;
            }
        };
    }

    UIGridUtil.isRefreshing = function (gridStatus) {
        if (gridStatus.isRefreshingGrid != undefined) {
            return gridStatus.isRefreshingGrid;
        }
        return false;
    }
    UIGridUtil.gridRefreshStart = function (gridStatus) {
        if (!UIGridUtil.isRefreshing(gridStatus)) {
            gridStatus.isRefreshingGrid = true;
            return true;
        }
        return false;

    };
    UIGridUtil.gridRefreshStopped = function (gridStatus) {
        gridStatus.isRefreshingGrid = false;
        gridStatus.loadingGrid = false;
    }

    UIGridUtil.gridRefreshComplete = function (gridStatus) {
        gridStatus.isRefreshingGrid = false;
        gridStatus.lastRefreshed = new Date();
        gridStatus.loadingGrid = false;
    }
    return UIGridUtil;
})();

