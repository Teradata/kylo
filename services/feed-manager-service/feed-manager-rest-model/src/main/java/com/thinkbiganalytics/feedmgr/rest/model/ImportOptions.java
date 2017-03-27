package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import com.thinkbiganalytics.feedmgr.rest.ImportComponent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportOptions {

    private String uploadKey;

    private Set<ImportComponentOption> importComponentOptions;

    public Set<ImportComponentOption> getImportComponentOptions() {
        if (importComponentOptions == null) {
            importComponentOptions = new HashSet<>();
        }
        return importComponentOptions;
    }

    public void setImportComponentOptions(Set<ImportComponentOption> importComponentOptions) {
        this.importComponentOptions = importComponentOptions;
    }


    public ImportComponentOption findImportComponentOption(ImportComponent component) {
        ImportComponentOption option = findOption(component);
        if (option == null) {
            option = new ImportComponentOption(component, false);
            importComponentOptions.add(option);
        }
        return option;
    }

    private ImportComponentOption findOption(ImportComponent component) {
        return getImportComponentOptions().stream().filter(importComponentOption -> importComponentOption.getImportComponent().equals(component)).findFirst().orElse(null);
    }

    public boolean isImport(ImportComponent component) {
        ImportComponentOption option = findImportComponentOption(component);
        return (option != null && option.isShouldImport());
    }

    public boolean isAnalyzed(ImportComponent component) {
        ImportComponentOption option = findImportComponentOption(component);
        return (option != null && option.isAnalyzed());
    }

    public boolean isUserAcknowledged(ImportComponent component) {
        ImportComponentOption option = findImportComponentOption(component);
        return (option != null && option.isUserAcknowledged());
    }


    public boolean isImportAndOverwrite(ImportComponent component) {
        ImportComponentOption option = findImportComponentOption(component);
        return (option != null && option.isShouldImport() && option.isOverwrite());
    }

    public boolean isContinueIfExists(ImportComponent component) {
        ImportComponentOption option = findImportComponentOption(component);
        return (option != null && option.isShouldImport() && option.isContinueIfExists());
    }

    public boolean stopProcessingAlreadyExists(ImportComponent component) {
        ImportComponentOption option = findImportComponentOption(component);
        return (option != null && option.isShouldImport() && !option.isContinueIfExists() && !option.isOverwrite());
    }

    private void removeOption(ImportComponent component) {
        ImportComponentOption option = findOption(component);
        if (option != null) {
            importComponentOptions.remove(option);
        }
    }

    /**
     * set the value of the options to the incoming set, matching on the component
     */
    public void updateOptions(Set<ImportComponentOption> options) {
        Set<ImportComponentOption> optionsToRemove = new HashSet<>();
        if (!options.equals(this.importComponentOptions)) {
            for (ImportComponentOption option : options) {
                removeOption(option.getImportComponent());
            }
            //add them in
            getImportComponentOptions().addAll(options);
        }
    }

    /**
     * @param importComponent the component to look for
     * @return a List of properties for the component.
     */
    public List<ImportProperty> getProperties(ImportComponent importComponent) {
        return findImportComponentOption(importComponent).getProperties();
    }

    /**
     * match the incoming options against the current set based upon component.  if the current set matches and it is not analyzed, reset it to the value of the incoming option.
     */
    public void updateUnAnalyzedOptions(Set<ImportComponentOption> options) {
        Set<ImportComponentOption> optionsToRemove = new HashSet<>();
        for (ImportComponentOption option : options) {
            ImportComponentOption matchingOption = findOption(option.getImportComponent());
            if (matchingOption != null && !matchingOption.isAnalyzed()) {
                removeOption(option.getImportComponent());
                getImportComponentOptions().add(option);
            }
        }
    }

    /**
     * If the current set of options doesnt contain an option with the incoming component, then add it
     *
     * @param options options to add
     */
    public void addOptionsIfNotExists(Set<ImportComponentOption> options) {
        for (ImportComponentOption option : options) {
            ImportComponentOption matchingOption = findOption(option.getImportComponent());
            if (matchingOption == null) {
                getImportComponentOptions().add(option);
            }
        }
    }


    public void addErrorMessage(ImportComponent component, String msg) {
        findImportComponentOption(component).getErrorMessages().add(msg);
    }

    public boolean hasErrorMessages(ImportComponent component) {
        return findImportComponentOption(component).getErrorMessages().size() > 0;
    }


    public String getUploadKey() {
        return uploadKey;
    }

    public void setUploadKey(String uploadKey) {
        this.uploadKey = uploadKey;
    }

}
