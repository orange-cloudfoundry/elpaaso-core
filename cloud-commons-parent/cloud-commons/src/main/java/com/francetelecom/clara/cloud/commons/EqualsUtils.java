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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A utility class to share utilities related to equals() and hashCode() implementation, mainly in logical model and technical model.
 * 
 * @author skwg9735
 */
public class EqualsUtils {

	/**
	 * Utility method to merge two list of excluded fields.
	 */
	public static String[] mergeExcludedFieldLists(String[] superClassExcludes, String[] localClassExcludes) {
		List<String> localExcludes = Arrays.asList(localClassExcludes);
		List<String> superExcludes = Arrays.asList(superClassExcludes);
		List<String> mergedExcludes = new ArrayList<String>(localExcludes);
		mergedExcludes.addAll(superExcludes);
		
		return mergedExcludes.toArray(new String[] {});
	}

}
