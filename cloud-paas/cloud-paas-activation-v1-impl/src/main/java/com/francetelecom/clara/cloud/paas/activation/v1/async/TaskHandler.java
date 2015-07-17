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
 * Task handler interface for two way (request-reply) asynchronous
 * communication. Uses polling pattern to fetch the reply.
 * 
 * @param <R>
 *            request sent to Receiver
 * @param <C>
 *            callback
 */
public interface TaskHandler<R, C extends TaskHandlerCallback<R>> {

	/**
	 * Handle 'request' part of our asynchronous communication.
	 * 
	 * @param request
	 *            data to be sent to the Receiver
	 * @param communicationId
	 *            asynchronous communication id
	 * @return taskStatus
	 */
	public TaskStatus handleRequest(R request, String communicationId);

	/**
	 * Used to poll the Receiver so as to check whether a process is completed
	 * or not.
	 * 
	 * @param taskStatus
	 *            to identify the Receiver process
	 * @param communicationId
	 *            asynchronous communication id
	 */
	public void onTaskPolled(TaskStatus taskStatus, RetryContext retryContext,
			String communicationId);

	/**
	 * Handle 'reply' part of our asynchronous communication.
	 * 
	 * @param taskStatus
	 *            to identify the Receiver process
	 * @param communicationId
	 *            asynchronous communication id
	 */
	public void onTaskComplete(TaskStatus taskStatus, String communicationId);

	/**
	 * Handle 'reply' part of an asynchronous communication when an exception
	 * occurred on the Receiver.
	 * 
	 * @param throwable
	 * @param communicationId
	 *            asynchronous communication id
	 */
	public void onTaskFailure(Throwable throwable, String communicationId);
}
