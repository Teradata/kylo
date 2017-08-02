/*-
 * #%L
 * kylo-ui-app
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
define(["angularMocks", "feed-mgr/module-name", "feed-mgr/module", "feed-mgr/module-require"], function (mocks, moduleName) {
    describe("DomainTypesServiceTest", function () {
        // Include dependencies
        beforeEach(mocks.module("kylo", moduleName));

        // detectDomainType
        it("should detect domain type", mocks.inject(function (DomainTypesService) {
            var domainTypes = [{id: "0", regexPattern: "f|m"}];

            expect(DomainTypesService.detectDomainType("female", domainTypes)).toBe(null);
            expect(DomainTypesService.detectDomainType("f", domainTypes)).toBe(domainTypes[0]);
        }));
    });
});
