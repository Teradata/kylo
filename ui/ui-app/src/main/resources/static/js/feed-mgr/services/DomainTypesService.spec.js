define(["angular-mocks", "feed-mgr/module-name", "feed-mgr/module", "feed-mgr/module-require"], function (mocks, moduleName) {
    describe("Service: DomainTypesService", function () {
        // Include dependencies
        beforeEach(mocks.module("kylo", moduleName));

        // detectDomainType
        describe("detectDomainType", function () {
            it("should detect domain type by name", mocks.inject(function (DomainTypesService) {
                var domainTypes = [{id: "0", fieldNameFlags: "i", fieldNamePattern: "zip_?code"}];
                expect(DomainTypesService.detectDomainType({name: "zip code"}, domainTypes)).toBe(null);
                expect(DomainTypesService.detectDomainType({name: "zipCode"}, domainTypes)).toBe(domainTypes[0]);
            }));
            it("should detect domain type by sample value", mocks.inject(function (DomainTypesService) {
                var domainTypes = [{id: "0", regexPattern: "f|m"}];

                expect(DomainTypesService.detectDomainType({name: "gender", sampleValues: "female"}, domainTypes)).toBe(null);
                expect(DomainTypesService.detectDomainType({name: "gender", sampleValues: ["m", "f"]}, domainTypes)).toBe(domainTypes[0]);
                expect(DomainTypesService.detectDomainType({name: "gender", sampleValues: ["x", "f"]}, domainTypes)).toBe(null);
            }));
            it("should detect domain type by name and sample value", mocks.inject(function (DomainTypesService) {
                var domainTypes = [{id: "0", fieldNamePattern: "gender", regexPattern: "f|m"}];
                expect(DomainTypesService.detectDomainType({name: "type", sampleValues: "f"}, domainTypes)).toBe(null);
                expect(DomainTypesService.detectDomainType({name: "gender", sampleValues: "x"}, domainTypes)).toBe(null);
                expect(DomainTypesService.detectDomainType({name: "gender", sampleValues: "f"}, domainTypes)).toBe(domainTypes[0]);
            }));
        });
    });
});
