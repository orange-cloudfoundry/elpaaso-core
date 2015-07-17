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

public interface RetryPolicy {

	/**
	 * Setter for retry attempts.
	 * 
	 * @param retryAttempts
	 *            the number of attempts before a retry becomes impossible.
	 */
	public abstract void setMaxAttempts(int retryAttempts);

	/**
	 * The maximum number of retry attempts before failure.
	 * 
	 * @return the maximum number of attempts
	 */
	public abstract int getMaxAttempts();

	/**
	 * Test for retryable operation based on the status.
	 * 
	 * @return true if the number of attempts so far is less than the limit.
	 */
	public abstract boolean canRetry(int retryCount);

}