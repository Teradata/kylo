/*-
 * #%L
 * thinkbig-ui-feed-manager
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
(function () {

    var controller = function ($scope, $stateParams, $http, $mdToast, $mdDialog, CategoriesService, StateService) {

        var self = this;
        this.templatesMigrated = 0;
        this.feedsMigrated = 0;
        this.categoriesMigrated = 0;
        this.status = "";
        this.migratingData = false;
        this.showMigrationResults = false;

        this.migrate = function (ev) {
            if (!self.migratingData) {
                self.showConfirm(ev);
            }

        }

        this.showConfirm = function (ev) {
            // Appending dialog to document.body to cover sidenav in docs app
            var confirm = $mdDialog.confirm()
                .title('Are you sure you want to migrate to modeshape?')
                .textContent('Any data you have in Modeshape that matches what you had in the MySQL database will be replaced.')
                .ariaLabel('Migrate')
                .targetEvent(ev)
                .ok('Please do it!')
                .cancel('Nope');
            $mdDialog.show(confirm).then(function () {
                self.migratingData = true;
                self.showMigrationResults = true;
                self.status = 'Migrating data from Mysql to Modeshape';
                $http.post("/proxy/v1/jpa2modeshape/migrate").then(function (response) {

                    self.status = "Finished migrating data.";
                    self.templatesMigrated = response.data.templatesMigrated;
                    self.categoriesMigrated = response.data.categoriesMigrated;
                    self.feedsMigrated = response.data.feedsMigrated;
                    self.migratingData = false;

                });

            }, function () {
                self.status = '';
            });
        };

    };

    angular.module(MODULE_FEED_MGR).controller('ModeshapeMigrationController', controller);

}());
