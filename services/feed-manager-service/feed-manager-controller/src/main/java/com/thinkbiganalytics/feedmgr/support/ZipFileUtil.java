package com.thinkbiganalytics.feedmgr.support;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility to interact with zip files
 */
public class ZipFileUtil {


    /**
     * Validate filenames in a zip file This does case insensitive comparison
     */
    public static boolean validateZipEntries(byte[] zipFile, Set<String> validNames, Set<String> requiredNames, boolean matchAllValidNames) throws IOException {
        if (validNames == null) {
            validNames = new HashSet<>();
        }
        List<String> validNamesList = validNames.stream().map(String::toLowerCase).collect(Collectors.toList());
        Set<String> fileNames = getFileNames(zipFile).stream().map(String::toLowerCase).collect(Collectors.toSet());

        boolean isValid = fileNames != null && !fileNames.isEmpty() && validNamesList.stream().allMatch(fileNames::contains);
        if (isValid && matchAllValidNames) {
            isValid = fileNames.size() == validNames.size();
        }

        if (isValid && requiredNames != null && !requiredNames.isEmpty()) {
            isValid = requiredNames.stream().allMatch(fileNames::contains);
        }
        return isValid;
    }

    public static String zipEntryToString(byte[] buffer, ZipInputStream zis, ZipEntry entry) throws IOException {
        // consume all the data from this entry
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = 0;
        while ((len = zis.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        out.close();
        return new String(out.toByteArray(), "UTF-8");
    }


    /**
     *
     * @param zipFile
     * @param validNames
     * @param requiredNames
     * @return
     * @throws IOException
     */
    public static boolean validateZipEntriesWithRequiredEntries(byte[] zipFile, Set<String> validNames, Set<String> requiredNames) throws IOException {
        return validateZipEntries(zipFile, validNames, requiredNames, false);
    }

    /**
     *
     * @param zipFile
     * @param validNames
     * @param matchAllValidNames
     * @return
     * @throws IOException
     */
    public static boolean validateZipEntries(byte[] zipFile, Set<String> validNames, boolean matchAllValidNames) throws IOException {
        return validateZipEntries(zipFile, validNames, null, matchAllValidNames);
    }

    /**
     * Gets the file names in a zip file
     */
    public static Set<String> getFileNames(byte[] zipFile) throws IOException {
        Set<String> fileNames = new HashSet<>();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(zipFile);
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            fileNames.add(entry.getName());
        }
        return fileNames;
    }


    /**
     * Adds an entry to a zip file
     *
     * @param zip      the zip file which will have the content added
     * @param file     the string to add to the zip
     * @param fileName the zip file name
     * @return the zip file with the newly added content
     */
    public static byte[] addToZip(byte[] zip, String file, String fileName) throws IOException {
        InputStream zipInputStream = new ByteArrayInputStream(zip);
        ZipInputStream zis = new ZipInputStream(zipInputStream);
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int len = 0;
                while ((len = zis.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.close();

                zos.putNextEntry(entry);
                zos.write(out.toByteArray());
                zos.closeEntry();

            }
            zis.closeEntry();
            zis.close();

            entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);
            zos.write(file.getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }


}

