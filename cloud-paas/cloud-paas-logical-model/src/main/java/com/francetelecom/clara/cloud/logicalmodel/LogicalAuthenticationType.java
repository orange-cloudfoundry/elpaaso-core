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
/**
 * 
 */
package com.francetelecom.clara.cloud.logicalmodel;

/**
 * LogicalAuthenticationType
 * Describes the types of authentication supported by the system.
 *
 * This typically applies to SOAP web services (see {@link LogicalSoapConsumer})
 *
 * Last updated : $LastChangedDate$
 * Last author  : $Author$
 * @version     : $Revision$
 */
public enum LogicalAuthenticationType {

    /**
     * No authentication is requested
     */
	NONE,
    /**
     * Authentication through HTTP 1.1 Basic Authentication, see http://www.ietf.org/rfc/rfc2617.txt
     */
	BASIC_AUTH,

    /**
     * the client authentication is performed through specific authen (typically login/password) through custom HTTP headers
     */
	CUSTOM_AUTH_HEADER,

    /**
     * The client is authenticated with SSL-based certificates exchanges. See http://en.wikipedia.org/wiki/Transport_Layer_Security#Client-authenticated_TLS_handshake
     */
	SSL_CLIENT
}
