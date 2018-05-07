package com.thinkbiganalytics.integration.catalog;

import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.kylo.catalog.rest.controller.DataSetController;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;

import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DataSetIT extends IntegrationTestBase {

    /**
     * Verify uploading files.
     */
    @Test
    public void testUpload() {
        final String id = UUID.randomUUID().toString();

        // Test empty data set
        List<DataSetFile> files = listUploads(id);
        Assert.assertEquals(0, files.size());

        // Upload sample files
        uploadFile(id, getSampleFile("userdata1.csv"));
        uploadFile(id, getSampleFile("userdata2.csv"));

        files = listUploads(id);
        Assert.assertThat(files, CoreMatchers.hasItem(nameEquals("userdata1.csv")));
        Assert.assertThat(files, CoreMatchers.hasItem(nameEquals("userdata2.csv")));
        Assert.assertEquals(2, files.size());

        // Delete a file
        given(DataSetController.BASE)
            .when().delete(id + "/uploads/userdata1.csv")
            .then().statusCode(204);

        files = listUploads(id);
        Assert.assertThat(files, CoreMatchers.hasItem(nameEquals("userdata2.csv")));
        Assert.assertEquals(1, files.size());
    }

    @Override
    protected void cleanup() {
        // nothing to do
    }

    /**
     * Asserts that a {@link DataSetFile}'s name equals the given name.
     */
    @Nonnull
    @SuppressWarnings("SameParameterValue")
    private static Matcher<DataSetFile> nameEquals(@Nonnull final String name) {
        return new BaseMatcher<DataSetFile>() {
            @Override
            public boolean matches(@Nullable final Object item) {
                if (item instanceof DataSetFile) {
                    return Objects.equals(name, ((DataSetFile) item).getName());
                }
                return false;
            }

            @Override
            public void describeTo(@Nonnull final Description description) {
                description.appendText("equal to DataSetFile[name=" + name + "]");
            }
        };
    }

    /**
     * Gets the sample file with the specified name.
     */
    @Nonnull
    private File getSampleFile(@Nonnull final String name) {
        final String path = getClass().getResource(".").getPath();
        final String basedir = path.substring(0, path.indexOf("services"));
        return Paths.get(basedir, "samples", "sample-data", "csv", name).toFile();
    }

    /**
     * Lists the uploaded files for the specified data set.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    private List<DataSetFile> listUploads(@Nonnull final String dataSetId) {
        final DataSetFile[] files = given(DataSetController.BASE)
            .when().get(dataSetId + "/uploads")
            .then().statusCode(200)
            .extract().as(DataSetFile[].class);
        return Arrays.asList(files);
    }

    /**
     * Uploads the specified file to the specified data set.
     */
    private void uploadFile(@Nonnull final String dataSetId, @Nonnull final File file) {
        given(DataSetController.BASE)
            .contentType("multipart/form-data")
            .multiPart(file)
            .when().post(dataSetId + "/uploads")
            .then().statusCode(200);
    }
}
