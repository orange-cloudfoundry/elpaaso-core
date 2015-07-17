/**
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;

/**
 * Utility class to centralize the fetching of app bits from MavenRef, InputStream or File, and
 * provides it into a temp file for further processing
 */
public class FileFetcherUtil {

    private static Logger logger = LoggerFactory.getLogger(FileFetcherUtil.class.getName());

    /**
     * Interface implemented by callers of {@link com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure.FileFetcherUtil}
     */
    public static interface FileProcessor {

        void process(String filename, String filetype, File file);

    }

    public FileFetcherUtil() {
    }

    /**
     * Typically invoked by ManageJEEDeploymentImpl.deployOnPaas() using an inputstream read from the SOAP request.
     *
     * @param filename the original name of the file (e.g. "myapp.ear")
     * @param filetype optional file type (e.g. usually extension "jar" "ear" or "war") or null to try to detect it from filename
     * @throws TechnicalException
     */
    public void fetchInputStreamAndApplyProcessing(String filename, String filetype, InputStream filestream, FileProcessor fileProcessor) throws TechnicalException {

        // define filetype regarding file extension
        if (filetype == null) {
            filetype = getDeployableType(filename);
        }

        File tempFile = null;
        try {
            // The CF SDK is requiring local files only, so we need to create a temp local file in a temp directory
            tempFile = createTempFileFromInputStream(filename, filestream, filetype);
            fileProcessor.process(filename, filetype, tempFile);
        } catch (IOException e) {
            throw new TechnicalException("IOException on cargo deployment : " + e.getMessage(), e);
        } catch (Exception e) {
            throw new TechnicalException("Error on stream processing : " + e.getMessage(), e);
        } finally {
            FileUtils.deleteQuietly(tempFile.getParentFile());
        }
    }

    /**
     * Typically invoked by CfPlugin for initial app push.
     * @param ref
     * @param fileProcessor
     */
    void fetchMavenReferenceAndApplyProcessing(MavenReference ref, FileProcessor fileProcessor){
        InputStream fileStream = null;
        try {
            final String filename = (new File(ref.getAccessUrl().getFile())).getName();
            String fileType = ref.getExtension();
            // file extension should match reference type (mainly ear or rar)
            if (!filename.endsWith("." + fileType)) {
                throw new TechnicalException("File (" + filename + ") is not a (" + fileType + ") file type");
            }

            fileStream = ref.getAccessUrl().openStream();
            fetchInputStreamAndApplyProcessing(filename, fileType, fileStream, fileProcessor);
        } catch (IOException e) {
            throw new TechnicalException("URL " + ref.getAccessUrl() + " not available", e);
        } finally {
            IOUtils.closeQuietly(fileStream);
        }
    }

    /**
     * Useful for generated artifact
     */
    public void readFileAndApplyProcessing(File file, FileProcessor fileProcessor) {
            // Extract filename/extension/stream content from current MavenReference
            String filename = file.getName();

            fileProcessor.process(filename, null, file);
    }

    //
    // Implementation
    //


    protected InputStream createInputStreamFromFile(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    private File createTempFileFromInputStream(String filename, InputStream filestream, String filetype) throws IOException {
        logger.debug("creating local temporary file for {} deployment", filetype);
        File tempDir = this.createTempDirectory();
        File tempFile = new File(tempDir, filename);
        FileUtils.copyInputStreamToFile(new BufferedInputStream(filestream), tempFile);
        logger.debug("temporary file name created : {}", tempFile.getAbsolutePath());
        return tempFile;
    }

    /**
     * create a temp file, delete it and create the corresponding directory
     *
     * @return the temporary directory
     *
     * @throws IOException
     */
    // @TODO : move to utility package
    public File createTempDirectory() throws IOException {
        final File temp;
        temp = File.createTempFile("deploy", "");

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }

    public String getDeployableType(String filename) {
        String filetype;
        try {
            filetype = filename.substring(filename.lastIndexOf(".") + 1);
        } catch (RuntimeException e) {
            throw new TechnicalException("IAAS cargo deployment : incorrect file type, error=" + e.getMessage());
        }
        if ("xml".equals(filetype)) {
            filetype = "file";
        } else if ("jar".equals(filetype)) {
            filetype = "bundle";
        } else if (!"ear".equals(filetype) && !"rar".equals(filetype)) {
            throw new TechnicalException("IAAS cargo deployment : incorrect file type (" + filetype + ")");
        }
        return filetype;
    }


}