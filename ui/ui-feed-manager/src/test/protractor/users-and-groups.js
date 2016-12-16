var GroupDetailsPage = require("./pages/GroupDetailsPage.js");
var UserDetailsPage = require("./pages/UserDetailsPage.js");
var UsersPage = require("./pages/UsersPage.js");

describe("Users and Groups", function() {
    it("should ensure default group tags are visible (UG-1)", function() {
        var page = new UserDetailsPage();
        page.get();
        page.setGroupText("n");

        expect(page.getGroupAutocomplete()).toEqual(["analyst", "admin", "designer", "operations"]);
    });

    it("should ensure no duplicate usernames are allowed or show warning (UG-2)", function() {
        var page = new UserDetailsPage();
        page.get();
        page.setUsername("dladmin");

        expect(page.getUsernameMessages()).toEqual(["That username is already in use."]);
    });

    it("should ensure rows per-page on user page is visible (UG-3)", function() {
        var page = new UsersPage();
        page.get();

        expect(page.getRowsPerPageOptions()).toEqual(["5", "10", "20", "50", "All"]);
    });

    it("should ensure no duplicate group name results in warning or error before saving (UG-4)", function() {
        var page = new GroupDetailsPage();
        page.get();
        page.setGroupName("user");

        expect(page.getGroupNameMessages()).toEqual(["That group name is already in use."]);
    });

    it("should ensure default group names are visible on the users list (UG-5)", function() {
        var detailsPage = new UserDetailsPage();
        detailsPage.get();
        detailsPage.setUsername("mickey.mouse");
        detailsPage.addGroup("analyst");
        detailsPage.addGroup("user");
    });
});
