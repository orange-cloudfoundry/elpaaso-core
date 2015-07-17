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
package com.francetelecom.clara.cloud.commons;

import com.francetelecom.clara.cloud.commons.error.ErrorCode;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * InfrastructureException
 *
 * source used : http://northconcepts.com/blog/2013/01/18/6-tips-to-improve-your-exception-handling/
 *
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
public class InfrastructureException extends TechnicalException {
	
	public static final String ERROR_CODE = "errorCode";
	public static final String REASON_TEXT = "reasonText";

    public static InfrastructureException wrap(Throwable exception, ErrorCode errorCode) {
        if (exception instanceof InfrastructureException) {
            InfrastructureException se = (InfrastructureException)exception;
            if (errorCode != null && errorCode != se.getErrorCode()) {
                return new InfrastructureException(exception.getMessage(), exception, errorCode);
            }
            return se;
        } else {
            return new InfrastructureException(exception.getMessage(), exception, errorCode);
        }
    }

    public static InfrastructureException wrap(Throwable exception) {
        return wrap(exception, null);
    }

    private ErrorCode errorCode;
    private final Map<String,Object> properties = new TreeMap<String,Object>();

    public InfrastructureException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public InfrastructureException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public InfrastructureException(Throwable cause, ErrorCode errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public InfrastructureException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public InfrastructureException setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T)properties.get(name);
    }

    public InfrastructureException set(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    public void printStackTrace(PrintStream s) {
        synchronized (s) {
            printStackTrace(new PrintWriter(s));
        }
    }

    public void printStackTrace(PrintWriter s) {
        synchronized (s) {
            s.println(this);
            s.println("\t-------------------------------");
            if (errorCode != null) {
                s.println("\t[" + errorCode.getClass().getSimpleName() + "]" + errorCode + ":");
            }
            for (String key : properties.keySet()) {
                s.println("\t\t" + key + "=[" + properties.get(key) + "]");
            }
            s.println("\t-------------------------------");
            StackTraceElement[] trace = getStackTrace();
            for (int i=0; i < trace.length; i++)
                s.println("\tat " + trace[i]);

            Throwable ourCause = getCause();
            if (ourCause != null) {
                ourCause.printStackTrace(s);
            }
            s.flush();
        }
    }

}
