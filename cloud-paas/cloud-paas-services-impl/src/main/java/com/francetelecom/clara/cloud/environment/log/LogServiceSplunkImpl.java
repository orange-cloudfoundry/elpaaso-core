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
package com.francetelecom.clara.cloud.environment.log;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class LogServiceSplunkImpl implements LogService {

	private BaseSearchURL baseSearchURL;

	@Autowired
	public LogServiceSplunkImpl(BaseSearchURL baseSearchURL) {
		Assert.notNull(baseSearchURL,"Unable to get logs url. Base search url has not been provided. Check paas configuration to ensure that required settings have been provided to build logs url");
		this.baseSearchURL = baseSearchURL;
	}

	@Override
	public URL getAppLogsUrl(String appName) {
		return new SplunkQueryBuilder(baseSearchURL).index("*").source("tcp:12345").appName(appName).onlyAppLog().build();
	}

	@Override
	public URL getRouteLogsUrl(String routeName) {
		return new SplunkQueryBuilder(baseSearchURL).index("*").source("tcp:12345").routeName(routeName).onlyRouteLog().build();
	}

	@Override
	public URL getEnvironmentLogsUrl(String... appName) {
		SplunkQueryBuilder queryBuilder = new SplunkQueryBuilder(baseSearchURL).index("*").source("tcp:12345");
		
		for (Iterator<String> iterator = Arrays.asList(appName).iterator(); iterator.hasNext();) {
			queryBuilder.appName((String) iterator.next());
			if (iterator.hasNext()) {
				queryBuilder.or();
			}
		}
		return queryBuilder.build();
	}

}
