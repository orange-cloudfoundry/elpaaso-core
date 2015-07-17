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
package com.francetelecom.clara.cloud.commons.testconfigurations;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class PropertiesHelper {
	
	public static Set<String> loadKeys(String propertiesFileName, InputStream in) throws IOException {
		Set<String> keys = new HashSet<String>();
		Properties prop = new Properties();
		if (in == null)
			throw new IOException("unable to get resource file " + propertiesFileName);
		prop.load(in);
		in.close();
		for (Object key : prop.keySet()) {
			keys.add((String) key);
		}
		return keys;
	}
	
}
