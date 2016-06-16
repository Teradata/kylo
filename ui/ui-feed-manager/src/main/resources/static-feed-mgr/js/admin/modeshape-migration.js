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
                    console.log('response', response)
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
