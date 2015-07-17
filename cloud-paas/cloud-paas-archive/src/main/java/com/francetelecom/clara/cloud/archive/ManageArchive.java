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
package com.francetelecom.clara.cloud.archive;

import java.io.File;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;

public interface ManageArchive {
	
	/**
	 * generate an ear file in a temporary directory for the given mavenReference and contextRoot.
	 * <b>WARNING: caller should delete ear file and parent directory after use</b> 
	 * @param mavenReference the ear Maven reference, used to define war and ear file names
	 * @param contextRoot the war context root
	 * @return the generated ear File
	 * @throws TechnicalException mainly for IOException
	 */
	public File generateMinimalEar(MavenReference mavenReference, String contextRoot) throws TechnicalException;


    /**
     * generate an war file in a temporary directory for the given mavenReference.
     * <b>WARNING: caller should delete war file and parent directory after use</b>
     * @param mavenReference the ear Maven reference, used to define war and ear file names
     * @param contextRoot the war context root
     * @return the generated ear File
     * @throws TechnicalException mainly for IOException
     */
    public File generateMinimalWar(MavenReference mavenReference, String contextRoot) throws TechnicalException;

}