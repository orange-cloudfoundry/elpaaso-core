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

import java.io.File;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;

/**
 *
 */
public class SampleProcessorCaller {

	private static Logger logger = LoggerFactory.getLogger(SampleProcessorCaller.class.getName());
    protected FileFetcherUtil fileFetcherUtil = new FileFetcherUtil();


    public void deployJEEArtifact(MavenReference ref, FileFetcherUtil.FileProcessor processor) throws TechnicalException {

        fileFetcherUtil.fetchMavenReferenceAndApplyProcessing(ref, processor);
    }

	public void deployJEEArtifact(File file, FileFetcherUtil.FileProcessor processor) throws TechnicalException {
        fileFetcherUtil.readFileAndApplyProcessing(file, processor);
	}

    public void deployJEEArtifact(String fileName, InputStream stream, FileFetcherUtil.FileProcessor processor) {
        fileFetcherUtil.fetchInputStreamAndApplyProcessing(fileName, null, stream, processor);
    }


    // Test support

    public void callMeBackToPerformProcessing(String filename, File file) {

    }


}
