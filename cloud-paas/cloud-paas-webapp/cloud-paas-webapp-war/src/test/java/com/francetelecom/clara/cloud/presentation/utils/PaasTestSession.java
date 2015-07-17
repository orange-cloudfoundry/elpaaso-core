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
package com.francetelecom.clara.cloud.presentation.utils;

import java.util.Locale;

import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

import com.francetelecom.clara.cloud.presentation.tools.WicketSession;

public class PaasTestSession extends WicketSession {

    private boolean useAuthentication = true;

    public PaasTestSession(Request request, Boolean useAuthentication) {
        super(request);
        this.useAuthentication = useAuthentication;
    }

    @Override
    public Roles getRoles() {
        if (useAuthentication) {
            return super.getRoles();
        } else {
        	if (getPaasUser() != null) {
         		return new Roles(getPaasUser().getPaasUserRole().name());

        	}
        	else {
        		return new Roles("ROLE_USER");
        	}
        }
    }

    @Override
    public Locale getLocale() {
        return new Locale("EN");
    }
}
