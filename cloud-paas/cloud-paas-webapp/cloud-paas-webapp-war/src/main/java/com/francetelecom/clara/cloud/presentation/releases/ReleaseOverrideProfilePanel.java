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
package com.francetelecom.clara.cloud.presentation.releases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile.MiddlewareProfileStatus;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;

/**
 * ReleaseOverrideProfilePanel
 *
 * panel that enable the user to choose an other middleware profile
 *
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
public class ReleaseOverrideProfilePanel extends Panel {

    @SpringBean
    private ManageApplicationRelease manageApplicationRelease;

	private DropDownChoice<MiddlewareProfile> middlewareProfileSelect;
    private WebMarkupContainer middlewareProfileContainer;
    
    public ReleaseOverrideProfilePanel(String id) {
        super(id);
        initComponents();
    }
    
    public static class ProfileChoiceRenderer extends ChoiceRenderer<MiddlewareProfile>{
    	//Create choice renderer for middleware profile
        final IChoiceRenderer<MiddlewareProfile.MiddlewareProfileStatus> statusRenderer;
        private Component parent;
        
        public ProfileChoiceRenderer(Component parent, IChoiceRenderer<MiddlewareProfileStatus> statusRenderer){
        	this.parent = parent;
        	this.statusRenderer =  statusRenderer; 
        }
    
     	public String getDisplayValue(MiddlewareProfile profile) {
     		StringBuilder builder = new StringBuilder();
     		builder.append(profile.getVersion());
     		builder.append(" (");
     		if(MiddlewareProfile.getDefault() == profile){
     			builder.append(getDefaultLabel());
     		}
     		builder.append(statusRenderer.getDisplayValue(profile.getStatus()));
     		builder.append(")");
    		return builder.toString();
     	}

		public String getDefaultLabel() {
			return parent.getString("portal.release.overrideProfile.default")+", ";
		};
     	
     	public String getIdValue(MiddlewareProfile profile, int index) {
    		return profile.getVersion();
     	};
    }

    private void initComponents() {
        initContainers();
        
	    //Select middlewareProfile choice
		middlewareProfileSelect = new DropDownChoice<>("middlewareProfileSelect",
                                                             new Model<MiddlewareProfile>(),
                                                             getMiddlewareProfileList(), 
                                                             new ProfileChoiceRenderer(this, new EnumChoiceRenderer<MiddlewareProfile.MiddlewareProfileStatus>(this)));
        middlewareProfileSelect.setNullValid(false);
        middlewareProfileSelect.setRequired(true);
        middlewareProfileSelect.setDefaultModelObject(MiddlewareProfile.getDefault());
        middlewareProfileContainer.add(middlewareProfileSelect);
    }

    private void initContainers() {
        middlewareProfileContainer = new WebMarkupContainer("middlewareProfileContainer");
        middlewareProfileContainer.setOutputMarkupPlaceholderTag(true);
        middlewareProfileContainer.setOutputMarkupId(true);
        add(middlewareProfileContainer);
    }

    public List<MiddlewareProfile> getMiddlewareProfileList() {
        List<MiddlewareProfile> displayedMiddlewareProfiles = new ArrayList<>();
        List<MiddlewareProfile> availableMiddlewareProfiles = manageApplicationRelease.findAllMiddlewareProfil();
        PaasUser currentUser = WicketSession.get().getPaasUser();
        Set<MiddlewareProfile> authorizedProfiles = MiddlewareProfile.filter(currentUser, availableMiddlewareProfiles);
        for (MiddlewareProfile middlewareProfile : authorizedProfiles) {
            displayedMiddlewareProfiles.add(middlewareProfile);
        }
        Collections.sort(displayedMiddlewareProfiles);
        return displayedMiddlewareProfiles;
    }

    public MiddlewareProfile getCurrentMiddlewareProfile() {
        return middlewareProfileSelect.getModelObject();
    }
}
