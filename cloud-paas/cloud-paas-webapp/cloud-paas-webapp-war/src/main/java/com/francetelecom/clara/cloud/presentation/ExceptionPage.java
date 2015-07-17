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
package com.francetelecom.clara.cloud.presentation;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.francetelecom.clara.cloud.presentation.common.NavigationMenuFirstLevel;
import com.francetelecom.clara.cloud.presentation.common.PageTemplate;

/**
 * Generic exception handling. This page is called by exceptions : - pages
 * exceptions - ExecutionHandlerRequestCycle::onRuntimeException Updated :
 * $LastChangedDate$
 * 
 * @author : $Author$
 * @version : $Revision$
 */
@AuthorizeInstantiation({"ROLE_USER","ROLE_ADMIN"})
public  abstract class ExceptionPage extends PageTemplate {
	/**
	 * serialUID
	 */
	private static final long serialVersionUID = 7894818834200368614L;
	
	/**
	 * Constructor
	 * 
	 * @param pageParameters
	 * @param e
	 */
	public ExceptionPage(PageParameters pageParameters, String errorKey) {
		super(pageParameters);
		init(pageParameters,errorKey);
	}

	private void init(PageParameters pageParameters,String errorKey) {
		NavigationMenuFirstLevel navFirstLvl = new NavigationMenuFirstLevel();
		add(navFirstLvl);
		add(new Label("message", getString("portal.error.message")));
		add(new Label("head_page_title", getString(errorKey)));
		add(new Label("description", this.getString(errorKey)));
	}

	public void setExceptionDetails(String details) {
		Label detailsLabel = new Label("details", details);
		add(detailsLabel);
		detailsLabel.setVisible(details != null);
	}
		
}