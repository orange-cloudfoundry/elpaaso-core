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
package com.francetelecom.clara.cloud.presentation.utils;


import com.francetelecom.clara.cloud.presentation.WicketApplication;
import org.apache.wicket.Session;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.context.ApplicationContext;

import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 12/01/12
 * Time: 16:32
 */
public class PaasTestApplication extends WicketApplication {

    /**
     * logger
     */
//    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(PaasTestApplication.class);

    private ApplicationContext context;

    private boolean useAuthentication = false;

    public PaasTestApplication() {
    }

    public ApplicationContext getContext() {
        return context;
    }

    public PaasTestApplication(ApplicationContext context, boolean useAuthentication) {
        this.context = context;
        this.useAuthentication = useAuthentication;
    }

    public PaasTestApplication(ApplicationContext context) {
        this.context = context;
    }

    @Override
    protected void mountBookmarks() {
        // nothing to do to avoid mounting twice same bookmark when reinstantiate wickettester.
    }

    @Override
    protected void defineSpringInjector() {
        if (context != null) {
            getComponentInstantiationListeners().add(new SpringComponentInjector(this, context,true));
        }
    }



    @Override
    public Session newSession(Request request, Response response) {
        PaasTestSession session = new PaasTestSession(request, useAuthentication);
            // english is default language
        session.setLocale(Locale.US);
        return session;
   }

}
