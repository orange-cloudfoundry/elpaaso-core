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

/**
 * Created by sbortolussi on 02/06/2015.
 */
public class ServiceActivationStatus extends TaskStatus {

    private String spaceName;
    private String serviceName;

    private ServiceActivationStatus(String serviceName, String spaceName) {
        this.serviceName = serviceName;
        this.spaceName = spaceName;
    }


    public static ServiceActivationStatusBuilder ofService(String serviceName, String spaceName) {
        return new ServiceActivationStatusBuilder(serviceName, spaceName);
    }

    public String getSpaceName() {
        return spaceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public static final class ServiceActivationStatusBuilder {

        private String serviceName;
        private String spaceName;

        public ServiceActivationStatusBuilder(String serviceName, String spaceName) {
            this.serviceName = serviceName;
            this.spaceName = spaceName;
        }

        public ServiceActivationStatus isPending(String description) {
            final ServiceActivationStatus status = new ServiceActivationStatus(serviceName, spaceName);
            status.setAsStarted(description);
            return status;
        }

        public ServiceActivationStatus hasFailed(String description) {
            final ServiceActivationStatus status = new ServiceActivationStatus(serviceName, spaceName);
            status.setAsFinishedFailed(description);
            return status;
        }

        public ServiceActivationStatus hasSucceeded() {
            final ServiceActivationStatus status = new ServiceActivationStatus(serviceName, spaceName);
            status.setAsFinishedOk();
            return status;
        }
    }
}
