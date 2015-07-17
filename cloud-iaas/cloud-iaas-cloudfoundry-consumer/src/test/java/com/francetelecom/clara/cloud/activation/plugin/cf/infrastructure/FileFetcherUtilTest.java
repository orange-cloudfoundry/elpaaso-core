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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.francetelecom.clara.cloud.commons.MavenReference;

@RunWith(MockitoJUnitRunner.class)
public class FileFetcherUtilTest {

	@Spy
	private SampleProcessorCaller caller = new SampleProcessorCaller();

    @Mock
    private FileFetcherUtil.FileProcessor fileProcessor;


    URL accessUrl;

    @Before
    public void setUp() {
        accessUrl = FileFetcherUtilTest.class.getClassLoader().getResource("apps/hello-env.war");
    }

    @Test
    @Ignore
    public void supports_plain_files_for_tests() {
        //given: mock InputStream...

        //when
        caller.deployJEEArtifact(new File("path"), fileProcessor);

        //then
        caller.callMeBackToPerformProcessing("path", any(File.class));
    }
    
    @Test
    @Ignore
    public void supports_maven_ref() throws MalformedURLException {
        //given: mock InputStream...
        MavenReference mavenReference = new MavenReference("groupId", "artefactId", "version", "ear");
        mavenReference.setAccessUrl(accessUrl);
        mavenReference.setType("war");

        //when
        caller.deployJEEArtifact(mavenReference, fileProcessor);

        //then
        caller.callMeBackToPerformProcessing("path", any(File.class));
    }

    @Test
    @Ignore
    public void supports_input_stream_received_from_network() {
        //given: mock InputStream...

        //when
        caller.deployJEEArtifact("fileName.jar", mock(InputStream.class), fileProcessor);

        //then
        caller.callMeBackToPerformProcessing("path", any(File.class));

    }

}
