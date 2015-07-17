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
package com.francetelecom.clara.cloud.presentation.tools;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.slf4j.LoggerFactory;

public class AOPLogger {

	// Cette méthode est appelée à chaque fois (et avant)
	// qu'une méthode du package ew.service est interceptée
	public void logMethodEntry(JoinPoint joinPoint) {

		Object[] args = joinPoint.getArgs();

		// Nom de la méthode interceptée
		String name = joinPoint.getSignature().toLongString();
		StringBuffer sb = new StringBuffer(name + " called with: [");
		
		// Liste des valeurs des arguments reçus par la méthode
		for (int i = 0; i < args.length; i++) {
			Object o = args[i];
			sb.append("'" + o + "'");
			sb.append((i == args.length - 1) ? "" : ", ");
		}
		sb.append("]");

		LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringType()).info(sb.toString());
	}

	// Cette méthode est appelée à chaque fois (et après) qu'une méthode du
	// package ew.service est interceptée
	// Elle reçoit en argument 'result' qui est le retour de la méthode
	// interceptée
	public void logMethodExit(StaticPart staticPart, Object result) {

		// Nom de la méthode interceptée
		String name = staticPart.getSignature().toLongString();

		LoggerFactory.getLogger(staticPart.getSignature().getDeclaringType()).info(name + " returning: [" + result + "]");
	}

}
