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

import com.francetelecom.clara.cloud.presentation.tools.LdapAccessChecker;

/**
 * 
 * @author AMCU6536
 *
 */

@MountPath("/login/forgotPassword")
public class LoginForgotPage extends WebPage {

	/**
     * serialUID
     */
    private static final long serialVersionUID = -9029748747538352173L;
    
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(LoginForgotPage.class);

    @SpringBean(name = "signUpLinks")
   	private SignUpLinks signUpLinks;
    

	@SpringBean(required=false)
	private LdapAccessChecker ldapAccessChecker;
    
    public LoginForgotPage() {
    	super();
    }
    
    @Override
    protected void onInitialize() {
        initComponent();
        setOutputMarkupId(true);
        super.onInitialize();
    }
    
    private void initComponent() {
    	String urlForgot = signUpLinks.getForgottenUrl();
    	getRequestCycle().scheduleRequestHandlerAfterCurrent(new 
    		    RedirectRequestHandler(urlForgot));
        	
	}
	public void setSignUpLinks(SignUpLinks signUpLinks) {
		this.signUpLinks = signUpLinks;
	}
	
	/**
	 * @param ldapAccessChecker the ldapAccessChecker to set
	 */
	public void setLdapAccessChecker(LdapAccessChecker ldapAccessChecker) {
		this.ldapAccessChecker = ldapAccessChecker;
	}
}
