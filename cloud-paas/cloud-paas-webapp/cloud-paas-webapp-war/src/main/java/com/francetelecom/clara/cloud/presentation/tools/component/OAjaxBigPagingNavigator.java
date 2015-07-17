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
package com.francetelecom.clara.cloud.presentation.tools.component;

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.request.resource.CssResourceReference;

public class OAjaxBigPagingNavigator extends AjaxPagingNavigator {

	private static final long serialVersionUID = -982684696679617954L;

	/**
	 * Constructor.
	 * @param id See Component
	 * @param pageable The pageable component the page links are referring to.
	 */
	public OAjaxBigPagingNavigator(String id, IPageable pageable) {
		super(id, pageable);
	}

	/**
	 * Constructor.
	 * @param id See Component
	 * @param pageable The pageable component the page links are referring to.
	 * @param labelProvider The label provider for the link text.
	 */
	public OAjaxBigPagingNavigator(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
		super(id, pageable, labelProvider);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssReferenceHeaderItem.forReference(new CssResourceReference(OAjaxBigPagingNavigator.class, "OBigPagingNavigator-adapted.css")));
	}
	
	
}
