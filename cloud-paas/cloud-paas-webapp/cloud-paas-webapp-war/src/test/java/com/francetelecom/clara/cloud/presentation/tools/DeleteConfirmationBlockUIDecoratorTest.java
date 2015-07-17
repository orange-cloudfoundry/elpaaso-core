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

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class DeleteConfirmationBlockUIDecoratorTest {
	
	@Before
	public void setUp() {
		// Restore these static fields to default in case they were changed by some other tests
		DeleteConfirmationUtils.forceCancel = false;
		DeleteConfirmationUtils.forceOK = false;
	}
	
	@Test
	public void test_apostrophes_are_escaped_from_the_messages() {
		String confirmMessage = "You don't want to do it, are you ?";
		String blockMessage = "It's being done...";
		DeleteConfirmationBlockUIDecorator decorator = new DeleteConfirmationBlockUIDecorator(confirmMessage, blockMessage);
		String beforeSend = decorator.getBeforeSendHandler(null).toString();
		String confirmationMessage = decorator.getPrecondition(null).toString();
		assertTrue("Apostrophes must be escaped in script: " + confirmationMessage, confirmationMessage.contains("You don\\'t want to do it, are you ?"));
		assertTrue("Apostrophes must be escaped in script: " + beforeSend, beforeSend.contains("It\\'s being done..."));
	}
	
	@Test
	public void test_quotes_are_replaced_in_the_messages() {
		String confirmMessage = "Are you really \"ready\" ?";
		String blockMessage = "Operation is being \"done\"...";
		DeleteConfirmationBlockUIDecorator decorator = new DeleteConfirmationBlockUIDecorator(confirmMessage, blockMessage);
		String beforeSend = decorator.getBeforeSendHandler(null).toString();
		String confirmationMessage = decorator.getPrecondition(null).toString();
		assertTrue("Quotes must be replaced by apostrophes and escaped in script: " + confirmationMessage, confirmationMessage.contains("Are you really \\'ready\\' ?"));
		assertTrue("Quotes must be replaced by apostrophes and escaped in script: " + beforeSend, beforeSend.contains("Operation is being \\'done\\'..."));
	}

}
