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
package com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure;

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.AppActivationService;
import com.francetelecom.clara.cloud.activation.plugin.cf.domain.CfTaskStatus;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.tasks.Started;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AppActivationServiceDefaultImpl implements AppActivationService {

    private static Logger LOGGER = LoggerFactory.getLogger(AppActivationServiceDefaultImpl.class.getName());

    private CfAdapter cfAdapter;

    /**
     * in seconds
     */
    @Value("${cf.appStartTimeoutS:600}")
    private int appStartTimeout; // initial value to ease unit
    // tests. Default value
    // assigned in spring config

    @Autowired
    public AppActivationServiceDefaultImpl(CfAdapter cfAdapter) {
        super();
        this.cfAdapter = cfAdapter;
    }

    @Override
    public UUID activate(final App app) {
        return cfAdapter.createApp(app, app.getSpace().getValue());
    }

    @Override
    public TaskStatus start(final App app) {
        final TaskStatus status = new Started("starting cloud foundry app <" + app.getAppName() + ">");
        try {
            TaskStatus appSubStatus = new CfTaskStatus(System.currentTimeMillis(), app.getAppName(), app.getInstanceCount(), app.getSpace().getValue());
            status.addSubtask(appSubStatus);
            if (cfAdapter.isAppStarted(app.getAppName(), app.getSpace().getValue())) {
                LOGGER.warn("will not start app<" + app.getAppName() + ">. app is already started.");
                status.setAsFinishedOk();
            } else {
                cfAdapter.startApp(app, app.getSpace().getValue());
            }
        } catch (Exception e) {
            String message = "unable to start app<" + app + ">. " + e.getMessage();
            LOGGER.error(message, e);
            status.setAsFinishedFailed(message);
        }
        return status;
    }

    @Override
    public void stop(final App app) {
        if (cfAdapter.isAppStopped(app.getAppName(), app.getSpace().getValue())) {
            LOGGER.warn("will not stop app<" + app.getAppName() + ">. app is already stopped.");
        } else {
            cfAdapter.stopApp(app, app.getSpace().getValue());
        }
    }

    @Override
    public void delete(final App app) {
        if (!cfAdapter.appExists(app.getAppName(), app.getSpace().getValue())) {
            LOGGER.warn("will not delete app<" + app.getAppName() + ">. app is already deleted.");
        } else {
            cfAdapter.logAppDiagnostics(app.getAppName(), app.getSpace().getValue());
            cfAdapter.deleteApp(app, app.getSpace().getValue());
        }
    }

    @Override
    public TaskStatus getAppStatus(TaskStatus taskStatus) {
        List<TaskStatus> appSubTasks = taskStatus.listSubtasks();

        for (TaskStatus subTask : appSubTasks) {
            CfTaskStatus cfAppTaskStatus = (CfTaskStatus) subTask;
            String appName = cfAppTaskStatus.getAppName();
            int instanceCounts = cfAppTaskStatus.getInstanceCounts();
            String spaceName = cfAppTaskStatus.getSpaceName();
            try {
                boolean appStartedProperly;
                int nbPeeks = cfAppTaskStatus.incrNbPeeks();

                cfAppTaskStatus.setPercent(100 * nbStartedInstances(appName, instanceCounts, spaceName) / instanceCounts);
                appStartedProperly = (nbStartedInstances(appName, instanceCounts, spaceName) == instanceCounts);
                if (!appStartedProperly) {
                    long elapsed = System.currentTimeMillis() - cfAppTaskStatus.getStartTime();
                    if (elapsed > 1000L * (long) appStartTimeout) {
                        String msg = "timeout waiting for app " + appName + " to start: polled " + nbPeeks + " times and waited " + elapsed / 1000 + " s (max is:"
                                + appStartTimeout + " s)";
                        cfAdapter.logAppDiagnostics(appName, spaceName);
                        LOGGER.info(msg);
                        cfAppTaskStatus.setAsFinishedFailed(msg);
                    }
                }
                if (appStartedProperly) {
                    LOGGER.info("all " + instanceCounts + " instance(s) of " + appName + " have properly started");
                    cfAppTaskStatus.setAsFinishedOk();
                }
            } catch (Exception e) {
                throw new TechnicalException("unable to start app:" + appName + " caught:" + e, e);
            }
        }

        return new TaskStatus(taskStatus, new TaskStatus.SubTaskFactory() {
            @Override
            public TaskStatus createSubTask(TaskStatus subtask) {
                CfTaskStatus original = (CfTaskStatus) subtask;
                CfTaskStatus clone = new CfTaskStatus(original, original.getAppName(), original.getSpaceName(), original.getInstanceCounts(), original.getNbPeeks());
                return clone;
            }
        }); // convention to return a copy that will be serialied and stored
    }

    private int nbStartedInstances(String appName, int instanceCounts, String spaceName) {
        return cfAdapter.peekAppStartStatus(instanceCounts, appName, spaceName);
    }

    public void setAppStartTimeout(int appStartTimeout) {
        this.appStartTimeout = appStartTimeout;
    }

}
