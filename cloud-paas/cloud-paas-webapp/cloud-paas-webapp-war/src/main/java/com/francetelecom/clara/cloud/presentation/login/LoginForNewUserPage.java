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
package com.francetelecom.clara.cloud.presentation.login;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * 
 * @author AMCU6536
 *
 */

@MountPath("/login/newUser")
public class LoginForNewUserPage extends WebPage {

	/**
     * serialUID
     */
    private static final long serialVersionUID = -9029748747538352173L;
    
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(LoginForNewUserPage.class);

    @SpringBean(name = "signUpLinks")
   	private SignUpLinks signUpLinks;
    
    public LoginForNewUserPage() {
    	super();
    }
    
    @Override
    protected void onInitialize() {
        initComponent();
        setOutputMarkupId(true);
        super.onInitialize();
    }
    
    private void initComponent() {
    	String urlForCreate = signUpLinks.getSignupUrl();
    	getRequestCycle().scheduleRequestHandlerAfterCurrent(new 
    		    RedirectRequestHandler(urlForCreate));
        	
	}
	public void setSignUpLinks(SignUpLinks signUpLinks) {
		this.signUpLinks = signUpLinks;
	}

}
