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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class CodeMirrorTextArea<T> extends TextArea<T> {

    private static final long serialVersionUID = -3005705037397085288L;
    
    private boolean readOnly;

    public CodeMirrorTextArea(final String id, boolean readOnly) {
        super(id);
        setOutputMarkupId(true);
        this.readOnly = readOnly;
    }

    public CodeMirrorTextArea(final String id, final IModel<T> iModel, boolean readOnly) {
        super(id, iModel);
        setOutputMarkupId(true);
        this.readOnly = readOnly;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/lib/codemirror.js")));
        response.render(CssHeaderItem.forReference(new WebjarsCssResourceReference("codemirror/current/lib/codemirror.css")));
        response.render(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("codemirror/current/mode/properties/properties.js")));
        response.render(OnLoadHeaderItem.forScript(
                  "var cm = CodeMirror.fromTextArea(document.getElementById('" + getMarkupId() + "'), { readOnly: " + readOnly + " });"
                + "cm.on('change',function(cm){"
                +     "document.getElementById('" + getMarkupId() + "').value = cm.getValue();"
                + "});"));
    }

    @Override
    public void onEvent(IEvent<?> event) {
        super.onEvent(event);
        if (event.getPayload() instanceof CodeMirrorRefresh) {
            CodeMirrorRefresh refresh = (CodeMirrorRefresh) event.getPayload();
            // Fix rendering of CodeMirror in pop up: force a refresh
            // http://stackoverflow.com/a/8353537
            // http://wisercoder.com/get-reference-codemirror-instance/
            refresh.getTarget().appendJavaScript(
                      "window.setTimeout(function(){"
                    +     "$('.CodeMirror')[0].CodeMirror.refresh();"
                    + "}, 0);");
        }
    }

    /**
     * An event payload that represents a CodeMirror refresh
     */
    public static class CodeMirrorRefresh {

        private final AjaxRequestTarget target;

        public CodeMirrorRefresh(AjaxRequestTarget target) {
            this.target = target;
        }

        public AjaxRequestTarget getTarget() {
            return target;
        }
    }
}
