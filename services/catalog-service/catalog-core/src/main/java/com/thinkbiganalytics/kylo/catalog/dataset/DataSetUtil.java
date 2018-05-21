package com.thinkbiganalytics.kylo.catalog.dataset;

import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nonnull;

public class DataSetUtil {

    @Nonnull
    public static DataSetTemplate mergeTemplates(@Nonnull final DataSet dataSet) {
        DataSetTemplate template = null;
        if (dataSet.getDataSource() != null && dataSet.getDataSource().getConnector() != null && dataSet.getDataSource().getConnector().getTemplate() != null) {
            template = new DataSetTemplate(dataSet.getDataSource().getConnector().getTemplate());
        }

        if (dataSet.getDataSource() != null && dataSet.getDataSource().getTemplate() != null) {
            if (template == null) {
                template = new DataSetTemplate(dataSet.getDataSource().getTemplate());
            } else {
                mergeTemplates(template, dataSet.getDataSource().getTemplate());
            }
        }

        if (template == null) {
            template = new DataSetTemplate(dataSet);
        } else {
            mergeTemplates(template, dataSet);
        }
        return template;
    }

    public static void mergeTemplates(@Nonnull final DataSetTemplate dst, @Nonnull final DataSetTemplate src) {
        if (src.getFiles() != null) {
            if (dst.getFiles() != null) {
                dst.getFiles().addAll(src.getFiles());
            } else {
                dst.setFiles(new ArrayList<>(src.getFiles()));
            }
        }
        if (src.getJars() != null) {
            if (dst.getJars() != null) {
                dst.getJars().addAll(src.getJars());
            } else {
                dst.setJars(new ArrayList<>(src.getJars()));
            }
        }
        if (src.getFormat() != null) {
            dst.setFormat(src.getFormat());
        }
        if (src.getOptions() != null) {
            if (dst.getOptions() != null) {
                dst.getOptions().putAll(src.getOptions());
            } else {
                dst.setOptions(new HashMap<>(src.getOptions()));
            }
        }
        if (src.getPaths() != null) {
            dst.setPaths(src.getPaths());
        }
    }
}
