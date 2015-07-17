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

import java.util.Locale;

import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.bean.validation.BeanValidationConfiguration;
import org.apache.wicket.core.request.mapper.CryptoMapper;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;

import com.francetelecom.clara.cloud.presentation.tools.ServerPageMetaFilter;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;
import com.francetelecom.clara.cloud.scalability.ManageStatistics;

import de.agilecoders.wicket.webjars.WicketWebjars;


/**
 * ACP wicket application configure general application settings and define
 * application session
 */
public class WicketApplication extends AuthenticatedWebApplication { // implements IWiQuerySettings {

    /**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(WicketApplication.class);

    public WicketApplication() {
    }
    @SpringBean
    ManageStatistics manageStatistics;

//    @SpringBean(name = "wicketUrlEncryptor")
//    StandardPBEByteEncryptor urlEncryptor;

    public ManageStatistics getManageStatistics() {
        return manageStatistics;
    }

    @SuppressWarnings({"UnusedDeclaration"}) // used by spring
    public void setManageStatistics(ManageStatistics ms) {
        this.manageStatistics = ms;
    }

    /**
     * The application web session
     *
     * @see com.francetelecom.clara.cloud.presentation.tools.WicketSession
     */
    @Override
    protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
        return WicketSession.class;
    }


    /**
     * The application login page
     *
     * Redirect to HomePage and with Spring Security redirect to login.html
     */
    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return HomePage.class;
    }

    /**
     * The application home page
     *
     * @see com.francetelecom.clara.cloud.presentation.HomePage
     */
    @Override
    public Class<? extends Page> getHomePage() {
        // return
        // com.francetelecom.clara.cloud.presentation.page.HomePage.class;
        return HomePage.class;

    }

    /**
     * Action at the session start :
     *  * Add the User provided by GASSI in the application session
     *  * Add the corresponding PaaS user into session
     *  * Add demo data
     */
    @Override
    public Session newSession(Request request, Response response) {
        if (logger.isDebugEnabled()) {
            logger.debug("Created a new AcpSession");
        }
        WicketSession session = new WicketSession(request);
        // english is default language
        session.setLocale(Locale.US);
        return session;
    }

    /**
     * Define application settings
     */
    @Override
    protected void init() {
        if (logger.isDebugEnabled()) {
            logger.debug("Wicket application init (configurationType:[{}])", getConfigurationType());
        }
        defineSpringInjector();
        if (!RuntimeConfigurationType.DEVELOPMENT.equals(getConfigurationType())) {
            // change default error/timeout pages in production mode
            //defineErrorPage();
            // remove "wicket:id" from html generated code in production mode
            getMarkupSettings().setStripWicketTags(true);
        } else {
            setupDevelopmentSettings();
        }
        //TODO Remove
  //     getDebugSettings().setAjaxDebugModeEnabled(true);
        IApplicationSettings settings = getApplicationSettings();
//        https://cwiki.apache.org/WICKET/error-pages-and-feedback-messages.html
        settings.setInternalErrorPage(UnknownExceptionPage.class);
//        settings.setAccessDeniedPage();
//        settings.setPageExpiredErrorPage();
        getRequestCycleListeners().add(new ExecutionHandlerRequestCycle(this, new WebPageBaseFactory()));
        //TODO : Not available in Wicket 1.5 because it's enabled by default. Why was it disabled?
//        getPageSettings().setAutomaticMultiWindowSupport(false);
        //will encrypt/decrypt the URLs generated by the inner one
        IRequestMapper cryptoMapper = new CryptoMapper(getRootRequestMapper(), this);
        setRootRequestMapper(cryptoMapper);
//        ICryptFactory jasyptCryptFactory = new JasyptCryptFactory(urlEncryptor);
//        getSecuritySettings().setCryptFactory(jasyptCryptFactory);
        mountBookmarks();
//        https://cwiki.apache.org/WICKET/request-mapping.html
//        https://issues.apache.org/jira/browse/WICKET-4488
//        URL with a previous page version ignores requested page based on mount path
//        http://apache-wicket.1842946.n4.nabble.com/I-don-t-want-url-page-count-parameter-localhost-8080-context-0-td4481510i40.html
        super.init();

        WicketWebjars.install(this);

        new BeanValidationConfiguration().configure(this);

        // For VMWare SDK logging (vCloud API)
        // call only once during initialization time of your application
//		Do not work (NPE on undeploy)
//		SLF4JBridgeHandler.install();        
    }




    /**
     * init the dev mode
     * @see org.apache.wicket.response.filter.IResponseFilter
     */
    protected void setupDevelopmentSettings() {
        IRequestCycleSettings reqSettings = getRequestCycleSettings();
        reqSettings.setBufferResponse(true);
        // reqSettings.addResponseFilter(new ServerTimeFilter());
        reqSettings.addResponseFilter(new ServerPageMetaFilter());

    }
    /**
     * init the permalinks
     * cf. mingle#1120
     */
    protected void mountBookmarks() {
        new AnnotatedMountScanner().scanPackage("com.francetelecom.clara.cloud.presentation").mount(this);
    }

    protected void defineSpringInjector() {
        // SpringComponentInjector uses Spring's WebApplicationContextUtils
        // class to retrieve
        // Spring context (loaded from web.xml file).
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));
    }

}
