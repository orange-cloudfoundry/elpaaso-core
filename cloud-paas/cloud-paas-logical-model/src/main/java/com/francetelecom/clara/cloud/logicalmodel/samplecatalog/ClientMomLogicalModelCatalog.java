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
package com.francetelecom.clara.cloud.logicalmodel.samplecatalog;

import java.net.URL;
import java.util.Map;

import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;

public class ClientMomLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

	@Override
	public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInstantiable() {
        return false;
    }

    @Override
    public String getAppCode() {
        return "MyClientMomSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "MyClientMomSample";
    }

    @Override
    public String getAppDescription() {
        return "MyClientMomSample description";
    }

    @Override
    public String getAppReleaseDescription() {
        return "MyClientMomSampleCODE release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
