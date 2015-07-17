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
package com.francetelecom.clara.cloud.commons.tasks;

/**
 * Interface implemented by clara-cloud asynchronous components (they offer
 * startUp method returning a Task, and respond to poll request on this
 * interface)
 */
public interface PollTaskStateInterface<T extends TaskStatus> {

	/**
	 * Return a new task status that represent the current state of the given
	 * task.
	 * 
	 * @param taskStatus The task status to update
	 * @return A new task status
	 */
	T giveCurrentTaskStatus(T taskStatus);
}
