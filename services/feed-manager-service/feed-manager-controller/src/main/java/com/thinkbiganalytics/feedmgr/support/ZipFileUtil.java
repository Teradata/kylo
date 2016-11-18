package com.thinkbiganalytics.feedmgr.support;

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
 * Created by sr186054 on 11/17/16.
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
        ;
        boolean isValid = fileNames != null && !fileNames.isEmpty() && fileNames.stream().allMatch(name -> validNamesList.contains(name));
        if (isValid && matchAllValidNames) {
            isValid &= fileNames.size() == validNames.size();
        }

        if (isValid && requiredNames != null && !requiredNames.isEmpty()) {
            isValid &= requiredNames.stream().allMatch(name -> fileNames.contains(name));
        }
        return isValid;
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

