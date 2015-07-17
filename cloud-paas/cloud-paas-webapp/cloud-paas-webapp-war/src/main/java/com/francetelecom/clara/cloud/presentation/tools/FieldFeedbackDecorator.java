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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;


/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 13/10/11
 */
public class FieldFeedbackDecorator extends Behavior { // implements IAjaxRegionMarkupIdProvider {
    private static final long serialVersionUID = -4303340421879866940L;

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(FieldFeedbackDecorator.class.getName());

    public void bind(Component component) {
        component.setOutputMarkupId(true);
    }

    public void beforeRender(Component component) {
        FormComponent<?> fc = (FormComponent<?>) component;
        Response r = component.getResponse();

        String label = (fc.getLabel() != null) ? fc.getLabel().getObject(): null;
        if (label != null) {
            r.write("<span class=\"param\">");
            r.write("<label for=\"");
            r.write(fc.getMarkupId());
            r.write("\"");
            if (!fc.isValid()) {
                r.write(" class=\"error\"");
            }
            r.write(" />");
            r.write(Strings.escapeMarkup(label));
            r.write("</label>");
            r.write("</span>");

            NotNull clazz;

            try {
                Field field = fc.getForm().getModelObject().getClass().getDeclaredField(fc.getInputName());
                clazz = field.getAnnotation(NotNull.class);
            } catch (NoSuchFieldException e) {
                clazz = null;
            }

            if (clazz != null || fc.isRequired()) {
                r.write("<span class=\"required\" title=\"");
                r.write(fc.getString("portal.error.required.field.title"));
                r.write("\">");
                r.write(fc.getString("portal.required.field")+"</span>");
            } else {
                r.write("<span class=\"notrequired\"></span>");
            }
            r.write("<span class=\"value\">");

        }
        super.beforeRender(component);
    }

    @Override
    public void afterRender(Component component) {
        FormComponent<?> fc = (FormComponent<?>) component;
        Response r = component.getResponse();

        r.write("</span>");

        if (fc.hasFeedbackMessage()) {
            r.write("<span class=\"feedbackPanelTextField\">");

            IFeedbackMessageFilter filter = new ComponentFeedbackMessageFilter(component);

            for (FeedbackMessage message : fc.getFeedbackMessages().messages(filter)) {
                r.write("<span class=\"feedbackPanel");
                r.write(message.getLevelAsString().toUpperCase());
                r.write("\">");
                r.write(Strings.escapeMarkup(message.getMessage().toString()));
            }

            r.write("</span>");
        }
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        FormComponent<?> fc = (FormComponent<?>) component;
        if (!fc.isValid()) {
            String c1 = tag.getAttribute("class");
            if (c1 == null) {
                tag.put("class", "errorField");
            } else {
                tag.put("class", " errorField " + c1);
            }
        }
    }



}
