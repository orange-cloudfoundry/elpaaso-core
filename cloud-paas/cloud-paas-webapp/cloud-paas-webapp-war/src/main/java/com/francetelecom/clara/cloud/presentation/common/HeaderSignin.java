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
package com.francetelecom.clara.cloud.presentation.common;

import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;

/**
 * HeaderSignin
 * Under title header including welcome msg, lang link and logout button
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 10/05/11
 * Updated  : $LastChangedDate$
 * @author  : $Author$
 * @version : $Revision$
 */
public class HeaderSignin extends Panel {
	
	private Label labelLang ;

    /**
     * serialUID
     */
    private static final long serialVersionUID = -9029748747538352173L;
    /**
     * Logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(HeaderSignin.class);
    /**
     * lang switch link
     */
    private SwitchLangLink langLink;
    
    private  LogoutLink logout;

    public HeaderSignin(String id) {
        super(id);
        createComponents();
    }

    @Override
	protected void onBeforeRender() {
    	
    	labelLang.setDefaultModel(getStringResourceModel("portal.design.header.signin.lang"));
    	langLink.add(labelLang);
    	
    	IModel<String> onLogoutMessage = new StringResourceModel("portal.headercommon.logoutconfirm", this, null);
        logout.add(new AttributeModifier("onclick", "return confirm('" + onLogoutMessage.getObject().replace("'", "\\'")+ "');"));
        
        super.onBeforeRender();
       
	}

    private void createComponents() {
        logger.debug("create header signin panel");

        PaasUser paasUser = WicketSession.get().getPaasUser();
        Label username;

        if (paasUser == null) {            // not connected
            getSession().invalidate();
            setResponsePage(HomePage.class);
            return;
        } else {
            username = new Label("loggedUserName", paasUser.getFirstName());
        }

        add(username);

//        LogoutLink logout = new LogoutLink("logoutLink");
        logout = new LogoutLink("logoutLink");
        add(logout);
//        IModel<String> onLogoutMessage = new StringResourceModel("portal.headercommon.logoutconfirm", this, null);
//        logout.add(new AttributeModifier("onclick", "return confirm('" + onLogoutMessage.getObject().replace("'", "\\'")+ "');"));

        Locale currentLocale = getSession().getLocale();
        String localeDisplayName = currentLocale.getDisplayName();
        logger.debug("refreshSwitchLangLink() : current local display name : {} ", localeDisplayName);
        if (Locale.FRENCH.equals(currentLocale)) {
            langLink = new SwitchLangLink("switchLangLink", Locale.ENGLISH);
        } else {
            langLink = new SwitchLangLink("switchLangLink", Locale.FRENCH);
        }

        add(langLink);

//        langLink.add(new Label("lang", getStringResourceModel("portal.design.header.signin.lang")));
        labelLang = new Label("lang","version anglaise");
        langLink.add(labelLang);
    }
    
    /**
     * define a link for logout with confirmation
     */
    private class LogoutLink extends Link<Void> {

        private static final long serialVersionUID = -1082992144163134177L;

        public LogoutLink(String id) {
            super(id);

            /** We just need to add this event for accessibility **/
            add(new AjaxEventBehavior("onkeypress") {

                private static final long serialVersionUID = 4037973119477629991L;

                @Override
                protected void onEvent(AjaxRequestTarget target) {
                }
            });
        }

        @Override
        public void onClick() {
        	if (WicketSession.get() != null) {
	            PaasUser paasUser = WicketSession.get().getPaasUser();
	        	if (paasUser != null) {
	        		logger.info(paasUser.getSsoId() + " ("+paasUser.getFullName()+") logged OUT with wicket session "+WicketSession.get().getId());
	        	}
        	}
            getSession().invalidate();
            setResponsePage(HomePage.class);
        }
    }

    private StringResourceModel getStringResourceModel(java.lang.String key) {
        // BVA fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
        return new StringResourceModel(key, this, null);
    }

    /**
     * Define link for switching locale
     */
    private class SwitchLangLink extends Link<Void> {

        private static final long serialVersionUID = -4849232308019888045L;
        private Locale locale;

        public SwitchLangLink(String id, Locale locale) {
            super(id);
            this.locale = locale;
        }

        @Override
        public void onClick() {
            logger.debug("Lang switch to " + locale.toString());
            getSession().setLocale(locale);
            if (Locale.FRENCH.equals(locale)) {
                this.locale = Locale.ENGLISH;
            } else {
                this.locale = Locale.FRENCH;
            }
            setResponsePage(this.getPage());
        }

        @Override
        public boolean isVisible() {
            if (getPage() instanceof HomePage) {
                return true;
            }
            return false;
        }
    }
}
