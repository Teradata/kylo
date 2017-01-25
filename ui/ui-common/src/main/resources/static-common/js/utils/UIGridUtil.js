/*-
 * #%L
 * thinkbig-ui-common
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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

