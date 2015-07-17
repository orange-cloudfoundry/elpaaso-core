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

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.presentation.resource.LocateResources;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 10/05/11
 */
public class PageTemplate extends WebPage {
    /**
     * serialUID
     */
    private static final long serialVersionUID = -2406144181450494997L;

    /**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(PageTemplate.class);
    public static final int ROWS_PER_PAGE = 15;

    /**
	 * paas user layer manager
	 */
	@SpringBean
    ManagePaasUser managePaasUser;

    /**
     * navigation header component.
     * allows to access directly some parts on the current web page
     */
    private HeaderNavigation headerNavigation;

    /**
     * signin header component.
     * display :
     *   - logged user first name
     *   - logged user last name
     *   - logged user subtenant
     *   - loggout link
     *   - change language link
     */
    private HeaderSignin headerSignin;

    /**
     * page title component
     * contains :
     *   - Orange logo
     *   - portal name
     *   - France Telecom logo
     */
    private HeaderTitle headerTitle;

    /**
     * footer component
     * contains somme useful information
     *      - elpaaso portal version
     *      - user ssoid
     *      - user profile
     */
    private Footer footer;

    @Override
    public void renderHead(IHeaderResponse response) {
    	
        response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(LocateResources.class, "elpaaso_common.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(LocateResources.class, "elpaaso_designer.css")));  
//        response.render(CssHeaderItem.forReference(new CssResourceReference(Application.class, "elpaaso_common.css")));
//        response.render(CssHeaderItem.forReference(new CssResourceReference(Application.class, "elpaaso_designer.css")));  
        super.renderHead(response);
    }

    public void logSessionSize() {
        long storeCurrentSizeBytes = -1;
        try {
            // wont work with back button !
            storeCurrentSizeBytes = getSession().getSizeInBytes();
        } catch (Exception ee) {
            logger.warn("unable to stat session size : {} ", ee.getMessage());
            return;
        }
        float storeCurrentSizeMB = storeCurrentSizeBytes/1024000;
        String storeSizeStr = (storeCurrentSizeMB > 1
                    ? storeCurrentSizeMB + " MB"
                    : storeCurrentSizeBytes+" Bytes");
        logger.debug("Wicket Session store size {}", storeSizeStr);
    }


    /**
     * PageTemplate constructor
     * @param params - page parameters map
     */
    public PageTemplate(final PageParameters params) {
        super(params);
        headerNavigation = new HeaderNavigation("header_navigation");
        headerSignin = new HeaderSignin("header_signin");
        headerTitle = new HeaderTitle("header_title");

        footer = new Footer("footer");

        add(headerNavigation);
        add(headerSignin);
        add(headerTitle);
        add(footer);
        logSessionSize();
    }

}
