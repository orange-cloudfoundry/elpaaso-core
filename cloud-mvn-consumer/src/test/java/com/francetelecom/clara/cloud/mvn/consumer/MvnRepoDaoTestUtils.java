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
package com.francetelecom.clara.cloud.mvn.consumer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.francetelecom.clara.cloud.commons.MavenReference;
import org.slf4j.LoggerFactory;

public class MvnRepoDaoTestUtils {
    /**
     * logger
     */
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(MvnRepoDaoTestUtils.class);
    /**
     * Configure resolveUrl method of a MvnRepDao mock Mock must have been
     * created using Mockito ResolveUrl is configured to keep accessUrl
     *
     * accessUrl must already be set (test purpose)
     *
     * @param mvnRepoDaoMock
     *            the mvnRepoDaoMock created with Mockito
     */
    public static void mockResolveUrlDoNothing(MvnRepoDao mvnRepoDaoMock) {
        when(mvnRepoDaoMock.resolveUrl(any(MavenReference.class))).thenAnswer(new Answer<MavenReference>() {
            @Override
            public MavenReference answer(InvocationOnMock invocation) throws Throwable {
                MavenReference input = (MavenReference) invocation.getArguments()[0];
                MavenReference output = null;
                // Surprisingly, invocation is sometimes null...
                if (input != null) {
                    if (input.getAccessUrl() == null) {
                        logger.warn("using mocked version of mvn consumer without final access url result");
                    }
                    output = new MavenReference(input);
                }
                return output;
            }
        });

    }

	/**
	 * Configure resolveUrl method of a MvnRepDao mock Mock must have been
	 * created using Mockito ResolveUrl is configured to set accessUrl as
	 * http://myrepo:80/<artifactName>
	 * 
	 * @param mvnRepoDaoMock
	 *            the mvnRepoDaoMock created with Mockito
	 */
	public static void mockResolveUrl(MvnRepoDao mvnRepoDaoMock) {
		when(mvnRepoDaoMock.resolveUrl(any(MavenReference.class))).thenAnswer(new Answer<MavenReference>() {
			@Override
			public MavenReference answer(InvocationOnMock invocation) throws Throwable {
				MavenReference input = (MavenReference) invocation.getArguments()[0];
				MavenReference output = null;
				// Surprisingly, invocation is sometimes null...
				if (input != null) {
					output = new MavenReference(input);
					// compute a representative accessurl
					output.setAccessUrl(new URL("http://myrepo:80/" + input.getArtifactName()));
				}
				return output;

			}
		});

	}

	/**
	 * Configure getFileFromLocalRepository method of a MvnRepDao mock Mock must
	 * have been created using Mockito getFileFromLocalRepository is configured
	 * to return a file named
	 * /localrepo/<artifactGroup>/<artifactName>.<artifactType>
	 * 
	 * @param mvnRepoDaoMock
	 *            the mvnRepoDaoMock created with Mockito
	 */
	public static void mockGetFileFromLocalRepository(MvnRepoDao mvnRepoDaoMock) {
		when(mvnRepoDaoMock.getFileFromLocalRepository(any(MavenReference.class))).thenAnswer(new Answer<File>() {
			@Override
			public File answer(InvocationOnMock invocation) throws Throwable {
				MavenReference input = (MavenReference) invocation.getArguments()[0];
				File output = null;
				// Surprisingly, invocation is sometimes null...
				if (input != null) {
					String fileName = "/localrepo/" + input.getGroupId() + "/" + input.getArtifactId() + "." + input.getExtension();
					output = new File(fileName);
				}
				return output;

			}
		});

	}
}
