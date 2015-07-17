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
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

/**
 * Decorator which allows to block user actions when the action
 * on the component is triggered (e.g.: IndicatingAjaxButton)
 *
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 01/12/11
 * Time: 10:29
 * To change this template use File | Settings | File Templates.
 */
public class BlockUIDecorator implements IAjaxCallListener, IComponentAwareHeaderContributor {

    /**
     * Message to be displayed while the user is waiting
     */
    private String message;

	/**
	 * Creates a {@link BlockUIDecorator}. The given <code>message</code> is
	 * shown in a block when the ajax call is performed. This message is escaped
	 * to avoid javascript errors as explained in {@link IAjaxCallDecorator}
	 * 
	 * @param message
	 *            The message to be shown when the action is performed.
	 */
    public BlockUIDecorator(String message) {
        super();
        setMessage(message);
    }

    public void setMessage(String message) {
        this.message = message.replace("\"", "'").replace("'", "\\'");
    }

    @Override
    public CharSequence getBeforeSendHandler(Component component) {
        return "$.blockUI({" +
                "message : '" + message + "'," +
                "css: {padding:'10px'}" +
                "});";
    }

    @Override
    public CharSequence getCompleteHandler(Component component) {
        return "$.unblockUI();";
    }

	@Override
    public CharSequence getPrecondition(Component component) {
	    return null;
    }

	@Override
    public CharSequence getBeforeHandler(Component component) {
	    return null;
    }

	@Override
    public CharSequence getAfterHandler(Component component) {
	    return null;
    }

	@Override
    public CharSequence getSuccessHandler(Component component) {
	    return null;
    }

	@Override
    public CharSequence getFailureHandler(Component component) {
	    return null;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("jquery-blockui/current/jquery.blockUI.js"), "blockui-js"));
    }
}
