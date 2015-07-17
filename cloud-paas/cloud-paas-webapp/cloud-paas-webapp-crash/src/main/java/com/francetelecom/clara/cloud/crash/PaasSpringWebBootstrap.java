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
package com.francetelecom.clara.cloud.crash;

import java.io.File;
import java.io.IOException;

import org.crsh.spring.SpringWebBootstrap;
import org.crsh.vfs.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaasSpringWebBootstrap extends SpringWebBootstrap {
	public static final String PAAS_CRASH_DIRECTORY_KEY = "paas.crash.directory";
	protected static Logger LOG = LoggerFactory.getLogger(PaasSpringWebBootstrap.class);

	private File paasCrashDirectory = null;

	public PaasSpringWebBootstrap(String paasCrashDirectory) throws IOException {
		LOG.info("PaaSSpringWebBootstrap constructor() paasCrashDirectory={}", paasCrashDirectory);
		checkAndSetPaasCrashDirectory(paasCrashDirectory);
	}

	private void checkAndSetPaasCrashDirectory(String paasCrashPath) {
		if (paasCrashPath == null) {
			throw new RuntimeException("crash path not set [" + PAAS_CRASH_DIRECTORY_KEY + "]");
		}
		File crashDir = new File(paasCrashPath);
		if (!crashDir.exists()) {
			LOG.error("unable to mount crash directory (missing) : {}={}", PAAS_CRASH_DIRECTORY_KEY, paasCrashPath);
			return;
		}
		if (!crashDir.canRead() && crashDir.isDirectory()) {
			LOG.error("unable to mount crash directory (no read permission or not a directory) : {}={}", PAAS_CRASH_DIRECTORY_KEY, paasCrashPath);
			return;
		}
		this.paasCrashDirectory = crashDir;
		LOG.info("CRaSH scripts path : {}", paasCrashPath);
	}

	  @Override
	  protected String getDefaultCmdMountPointConfig() {
	    return "classpath:/crash/commands/";
	  }

	  @Override
	  protected String getDefaultConfMountPointConfig() {
	    return "classpath:/crash/";
	  }
	
	@Override
	protected FS createCommandFS() throws IOException {
		FS commandFS = super.createCommandFS();
		LOG.info("CRaSH mount directories");
		if (paasCrashDirectory != null) {
			mountCrashDirectory(commandFS, paasCrashDirectory);
		}
		return commandFS;
	}

	private void mountCrashDirectory(FS commandFS, File paasCrashDirectory) throws IOException {
		LOG.debug("CRaSH PaaS directory mount : {}", paasCrashDirectory.getAbsolutePath());
		commandFS.mount(paasCrashDirectory);
	}
	
	
	
}
