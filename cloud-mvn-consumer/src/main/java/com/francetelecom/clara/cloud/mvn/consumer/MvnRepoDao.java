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
import java.util.ArrayList;

import com.francetelecom.clara.cloud.commons.MavenReference;


public interface MvnRepoDao  {



	/**
	 * Resolves the access Url of the specified MavenReference
	 * @param mavenReference The original maven reference which access Url needs to be filled
     * @return a nex MavenReference instance with the accessUrl filled
     * @throws com.francetelecom.clara.cloud.commons.TechnicalException if the URL is malformed or can't be resolved.
	 */
	MavenReference resolveUrl(MavenReference mavenReference);

    /**
     * Retrieve the file corresponding to a maven reference from the local repository
     * This method may try to resolve url and download file in the local repository 
     * But it does not update the maven refernce url 
     * @param mavenReference
     * @return the File object representing the maven reference in the local repository
     */
	File getFileFromLocalRepository(MavenReference mavenReference);

	/**
	 * should deploy a fileset into maven repo
	 * 
	 * @param gav
	 *            ref to be deploy to
	 * @param fileSet
	 *            to deploy
	 */
	void deployFileset(MavenReference gav, ArrayList<FileRef> fileSet);
}