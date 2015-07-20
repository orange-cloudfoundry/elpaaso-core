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
package com.francetelecom.clara.cloud.presentation.tools;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.WicketApplication;
import com.francetelecom.clara.cloud.presentation.common.Breadcrumbs;
import com.francetelecom.clara.cloud.presentation.common.NavigationMenuFirstLevel;
import com.francetelecom.clara.cloud.presentation.common.PageTemplate;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.coremodel.exception.InvalidApplicationException;
import com.francetelecom.clara.cloud.coremodel.exception.InvalidReleaseException;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.exception.DataException;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

import javax.validation.ConstraintViolationException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 04/11/11
 */

@MountPath("/populate")
@AuthorizeInstantiation("ROLE_ADMIN")
public class PopulateDatasPage extends PageTemplate {
   /**
     * Logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(PopulateDatasPage.class);
    private static final long serialVersionUID = 5136649670545875625L;
    @SpringBean(name="populateService")
    PopulateDatasService populateService;

    /**
     * PageTemplate constructor
     *
     * @param params - page parameters map
     */
    public PopulateDatasPage(final PageParameters params) {
        super(params);

        if (populateService == null) {
            logger.warn("populateService was null");
            populateService = new PopulateDatasService();
        }
        populateService.setUser(WicketSession.get().getPaasUser());
    }

    private void initComponents() {
        NavigationMenuFirstLevel navFirstLvl = new NavigationMenuFirstLevel();
        add(navFirstLvl);
        /* set head page title to display in browser title bar */
        add(new Label("head_page_title", getString("portal.design.web.title.populatedatas")));

        List<BreadcrumbsItem> breadcrumbsItems = new ArrayList<BreadcrumbsItem>();

        breadcrumbsItems.add(new BreadcrumbsItem(this.getClass(), "portal.design.breadcrumbs.populatedatas", null, true));

        Breadcrumbs breadcrumbs = new Breadcrumbs("breadcrumbs", breadcrumbsItems);
        add(breadcrumbs);

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        final CheckGroup<PopulateApplicationInformation> group = new CheckGroup<>("groupCheckBox",
                new ArrayList<PopulateApplicationInformation>());

        AppSelectionForm appSelectionForm = new AppSelectionForm("appSelectionForm", group);

        appSelectionForm.add(group);
        group.add(new CheckGroupSelector("groupselector"));

        ListView<PopulateApplicationInformation> appListView = new ApplicationListview();

        group.add(appListView);
        add(appSelectionForm);
    }

    @Override
    protected void onInitialize() {
        initComponents();
        setOutputMarkupId(true);
        super.onInitialize();
    }

    class AppSelectionForm extends Form<List<PopulateApplicationInformation>> {

        private static final long serialVersionUID = 7043467951744971241L;
        private final CheckGroup<PopulateApplicationInformation> group;

        AppSelectionForm(String id, CheckGroup<PopulateApplicationInformation> group) {
            super(id);
            this.group = group;
        }

        @Override
        protected void onSubmit() {
            super.onSubmit();
            logger.debug("onsubmit");
            Collection<PopulateApplicationInformation> SelectedAppsList = group.getModelObject();
            WebPage webPage = submitPopulate(SelectedAppsList);
            setResponsePage(webPage);
        }

        private WebPage submitPopulate(Collection<PopulateApplicationInformation> selectedAppsList) {
            WebPage webPage;

            int nbEnvironments = 0;
            for (PopulateApplicationInformation appInfo : selectedAppsList) {
                nbEnvironments += Integer.parseInt(appInfo.getNbOfEnvironments());
            }

            WebApplication webApp = WicketApplication.get();
            String param = webApp.getInitParameter("mockMode");
            boolean mock = Boolean.valueOf(param);

            webPage = this.getWebPage();
            if (!mock && nbEnvironments > 1) {
                webPage.error(getString("portal.populate.prod.nb.env.error", new Model<Object[]>(new Object[]{nbEnvironments})));

            } else {
                for (PopulateApplicationInformation appInfo : selectedAppsList) {
                    // TODO: enhance exception handler to handle all those exceptions
                    try {
                        populateService.populateSingleApp(appInfo.getAppName(), appInfo.getNbOfReleases(), appInfo.getNbOfEnvironments(), mock);
                            webPage.info(getString("portal.populate.success", getAppInfoModel(appInfo)));
                        webPage = new HomePage(getPageParameters());
                    } catch (ObjectNotFoundException e) {
                            webPage.error(getString("portal.populate.error.objectnotfound", getAppInfoModel(appInfo)));
                        logger.error(e.getMessage(), e);
                            logger.error(exceptionMessage(getString("portal.populate.error.objectnotfound", getAppInfoModel(appInfo))));
                    } catch (InvalidReleaseException e) {
                            webPage.error(getString("portal.populate.error.invalidrelease", getAppInfoModel(appInfo)));
                        logger.error(e.getMessage(), e);
                            logger.error(exceptionMessage(getString("portal.populate.error.invalidrelease", getAppInfoModel(appInfo))));
                    } catch (MalformedURLException e) {
                            webPage.error(getString("portal.populate.error.malformedurl", getAppInfoModel(appInfo)));
                        logger.error(e.getMessage(), e);
                            logger.error(exceptionMessage(getString("portal.populate.error.malformedurl", getAppInfoModel(appInfo))));
                    } catch (InvalidApplicationException e) {
                            webPage.error(getString("portal.populate.error.invalidapplication", getAppInfoModel(appInfo)));
                        logger.error(e.getMessage(), e);
                            logger.error(exceptionMessage(getString("portal.populate.error.invalidapplication", getAppInfoModel(appInfo))));
                    } catch (InvalidMavenReferenceException e) {
                            webPage.error(getString("portal.populate.error.invalidmavenref", getAppInfoModel(appInfo)));
                        logger.error(e.getMessage(), e);
                            logger.error(exceptionMessage(getString("portal.populate.error.invalidmavenref", getAppInfoModel(appInfo))));
                    } catch (DataException de) {
                        String excMsg = getString("portal.populate.error.dataexception", new Model<Object[]>(new Object[]{appInfo.getAppName(), appInfo.getNbOfReleases(), appInfo.getNbOfEnvironments(), de.getMessage()}));
                        webPage.error(excMsg);
                        logger.error(de.getMessage(), de);
                        logger.error(exceptionMessage(excMsg));
                    } catch (ConstraintViolationException constraintViolationException) {
                        String excMsg = getString("portal.populate.error.dataexception", new Model<Object[]>(new Object[]{appInfo.getAppName(), appInfo.getNbOfReleases(), appInfo.getNbOfEnvironments(), constraintViolationException.getMessage()}));
                        webPage.error(excMsg);
                        logger.error(constraintViolationException.getMessage(), constraintViolationException);
                        logger.error(exceptionMessage(excMsg));
                        } catch (DuplicateApplicationException e) {
                            // For all other exception use our exception handler to process exception
                        	// This block specifically catches DuplicateApplicatioError to log using WARN level
                            BusinessExceptionHandler handler = new BusinessExceptionHandler(webPage);
                            handler.error(e);
                            logger.warn(e.getMessage(), e);
                        }  catch (BusinessException e) {
                        // For all other exception use our exception handler to process exception
                        BusinessExceptionHandler handler = new BusinessExceptionHandler(webPage);
                        handler.error(e);
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            return webPage;
        }

			private Model<Object[]> getAppInfoModel(
					PopulateApplicationInformation appInfo) {
				return new Model<Object[]>(new Object[]{appInfo.getAppName(), appInfo.getNbOfReleases(), appInfo.getNbOfEnvironments()});
			}

    }
    
    private class ApplicationListview extends ListView<PopulateApplicationInformation> {

        private static final long serialVersionUID = 7043467951744971241L;

        public ApplicationListview() {
            super("appListView", PopulateDatasPage.this.populateService.getApplicationsList());
        }

        @Override
        protected void populateItem(ListItem<PopulateApplicationInformation> listItem) {
            Check<PopulateApplicationInformation> appCheckBox = new Check<>("appCheckBox", listItem.getModel());
            /* checkboxes must have distinct names so that automatic UI tests can find them
               if you change that line, please change the tests that use the populate page
             */
            appCheckBox.add(new AttributeModifier("id", new Model<String>(((PopulateApplicationInformation)listItem.getModelObject()).getAppName())));
           // appCheckBox.add(new AttributeAppender("name", new Model<String>(((PopulateApplicationInformation)listItem.getModelObject()).getAppLabel())," "));
            listItem.add(appCheckBox);
            listItem.add(new Label("appName", new PropertyModel(listItem.getModel(), "appName")));
            listItem.add(new TextField("nbOfReleases", new PropertyModel(listItem.getModel(), "nbOfReleases")).add(new AttributeModifier("class","small")));
            listItem.add(new TextField("nbOfEnvironments", new PropertyModel(listItem.getModel(), "nbOfEnvironments")).add(new AttributeModifier("class","small")));
        }

    }
}
