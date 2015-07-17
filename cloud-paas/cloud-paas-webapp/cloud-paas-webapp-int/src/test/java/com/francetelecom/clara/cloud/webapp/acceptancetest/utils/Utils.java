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
package com.francetelecom.clara.cloud.webapp.acceptancetest.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {
	public final static boolean ENABLE_SSL_CERTIFICATE_CHECK = false;
	public final static boolean DISABLE_SSL_CERTIFICATE_CHECK = true;

	/**
	 * asserts that a given url is reachable; ie : HttpStatus is 200
	 * 
	 * @param url
	 *            url to reach
	 * @return true if url is reachable; ie : HttpStatus is 200
	 */
	public static boolean canReachURL(String url) {
		HttpURLConnection urlConnection;
		try {
			urlConnection = (HttpURLConnection) new URL(url).openConnection();
			System.err.println(url + urlConnection.getResponseCode());
			return urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK;
		} catch (IOException e) {
			throw new RuntimeException("Given URL " + url + " cannot be reached.", e);
		}
	}

	public static void disableSslCertificateCheck() {
			XTrustProvider.install();
	}

}
