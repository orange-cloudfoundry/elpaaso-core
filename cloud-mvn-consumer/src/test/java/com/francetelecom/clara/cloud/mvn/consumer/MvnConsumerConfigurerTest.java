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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MvnConsumerConfigurerTest {

	protected MvnConsumerConfigurer mvnConsumerConfigurer;
	File templateFile;

	@Before
	public void setup() throws IOException {
		this.mvnConsumerConfigurer = new MvnConsumerConfigurer();
		mvnConsumerConfigurer.setPullRepositoryUrls(new String[]{"http://repo1.com", "http://repo2.com"});

		File dummyLocalRepo = new File("target/dummy-local-repo/");
		FileUtils.forceMkdir(dummyLocalRepo);
		this.mvnConsumerConfigurer.setLocalM2RepoPath(dummyLocalRepo);

		File templateFile = new File("target" + File.separator + "templateFile.xml");
		templateFile.createNewFile();
		this.mvnConsumerConfigurer.setAssemblyTemplateFile(new FileInputStream(templateFile));
		templateFile.deleteOnExit();
	}

	@Test
	public void should_cleanup_local_repo_on_startup() throws IOException {
		File dummyDir = new File(this.mvnConsumerConfigurer.getLocalM2RepoPath() + File.separator + "xxx");
		FileUtils.forceMkdir(dummyDir);

		assertTrue("Directory :" + dummyDir + "should exist", dummyDir.isDirectory());
		this.mvnConsumerConfigurer.init();

		assertFalse("Directory :" + dummyDir + "should NOT exist", dummyDir.exists());

	}


	@Test
	public void should_support_multiple_ordered_pull_repo() throws IOException {
		String[] repos=new String[]{"http://repo1.com", "http://repo2.com"};
		this.mvnConsumerConfigurer.setPullRepositoryUrls(repos);
		this.mvnConsumerConfigurer.init();

		List<RemoteRepository> remoteRepos = this.mvnConsumerConfigurer.getPullRemoteRepo();
		assertEquals("Should contain 2 repo", 2, remoteRepos.size());
		assertEquals("Should find repo1", "http://repo1.com", remoteRepos.get(0).getUrl());
		assertEquals("Should find repo2", "http://repo2.com", remoteRepos.get(1).getUrl());
	}

	@Test(expected = NullPointerException.class)
	public void should_fail_when_no_pull_repo_defined() throws IOException {
		this.mvnConsumerConfigurer.setPullRepositoryUrls(null);
		this.mvnConsumerConfigurer.init();
		fail("should fail during init if no pull repo defined");
	}
}
