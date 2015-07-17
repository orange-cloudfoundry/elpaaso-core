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
package com.francetelecom.clara.cloud.activation.plugin.cf.domain;

import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.techmodel.cf.App;

import java.util.UUID;

/**
 * handle cloud foundry app activation
 *
 */
public interface AppActivationService {

	/**
     * Activate app.
     * @param app
     *            the app
     * @return external app id
     */
    public UUID activate(App app);

	/**
     * Start app.
     * Will not start app if app is already started.
     * @param app
     *            the app
     * @return a task status indicating task progress
	 */
	public TaskStatus start(App app);

	/**
     * Stop app.
     * Will not stop app if app is already stopped.
     * @param app
     *            the app
     */
    public void stop(App app);

	/**
     * Delete app. if bindings exist, removes those bindings.
     * Will not attempt to delete app if app is missing.
     * @param app
     *            the app
     */
    public void delete(App app);

	/**
	 * @return app status
	 */
	public TaskStatus getAppStatus(TaskStatus taskStatus);

}
