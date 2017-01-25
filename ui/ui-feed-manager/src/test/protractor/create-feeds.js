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
// TODO: refactor to use Page Objects (http://www.protractortest.org/#/page-objects)

describe("Create Feeds", function() {
    it("should import Index Schema Service feed", function() {
        browser.get("http://dladmin:thinkbig@localhost:8400/feed-mgr/index.html");

        browser.findElement(by.css("add-button button")).click();
        browser.findElement(by.css("span[ng-if='vm.allowImport'] a")).click();
        browser.findElement(by.css("upload-file input[type=file]")).sendKeys(require("path").resolve(__dirname, "../../../../../samples/feeds/nifi-1.0/index_schema_service.zip"));
        browser.findElement(by.css("button.md-primary")).click();
        browser.waitForAngular();

        expect(element(by.binding("vm.message")).getText()).toEqual("Successfully imported the feed system.index_schema_service.");
    });

    it("should import Index Text Service feed", function() {
        browser.get("http://dladmin:thinkbig@localhost:8400/feed-mgr/index.html");

        browser.findElement(by.css("add-button button")).click();
        browser.findElement(by.css("span[ng-if='vm.allowImport'] a")).click();
        browser.findElement(by.css("upload-file input[type=file]")).sendKeys(require("path").resolve(__dirname, "../../../../../samples/feeds/nifi-1.0/index_text_service.zip"));
        browser.findElement(by.css("button.md-primary")).click();
        browser.waitForAngular();

        expect(element(by.binding("vm.message")).getText()).toEqual("Successfully imported the feed system.index_text_service.");
    });

    it("should create Data Ingest feed", function() {
        browser.get("http://dladmin:thinkbig@localhost:8400/feed-mgr/index.html");

        browser.findElement(by.xpath("//add-button/button")).click();

        browser.findElement(by.xpath("//md-card[1]")).click();

        browser.findElement(by.name("feedName")).sendKeys("User Data");
        browser.findElement(by.name("category")).click();
        browser.waitForAngular();
        element.all(by.css(".autocomplete-categories-template li")).get(1).click();
        browser.findElement(by.tagName("textarea")).sendKeys("Ingest sample data");
        element(by.css("thinkbig-define-feed-general-info thinkbig-step-buttons button.md-primary")).click();

        browser.waitForAngular();
        element.all(by.css("md-radio-button")).get(1).click();
        element.all(by.xpath("//input[@ng-model='property.value']")).get(0).clear().sendKeys("userdata\\d{1,3}.csv");
        element(by.css("thinkbig-define-feed-details thinkbig-step-buttons button.md-primary")).click();

        browser.findElement(by.css("upload-file #fileInput")).sendKeys(require("path").resolve(__dirname, "../../../../../samples/sample-data/userdata1.csv"));
        browser.findElement(by.id("upload-sample-file-btn")).click();
        browser.waitForAngular();
        // Set registration_dttm properties
        element.all(by.xpath("//md-select[@ng-model='columnDef.dataType']")).get(0).click();
        browser.findElement(by.css("div.md-select-menu-container.md-active md-option[value=timestamp]")).click();
        element.all(by.xpath("//md-checkbox[@ng-model='columnDef.createdTracker']")).get(0).click();
        // Set id properties
        element.all(by.xpath("//md-checkbox[@ng-model='columnDef.primaryKey']")).get(1).click();
        // Set cc properties
        element.all(by.css("md-select[ng-model='columnDef.dataType']")).get(7).click();
        browser.findElement(by.css("div.md-select-menu-container.md-active md-option[value=string]")).click();
        // Set birthdate properties
        element.all(by.css("md-select[ng-model='columnDef.dataType']")).get(9).click();
        browser.findElement(by.css("div.md-select-menu-container.md-active md-option[value=date]")).click();
        // Set comments properties
        element.all(by.css("md-select[ng-model='columnDef.dataType']")).get(12).click();
        browser.findElement(by.css("div.md-select-menu-container.md-active md-option[value=string]")).click();
        element(by.css("thinkbig-define-feed-table thinkbig-step-buttons button.md-primary")).click();

        browser.waitForAngular();
        // Add Date/Time standardize rule to registration_dttm
        browser.executeScript("arguments[0].scrollIntoView()", element.all(by.css("thinkbig-define-feed-data-processing")).get(0).getWebElement());
        element.all(by.css("ng-md-icon[icon=add_circle_outline]")).get(0).click();
        browser.findElement(by.css("ng-form[name=policyForm] md-select")).click();
        browser.waitForAngular();
        element.all(by.css("div.md-select-menu-container.md-active md-option")).get(1).click();
        browser.waitForAngular();
        element.all(by.css("thinkbig-policy-input-form input")).get(0).sendKeys("YYYY-MM-dd'T'HH:mm:ss'Z'");
        element.all(by.css("thinkbig-policy-input-form md-select")).get(0).click();
        browser.findElement(by.css("div.md-select-menu-container.md-active md-option[value=DATETIME]")).click();
        browser.findElement(by.css("button[aria-label='ADD RULE']")).click();
        browser.findElement(by.css("button[aria-label=Done]")).click();
        // Index first_name
        element.all(by.css("md-checkbox[ng-model='policy.index']")).get(2).click();
        // Index last_name
        element.all(by.css("md-checkbox[ng-model='policy.index']")).get(3).click();
        // Index email
        element.all(by.css("md-checkbox[ng-model='policy.index']")).get(4).click();
        // Add Email validate rule to email
        element.all(by.css("ng-md-icon[icon=add_circle_outline]")).get(9).click();
        browser.findElement(by.css("ng-form[name=policyForm] md-select")).click();
        browser.waitForAngular();
        element.all(by.css("div.md-select-menu-container.md-active md-option")).get(2).click();
        browser.findElement(by.css("button[aria-label='ADD RULE']")).click();
        browser.findElement(by.css("button[aria-label=Done]")).click();
        // Add IP Address validate rule to ip_address
        element.all(by.css("ng-md-icon[icon=add_circle_outline]")).get(13).click();
        browser.findElement(by.css("ng-form[name=policyForm] md-select")).click();
        browser.waitForAngular();
        element.all(by.css("div.md-select-menu-container.md-active md-option")).get(3).click();
        browser.findElement(by.css("button[aria-label='ADD RULE']")).click();
        browser.findElement(by.css("button[aria-label=Done]")).click();
        // Add Date/Time standardize rule to birthday
        browser.waitForAngular();
        element.all(by.css("ng-md-icon[icon=add_circle_outline]")).get(18).click();
        browser.findElement(by.css("ng-form[name=policyForm] md-select")).click();
        browser.waitForAngular();
        element.all(by.css("div.md-select-menu-container.md-active md-option")).get(1).click();
        browser.waitForAngular();
        element.all(by.css("thinkbig-policy-input-form input")).get(0).sendKeys("M/d/YYYY");
        element.all(by.css("thinkbig-policy-input-form md-select")).get(0).click();
        browser.findElement(by.css("div.md-select-menu-container.md-active md-option[value=DATE_ONLY]")).click();
        browser.findElement(by.css("button[aria-label='ADD RULE']")).click();
        browser.findElement(by.css("button[aria-label=Done]")).click();
        element(by.css("thinkbig-define-feed-data-processing thinkbig-step-buttons button.md-primary")).click();

        browser.findElement(by.model("vm.model.dataOwner")).sendKeys("Think Big Kylo Team");
        browser.findElement(by.css("md-chips[ng-model='vm.model.tags'] input")).sendKeys("kylo\nusers\nsample\n");
        element(by.css("thinkbig-define-feed-properties thinkbig-step-buttons button.md-primary")).click();

        browser.findElement(by.css("md-select[ng-model='vm.timerUnits']")).click();
        browser.findElement(by.css("div.md-select-menu-container md-option[value=sec]")).click();
        element(by.css("thinkbig-define-feed-schedule thinkbig-step-buttons button.md-primary")).click();
        browser.waitForAngular();

        expect(element(by.css("div.md-headline")).getText()).toEqual("Congratulations! Your feed has been successfully created.");
    }, 60000);
});
