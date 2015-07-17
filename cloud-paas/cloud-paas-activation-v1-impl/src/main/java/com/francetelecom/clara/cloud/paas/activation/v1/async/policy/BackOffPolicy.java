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
package com.francetelecom.clara.cloud.paas.activation.v1.async.policy;

import com.francetelecom.clara.cloud.paas.activation.v1.async.RetryContext;

/**
 * Strategy interface to control back off between attempts in a single retry
 * operation.
 * 
 */
public interface BackOffPolicy {

	/**
	 * Give the next scheduled delivery date in an implementation-specific
	 * fashion.
	 */
	public long getNextSheduleDate(RetryContext context);

}