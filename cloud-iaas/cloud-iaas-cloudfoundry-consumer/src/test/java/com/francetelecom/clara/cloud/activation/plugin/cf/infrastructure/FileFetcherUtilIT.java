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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:com/francetelecom/clara/cloud/cfconsumer/FileFetcherUtilIntegrationTest-context.xml")
public class FileFetcherUtilIT {

    @Autowired
    private MvnRepoDao mvnRepoDao;

    @Autowired
    @Qualifier("jeeProbeMavenReference")
    private MavenReference jeeProbeMavenReference;


    @Spy
	private SampleProcessorCaller caller = new SampleProcessorCaller();

    @Test
    public void classloader_can_fetch_jeeprobe_added_as_maven_dependency_and_process_it_with_fetcher_util() {
        //given
        MavenReference jeeProbeEarRef = mvnRepoDao.resolveUrl(jeeProbeMavenReference);
        URL url = jeeProbeEarRef.getAccessUrl();

        assertThat(url).isNotNull();

        //when
        FileFetcherUtil.FileProcessor processor = mock(FileFetcherUtil.FileProcessor.class);
        caller.deployJEEArtifact(jeeProbeEarRef, processor);

        //then
        //with paas sample release version, full name is paas-probe-jee-ear-1.0.22.ear
        //with snapshot, full name is paas-probe-jee-ear-1.0.38-20140701.141043-2.ear
		// so we check the start
		String expectedStart = jeeProbeMavenReference.getArtifactId() + "-" + jeeProbeMavenReference.getVersion().replace("SNAPSHOT", "");
		verify(processor).process(startsWith(expectedStart), eq("ear"), any(File.class));
    }

}
