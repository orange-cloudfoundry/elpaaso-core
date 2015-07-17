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
package com.francetelecom.clara.cloud.coremodel;

/**
 * Roles definition (Paas User to an ApplicationRelease)
 * @author apog7416
 *
 */
public enum PaasRoleEnum {
    RELEASE_MANAGER("release manager"),
    ARCHITECT("architect"),
    DEVELOPER("developper"),
    ROLE_ADMIN("role_admin"),
    ROLE_ADMIN_AWS("role_admin_aws"),
    ROLE_USER("role_user"),
    USER("user");

    String name;

    PaasRoleEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
