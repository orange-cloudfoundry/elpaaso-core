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

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.tester.Result;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WicketTester use by ElPaaso Wicket test suite.
 * 
 * This tester allow test developers to reference a component from a part of global path instead of absolute path.
 * 	ex : tester.getComponentFromLastRenderedPage("id")
 * 
 * This mecanism does'nt work with FormTester, you will need to specify all relative path to act on component form
 * 	ex : formTester.select("relative:path:to:component:id",true);
 * 
 * User: wwnl9733
 * Date: 13/01/12
 * Time: 10:16
 * To change this template use File | Settings | File Templates.
 */
public class PaasWicketTester extends WicketTester {

    private static final Logger logger = LoggerFactory.getLogger(PaasWicketTester.class.getName());

    public PaasWicketTester(WebApplication webApplication) {
        super(webApplication);
    }

    public PaasTestApplication getPaasApplication() {
        return (PaasTestApplication) this.getApplication();
    }
    
    @Override
	public Component getComponentFromLastRenderedPage(final String path) {
		// first check if we can find the component with the specified path - if not, check if its an abbreviated path
		String fullPath = lookupPath(getLastRenderedPage(), path);
		if (fullPath == null)
			return null;

		return super.getComponentFromLastRenderedPage(fullPath);
	}
	
	/* this method is overriden because in it's native version it doesn't use getComponentFromLastRenderedPage */
	@Override
	public Result isVisible(String path) {
		String fullPath = lookupPath(getLastRenderedPage(), path);
		if (fullPath == null)
			return null;

		return super.isVisible(fullPath);
	}

	/* Lookup for a Component from its id or its full path */
	public String lookupPath(final MarkupContainer markupContainer, final String path) {
		// try to look it up directly
		if (markupContainer.get(path) != null)
			return path;

		// if that fails, traverse the component hierarchy looking for it
		final List<Component> candidates = new ArrayList<Component>();
		markupContainer.visitChildren(new IVisitor<Component, List<Component>>() {
			Set<Component> visited = new HashSet<Component>();
			
			@Override
			public void component(Component c, IVisit<List<Component>> visit) {
				if (!visited.contains(c)) {
					visited.add(c);

					if (c.getId().equals(path)){
						candidates.add(c);
					}else{
						if( c.getPath().endsWith(path)){
							candidates.add(c);
						}
					}
				}
			}
		});
		// if its unambiguous, then return the full path
		if (candidates.isEmpty()) {
			fail("path: '" + path + "' not found for " +
					Classes.simpleName(markupContainer.getClass()));
			return null;
		} else 
		
		if (candidates.size() == 1) {
			String pathToContainer = markupContainer.getPath();
			String pathToComponent = candidates.get(0).getPath();
			return pathToComponent.replaceFirst(pathToContainer + ":", "");
		} else {
			String message = "path: '" + path + "' is ambiguous for " + Classes.simpleName(markupContainer.getClass()) + ". Possible candidates are: ";
			for (Component c : candidates) {
				message += "[" + c.getPath() + "]";
			}
			fail(message);
			return null;
		}
	}

    public Logger getLogger() {
        return logger;
    }

}
