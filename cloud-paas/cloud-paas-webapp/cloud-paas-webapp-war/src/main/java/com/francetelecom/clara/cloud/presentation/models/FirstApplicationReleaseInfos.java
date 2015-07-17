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
package com.francetelecom.clara.cloud.presentation.models;

import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * FirstApplicationReleaseInfos
 *
 * POJO representing application first release
 * used by ApplicationCreatePanel::appForm
 */
public class FirstApplicationReleaseInfos extends ApplicationRelease {

    private static final long serialVersionUID = 4428930301088961018L;

    @NotNull
    @Size(max = 255)
    String appLabel;
    @NotNull
    @Size(max = 255)
    String appCode;
    @Size(max = 255)
    String appDescription;
    @NotNull
    Boolean appPublic;

    // Field must match a list of members separated by spaces,
    // the "central" regular expression must be the same as in SSOId class
    @NotNull
    @Pattern(regexp="(\\b[a-zA-Z]+[0-9]*\\b\\s*)+", message="{portal.application.members.label.javax.validation}")
    String members;
    
    public FirstApplicationReleaseInfos() {
    	members = WicketSession.get().getPaasUser().getSsoId().getValue();
    	appPublic = Boolean.FALSE;
    }
    
    public String getAppLabel() {
        return appLabel;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAppDescription() {
        return appDescription;
    }

    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
    }
    
    public void setMembers(String members) {
        this.members = members;
    }
    
    public String getMembers() {
    	return members;
    }
    
    public Boolean getAppPublic() {
        return appPublic;
    }

    public void setAppPublic(Boolean appPublic) {
        this.appPublic = appPublic;
    }
}
