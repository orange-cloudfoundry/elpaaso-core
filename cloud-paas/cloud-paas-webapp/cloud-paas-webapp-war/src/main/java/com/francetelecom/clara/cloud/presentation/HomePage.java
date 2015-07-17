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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import com.francetelecom.clara.cloud.presentation.common.Breadcrumbs;
import com.francetelecom.clara.cloud.presentation.common.NavigationMenuFirstLevel;
import com.francetelecom.clara.cloud.presentation.common.PageTemplate;
import com.francetelecom.clara.cloud.presentation.models.HypericBean;
import com.francetelecom.clara.cloud.presentation.models.SplunkBean;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;
import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import com.francetelecom.clara.cloud.presentation.tools.PopulateDatasPage;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;


/**
 * Home page class
 */

@MountPath("/home")
@AuthorizeInstantiation({"ROLE_USER","ROLE_ADMIN"})
public class HomePage extends PageTemplate {

    @SpringBean
    private SplunkBean splunkBean;

    @SpringBean
    private HypericBean hypericBean;

    private FeedbackPanel feedback;

    private static final long serialVersionUID = -6381089242808635165L;

    /**
     * create home page
     * @param params - pageParameters map
     */
    public HomePage(PageParameters params) {
        super(params);
    }


    @Override
    protected void onInitialize() {
        initComponents();
        setOutputMarkupId(true);
        super.onInitialize();
    }

    private void initComponents() {
        NavigationMenuFirstLevel navFirstLvl = new NavigationMenuFirstLevel();
        add(navFirstLvl);
        /* set head page title to display in browser title bar */
        add(new Label("head_page_title", getString("portal.design.web.title.homepage")));

        List<BreadcrumbsItem> breadcrumbsItems = new ArrayList<BreadcrumbsItem>();

        breadcrumbsItems.add(new BreadcrumbsItem(this.getClass(), "portal.design.breadcrumbs.homepage", null, true));

        Breadcrumbs breadcrumbs = new Breadcrumbs("breadcrumbs", breadcrumbsItems);
        add(breadcrumbs);

        feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        add(getSplunkExternalLink());

        add(getHypericExternalLink());


        // populate link if i am an admin
        boolean isAdmin = WicketSession.get().getRoles().hasRole("ROLE_ADMIN");
        WebMarkupContainer processUser= new WebMarkupContainer("process-user");
        WebMarkupContainer processAdmin= new WebMarkupContainer("process-admin");
        add(processUser);
        add(processAdmin);
        processUser.setVisible(!isAdmin);
        processAdmin.setVisible(isAdmin);

        BookmarkablePageLink<Void> populateLink = new BookmarkablePageLink<Void>("populateLink", PopulateDatasPage.class);
//        populateLink.add(new Image("populateLinkImg", new PackageResourceReference(LocateResources.class,"process_info_icon.png")));
        populateLink.add(new CacheActivatedImage("image.populateLinkImg", new ResourceModel("process_info_icon").getObject()));
        populateLink.add(new AttributeModifier("title", new Model<String>(getString("portal.design.fun.monitorinfo.title"))));
        populateLink.add(new AttributeModifier("title", new Model<String>(getString("portal.design.fun.monitorinfo.title"))));
        processAdmin.add(populateLink);
        
        BookmarkablePageLink<Void> populateLinkUser = new BookmarkablePageLink<Void>("populateLinkUser", PopulateDatasPage.class);
	//  populateLink.add(new Image("populateLinkImg", new PackageResourceReference(LocateResources.class,"process_info_icon.png")));
	    populateLinkUser.add(new CacheActivatedImage("image.populateLinkImgUser", new ResourceModel("process_info_icon").getObject()));
	    populateLinkUser.add(new AttributeModifier("title", new Model<String>(getString("portal.design.fun.monitorinfo.title"))));
	    populateLinkUser.add(new AttributeModifier("title", new Model<String>(getString("portal.design.fun.monitorinfo.title"))));
	    processUser.add(populateLinkUser);
    	
    }

	  

	private ExternalLink getHypericExternalLink() {
		String hypericPortalUrl = hypericBean.getServerURL();
        ExternalLink hypericPortalLink = new ExternalLink("hyperic-portal-url", hypericPortalUrl);
        hypericPortalLink.add(new CacheActivatedImage("image.monitoring_info_icon", getString("monitoring_info_icon")));
        hypericPortalLink.add(new AttributeModifier("title", new Model<String>(getString("portal.design.fun.monitorinfo.title"))));
        hypericPortalLink.add(new AttributeModifier("alt", new Model<String>(getString("portal.design.fun.monitorinfo.title"))));
		return hypericPortalLink;
	}


	private ExternalLink getSplunkExternalLink() {
		String splunkServerUrl = splunkBean.getServerURL();
        ExternalLink externalLink = new ExternalLink("splunk-access-url", splunkServerUrl);
        externalLink.add(new CacheActivatedImage("image.splunk_info_icon", getString("splunk_info_icon")));
        externalLink.add(new AttributeModifier("title", new Model<String>(getString("portal.design.fun.logsinfo.title"))));
        externalLink.add(new AttributeModifier("alt", new Model<String>(getString("portal.design.fun.logsinfo.alt"))));
		return externalLink;
	}


	public FeedbackPanel getFeedback() {
        return this.feedback;
    }

}
