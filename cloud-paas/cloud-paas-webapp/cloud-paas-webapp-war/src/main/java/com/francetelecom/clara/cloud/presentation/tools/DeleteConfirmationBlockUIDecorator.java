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

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 14/12/11
 * Time: 11:23
 * To change this template use File | Settings | File Templates.
 */
public class DeleteConfirmationBlockUIDecorator extends BlockUIDecorator {

    private String confirmMessage;
    private static boolean forceOK = DeleteConfirmationUtils.forceOK;
    private static boolean forceCancel = DeleteConfirmationUtils.forceCancel;

	/**
	 * Creates a {@link DeleteConfirmationBlockUIDecorator}. The
	 * <code>confirmMessage</code> is shown in a validation window to confirm
	 * the action. The given <code>blockMessage</code> is shown in a block when
	 * the ajax call is performed. Both of these messages are escaped to avoid
	 * javascript errors as explained in {@link IAjaxCallDecorator}
	 * 
	 * @param confirmMessage
	 *            The confirmation message for validating the action.
	 * @param blockMessage
	 *            The message to be shown when the action is performed.
	 */
    public DeleteConfirmationBlockUIDecorator(String confirmMessage, String blockMessage) {
        super(blockMessage);
        this.confirmMessage = confirmMessage.replace("\"", "'").replace("'", "\\'");
    }

    @Override
    public CharSequence getPrecondition(Component component) {
        if (forceOK) {
            return "true";
        } else if (forceCancel) {
            return "false";
        }
        return "return confirm('" + confirmMessage + "')";
    }

}
