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

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.core.service.exception.*;
import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalModelNotConsistentException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

public class BusinessExceptionHandler {

	Component component;
	
	enum MessageLevel { ERROR, INFO };
	
	public BusinessExceptionHandler(Component c) {
		this.component = c;
	}

	public void error(BusinessException e) {
		process(MessageLevel.ERROR, e);
	}
	public void info(BusinessException e) {
		process(MessageLevel.INFO, e);
	}

	
	private void process(MessageLevel level, BusinessException e) {

		if(e instanceof LogicalModelNotConsistentException) {
			processLogicalModelNotConsistentException(level, (LogicalModelNotConsistentException) e);
		} else if (e instanceof InvalidConfigServiceException) {
			processInvalidConfigServiceException(level, (InvalidConfigServiceException) e);
		} else if (e instanceof InvalidMavenReferenceException) {
			processInvalidMavenReferenceException(level, (InvalidMavenReferenceException) e);
        } else if (e instanceof ApplicationNotFoundException) {
            processApplicationNotFoundException(level, (ApplicationNotFoundException) e);
        } else if (e instanceof ApplicationReleaseNotFoundException) {
            processApplicationReleaseNotFoundException(level, (ApplicationReleaseNotFoundException) e);
        } else if (e instanceof EnvironmentNotFoundException) {
            processEnvironmentNotFoundException(level, (EnvironmentNotFoundException) e);
        } else if (e instanceof PaasUserNotFoundException) {
            processPaasUserNotFoundException(level, (PaasUserNotFoundException) e);
        } else if (e instanceof ObjectNotFoundException) {
            processObjectNotFoundException(level, (ObjectNotFoundException) e);
        } else {
			processUnknownBusinessException(level, e);
		}
		
	}

	private void processInvalidMavenReferenceException(MessageLevel level, InvalidMavenReferenceException e) {
		String message = "";
		String [] parameters = new String[1];
		parameters[0] = e.getMavenReference().toGavString();

		switch(e.getType()) {
		case ARTIFACT_NOT_FOUND:
			message = component.getString("portal.logicaldeployment.artifactnotfound", new Model<String[]>(parameters));
			break;
		case UNKNOWN:
		default:
			message = component.getString("portal.logicaldeployment.invalidmavenref", new Model<String[]>(parameters));
			break;
		}
		
		addMessage(level, message);				
	}


	private void processInvalidConfigServiceException(MessageLevel level, InvalidConfigServiceException e) {
        String msg = "";
        List<String> args = new ArrayList<String>();
        args.add(e.getImpactedElementName());
        switch (e.getType()) {
            case UNKNOWN:
                msg = component.getString("portal.designer.config.summary.error.unknown.jee", new Model<String[]>(args.toArray(new String[1])));
                break;
            case TOO_LONG:
                args.add(String.valueOf(e.getMaxLength()));
                msg = component.getString("portal.designer.config.summary.error.tooLong.jee", new Model<String[]>(args.toArray(new String[2])));
                break;
            case TOO_MANY_ENTRIES:
                args.add(String.valueOf(e.getEntryCount()));
                args.add(String.valueOf(e.getMaxEntryCount()));
                msg = component.getString("portal.designer.config.summary.error.tooManyEntries.jee", new Model<String[]>(args.toArray(new String[3])));
                break;
            case DUPLICATE_KEYS:
                args.add(e.getDuplicateKeys().toString());
                msg = component.getString("portal.designer.config.summary.error.duplicateKeys.jee", new Model<String[]>(args.toArray(new String[2])));
                break;
            case SYNTAX_ERROR:
                msg = component.getString("portal.designer.config.summary.error.syntaxError.jee", new Model<String[]>(args.toArray(new String[1])));
                break;
            default:
                msg = component.getString("portal.designer.config.summary.error.unknown");
                break;
        }
		addMessage(level, msg);	
		
	}


	private void processLogicalModelNotConsistentException(MessageLevel level, LogicalModelNotConsistentException e) {
		for(BusinessException error:e.getErrors())
			process(level,error);
	}

    private void processApplicationNotFoundException(MessageLevel level, ApplicationNotFoundException e) {
        String msg = component.getString("portal.application.notfound");
        addMessage(level, msg);
    }

    private void processApplicationReleaseNotFoundException(MessageLevel level, ApplicationReleaseNotFoundException e) {
        String msg = component.getString("portal.release.notfound");
        addMessage(level, msg);
    }

    private void processEnvironmentNotFoundException(MessageLevel level, EnvironmentNotFoundException e) {
        String msg = component.getString("portal.environment.error.notfound");
        addMessage(level, msg);
    }

    private void processPaasUserNotFoundException(MessageLevel level, PaasUserNotFoundException e) {
        String msg = component.getString("portal.user.error.notfound", new Model<String[]>(new String[]{e.getMissingUserId().getValue()}));
        addMessage(level, msg);
    }

    private void processObjectNotFoundException(MessageLevel level, ObjectNotFoundException e) {
        String msg = component.getString("portal.error.notfound");
        addMessage(level, msg);
    }

	private void processUnknownBusinessException(MessageLevel level, BusinessException e) {
		String [] parameters = new String[1];
		parameters[0] = e.getMessage();
		String message = component.getString("portal.error.business", new Model<String[]>(parameters));
		addMessage(level, message);		
	}


	private void addMessage(MessageLevel level, String message) {
		switch (level) {
		case ERROR:
			component.error(message);
			break;

		case INFO:
			component.info(message);
			break;

		default:
			component.error(message);
		}
		
	}

    public static void addError(Component feedbackPanel, BusinessException e) {
        BusinessExceptionHandler handler = new BusinessExceptionHandler(feedbackPanel);
        handler.error(e);
    }

    public static void addError(AjaxRequestTarget target, Component feedbackPanel, BusinessException e) {
        addError(feedbackPanel, e);
        target.add(feedbackPanel);
    }

}
