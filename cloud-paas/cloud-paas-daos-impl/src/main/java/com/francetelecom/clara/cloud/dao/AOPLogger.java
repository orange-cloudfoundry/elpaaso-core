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
package com.francetelecom.clara.cloud.dao;

import org.aspectj.lang.JoinPoint;
import org.slf4j.LoggerFactory;

/**
 * AOLogger
 * <p>
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
public class AOPLogger {

    public static final String AOP_PREFIX = "#AOP#";

    private String lastLogMethodEntryResult = null;

    // Cette m�thode est appel�e � chaque fois (et avant)
    // qu'une m�thode du package ew.service est intercept�e
    public void logMethodEntry(JoinPoint joinPoint) {

        Object[] args = joinPoint.getArgs();

        // Nom de la m�thode intercept�e
        String name = joinPoint.getSignature().toShortString();
        StringBuilder sb = new StringBuilder(AOP_PREFIX + name + " called with: [");

        // Liste des valeurs des arguments re�us par la m�thode
        for (int i = 0; i < args.length; i++) {
            Object o = args[i];
            sb.append("'").append(o).append("'");
            sb.append((i == args.length - 1) ? "" : ", ");
        }
        sb.append("]");
        lastLogMethodEntryResult = sb.toString();
        // LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringType()).debug(sb.toString());
    }

    // Cette m�thode est appel�e � chaque fois (et apr�s) qu'une m�thode du
    // package ew.service est intercept�e
    // Elle re�oit en argument 'result' qui est le retour de la m�thode
    // intercept�e
    public void logMethodExit(JoinPoint.StaticPart staticPart, Object result) {

        // Nom de la m�thode intercept�e
        // String name = staticPart.getSignature().toShortString();

        // WARN : do not return a "result.toString" which could inject some bug
        String resultRepresentation = "(null)";
        if (result != null && result.getClass() != null) {
            resultRepresentation = result.getClass().getSimpleName();
        }

        LoggerFactory.getLogger(staticPart.getSignature().getDeclaringType()).trace(lastLogMethodEntryResult + " returning: [" + resultRepresentation + "]");
    }

}