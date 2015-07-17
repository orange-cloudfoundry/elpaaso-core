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

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 14/12/11
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class DeleteConfirmationDecorator implements IAjaxCallListener {

    private String message;
    private static boolean forceOK = DeleteConfirmationUtils.forceOK;
    private static boolean forceCancel = DeleteConfirmationUtils.forceCancel;

	/**
	 * Creates a {@link DeleteConfirmationDecorator}. The <code>message</code>
	 * is shown in a validation window to confirm the action. This message is
	 * escaped to avoid javascript errors as explained in
	 * {@link IAjaxCallDecorator}
	 * 
	 * @param message
	 *            The confirmation message for validating the action.
	 */
    public DeleteConfirmationDecorator(String message) {
        super();
        setMessage(message);
    }

    public void setMessage(String message) {
        this.message = message.replace("\"", "'").replace("'", "\\'");
    }

    
    @Override
    public CharSequence getPrecondition(Component component) {
        if (forceOK) {
            return "true";
        } else if (forceCancel) {
            return "false";
        }
        return "return confirm('" + message + "')";
    }

	@Override
    public CharSequence getBeforeHandler(Component component) {
	    return null;
    }

	@Override
    public CharSequence getBeforeSendHandler(Component component) {
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
    public CharSequence getCompleteHandler(Component component) {
	    return null;
    }
}
