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
package com.francetelecom.clara.cloud.presentation.resource;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;


public class CacheActivatedImage extends Image {

	private static final long serialVersionUID = 8657713524095681482L;


	public CacheActivatedImage(String id, IModel<?> model) {
		super(id, model);
		// TODO Auto-generated constructor stub
	}

	public CacheActivatedImage(String id, IResource imageResource) {
		super(id, imageResource);
		// TODO Auto-generated constructor stub
	}

	public CacheActivatedImage(String id, ResourceReference resourceReference, PageParameters resourceParameters) {
		super(id, resourceReference, resourceParameters);
		// TODO Auto-generated constructor stub
	}

	public CacheActivatedImage(String id, ResourceReference resourceReference) {
		super(id, resourceReference);
		// TODO Auto-generated constructor stub
	}

	public CacheActivatedImage(String id, String imageKey) {
		super(id, new PackageResourceReference(LocateResources.class, imageKey));
		// TODO Auto-generated constructor stub
	}

	public CacheActivatedImage(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	protected boolean shouldAddAntiCacheParameter() {		
		return false;
	}
	
}
