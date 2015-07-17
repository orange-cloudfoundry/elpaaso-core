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
package com.francetelecom.clara.cloud.model.converters;

import java.net.MalformedURLException;
import java.net.URL;

import org.dozer.DozerConverter;

/**
 * Dozer converter for URL
 * @author apog7416
 *
 */
public class URLConverter extends DozerConverter<URL, URL> {

	public URLConverter() {
		super(URL.class, URL.class);
	}

	@Override
	public URL convertTo(URL source, URL destination) {
		URL target;
		if (source==null)
			return null;
		
		try {
			target = new URL(source.toString());
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Unknown value!");
		}
		return target;
	}

	@Override
	public URL convertFrom(URL source, URL destination) {
		URL target;
		if (source==null)
				return null;
		try {
			target = new URL(source.toString());
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Unknown value!");
		}
		return target;
	}

}
