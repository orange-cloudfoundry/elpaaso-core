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
package com.francetelecom.clara.cloud.paas.activation.v1.async;

import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;

/**
 * Callback interface for two way (request-reply) asynchronous communication.
 * Uses polling pattern to fetch the reply.
 * 
 * @param <R>
 *            request sent to Receiver
 */
public interface TaskHandlerCallback<R> {

	/**
	 * Handle "request" part of our asynchronous communication. Used to run a
	 * Receiver operation whose logic is asynchronous oriented (once the
	 * Receiver operation process is started, it immediately returns a
	 * TaskStatus that can used to fetch the reply later on).
	 * 
	 * @param request
	 *            data to be sent to the Receiver
	 * @return
	 */
	public TaskStatus handleRequest(R request);

	/**
	 * Used to poll the Receiver so as to check whether a process is completed
	 * or not.
	 * 
	 * @param taskStatus
	 *            to identify the Receiver process
	 * 
	 * @return new taskStatus
	 */
	public TaskStatus onTaskPolled(TaskStatus taskStatus);

	/**
	 * Handle 'reply' part of our asynchronous communication when no exception
	 * occurred on the Receiver.
	 * 
	 * @param taskStatus
	 *            to identify the Receiver process
	 * @param communicationId
	 *            asynchronous communication id
	 * @throws Exception
	 */
	public void onTaskComplete(TaskStatus taskStatus, String communicationId);

	/**
	 * Handle "reply" part of our asynchronous communication when an exception
	 * occurred on the Receiver.
	 * 
	 * @param throwable
	 * @param communicationId
	 *            asynchronous communication id
	 */
	public void onFailure(Throwable throwable, String communicationId);
}
