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
package com.francetelecom.clara.cloud.paas.activation;

import com.francetelecom.clara.cloud.application.ManageModelItem;
import com.francetelecom.clara.cloud.commons.tasks.PollTaskStateInterface;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.commons.xstream.XStreamUtils;
import com.francetelecom.clara.cloud.coremodel.ActivationContext;
import com.francetelecom.clara.cloud.model.ModelItem;
import com.francetelecom.clara.cloud.technicalservice.exception.NotFoundException;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class that defines the contract that the plugin providers need to meet
 * to plug into the activation process.
 * 
 * An ActivationPlugin class takes responsibility for the life-cycle by
 * inspecting the ModelItem in the accept() method. Then it is invoked during
 * the various life cycle steps.
 * 
 * Note that ActivationPlugin subclasses need to be stateless and thread-safe: a
 * single instance is called is concurrence for all process activations.
 * 
 * TODO: confirm persistence strategy: The persistent data such as subscriptions
 * may be stored in the TDI, or in the TaskStatus
 */
public abstract class ActivationPlugin<T extends ModelItem> implements PollTaskStateInterface<TaskStatus> {

	protected static Logger logger = LoggerFactory.getLogger(ActivationPlugin.class);

	private ManageModelItem manageModelItem;

    public TaskStatus init(int entityId, final Class<T> entityClass) throws NotFoundException {
        return init(
                (T) manageModelItem.findModelItem(entityId, entityClass));
    }

	/**
	 * Activate the TDI
	 * 
	 * @param entityId The JPA identifier of the model Entity associated with this plugin
     *                 (e.g. ApplicationServerInstance if the plugin responded true in the {@link #accept(Class, ActivationStepEnum)} method)
     * @param context
     *            A string that identify environment: it's dedicated to Xaas to
	 *            tag resources
	 * @return A TaskStatus
	 * @throws NotFoundException
	 *             If entity is not found
	 */
    public TaskStatus activate(int entityId, final Class<T> entityClass, ActivationContext context) throws NotFoundException {
        return activate(
                (T) manageModelItem.findModelItem(entityId, entityClass),
                context);
    }

    public TaskStatus firststart(int entityId, final Class<T> entityClass) throws NotFoundException {
        return firststart(
                (T) manageModelItem.findModelItem(entityId, entityClass));
    }

    public TaskStatus start(int entityId, final Class<T> entityClass) throws NotFoundException {
        return start(
                (T) manageModelItem.findModelItem(entityId, entityClass));
    }

    public TaskStatus stop(int entityId, final Class<T> entityClass) throws NotFoundException {
        return stop(
                (T) manageModelItem.findModelItem(entityId, entityClass));
    }

    /**
     * See {@link #delete(com.francetelecom.clara.cloud.model.ModelItem)}  }
     */
    public TaskStatus delete(int entityId, final Class<T> entityClass) throws NotFoundException {
        return delete(
                (T) manageModelItem.findModelItem(entityId, entityClass));
    }

	/**
	 * Called before creation. This task must be asynchronous and return fast.
	 * This task is used to check runtime dependencies
	 * 
	 * @param entity
	 *            The entity to init
	 * @return A TaskStatus to check task status
	 */
    public TaskStatus init(final T entity) {
        // This should not be called but it's possible
		logger.warn("This plugin method should not be called (" + this.getClass().getName() + "::init is missing)");
		TaskStatus status = new TaskStatus();
		status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
		status.setPercent(100);
		return status;
	}

	/**
	 * Called for resource creation (e.g. upon completion the resource is created but not started).
     * This task must be asynchronous and return fast.
	 * 
	 * @param entity
	 *            The entity to activate
     * @param context
     *            A string that identify environment: it's dedicated to Xaas to
	 *            tag resources
	 * @return A TaskStatus to check task status
	 */
    public TaskStatus activate(final T entity, ActivationContext context) {
        // This should not be called but it's possible
		logger.warn("This plugin method should not be called " + this.getClass().getName() + "::activate");
		TaskStatus status = new TaskStatus();
		status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
		status.setPercent(100);
		return status;
	}

	/**
	 * Called for first start so you can configure your entity and start it.
	 * This task must be asynchronous and return fast.
	 * 
	 * @param entity
	 *            The entity to start
	 * @return A TaskStatus to check task status
	 */
    public TaskStatus firststart(final T entity) {
        // This should not be called but it's possible
		logger.warn("This plugin method should not be called " + this.getClass().getName() + "::firststart");
		TaskStatus status = new TaskStatus();
		status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
		status.setPercent(100);
		return status;
	}

	/**
	 * Called for start so you must start your entity. This task must be
	 * asynchronous and return fast.
	 * 
	 * @param entity
	 *            The entity to start
	 * @return A TaskStatus to check task status
	 */
    public TaskStatus start(final T entity) {
        // This should not be called but it's possible
		logger.warn("This plugin method should not be called " + this.getClass().getName() + "::start");
		TaskStatus status = new TaskStatus();
		status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
		status.setPercent(100);
		return status;
	}

	/**
	 * Called for stop so you must stop your entity. This task must be
	 * asynchronous and return fast.
	 * 
	 * @param entity
	 *            The entity to stop
	 * @return A TaskStatus to check task status
	 */
    public TaskStatus stop(final T entity) {
        // This should not be called but it's possible
		logger.warn("This plugin method should not be called " + this.getClass().getName() + "::stop");
		TaskStatus status = new TaskStatus();
		status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
		status.setPercent(100);
		return status;
	}

	/**
	 * Called for deletion of a resource. This task must be asynchronous and return fast.
     *
     * Should also be idempotent:
     * <ol>
     *     <li>If the delete() request previously completed successfully and the next delete() requests should complete
     *     successfully (i.e. delete() should ignore request if entity(tdi) is in DELETED state)</li>
     *     <li>If the real-life resource is missing (e.g. no corresponding dbaas instance or cf space, because of an
     *     inconsistency between elpaaso view and real-life-view),
     *      the delete() request should complete successfully.</li>
     * </ol>
     *
     * Should however prevent resource leaks, and report a failure when a real-life resource exists which fails to be deleted:
     * <ol>
     *     <li>If the remote resource's broker is temporary unavailable, unreacheable, times-out, or returns a 500 error,
     *     this method should return a TaskStatus with a failure completion, potentially after rechecking the resource
     *     is still present,or retrying the delete.</li>
     * </ol>
     *
	 * 
	 * @param entity
	 *            The entity to delete
	 * @return A TaskStatus to check task status
     *
     */
    public TaskStatus delete(final T entity) {
        // This should not be called but it's possible
		logger.warn("This plugin method should not be called " + this.getClass().getName() + "::delete");
		TaskStatus status = new TaskStatus();
		status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
		status.setPercent(100);
		return status;
	}

	/**
	 * Does this plugin want to be called for this entity at this step?
	 * 
	 * @param entityClass
	 *            The entity
	 * @param step
	 *            The step
	 * @return true if this plugin wants to be called for this entity at this
	 *         step
	 */
	public abstract boolean accept(Class<?> entityClass, ActivationStepEnum step);

	public abstract TaskStatus giveCurrentTaskStatus(TaskStatus taskStatus);

	public ManageModelItem getManageModelItem() {
		return manageModelItem;
	}

	public void setManageModelItem(ManageModelItem manageModelItem) {
		this.manageModelItem = manageModelItem;
	}

    protected String dumpToXmlForTraces(ModelItem modelItem) {
        XStream xStream = XStreamUtils.instanciateXstreamForHibernate();
		return xStream.toXML(modelItem);
	}

}
