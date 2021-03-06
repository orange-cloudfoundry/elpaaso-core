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
/**
 * 
 */
package com.francetelecom.clara.cloud.commons;

/**
 * @author poyt7496
 * 
 */
public interface Constants {

	// this "empty" maven reference is used when the maven reference given to a com.francetelecom.clara.cloud.model.Server
	// must be ignored by the USS appliance manager.
	MavenReference mvnEmptyRef = new MavenReference("com.francetelecom.clara.cloud.catalog.product", "empty", "0");

	MavenReference mvnRefApache2_2_15 = new MavenReference("com.francetelecom.clara.cloud.catalog.product", "Apache Tar", "2.2.15-G02R01C01", "tar.gz");
	MavenReference mvnRefApache2_2_15_nativeRpm = new MavenReference("com.francetelecom.clara.cloud.catalog.product", "apache-httpd", "2.2.15","rpm");

	/** @deprecated old Mysql version, should not be use anymore **/
	@Deprecated
	MavenReference mvnRefMysql = new MavenReference("com.francetelecom.clara.cloud.catalog.product", "cloud-products-software-mysql", "5.0.0-SNAPSHOT",
			"tar.gz");
	
	MavenReference mvnRefJonas5_2_2_nativeRpm = new MavenReference("com.francetelecom.clara.cloud.catalog.product", "Jonas Full", "5.2.2", "rpm");
	MavenReference mvnRefJonas5_2_4 = new MavenReference("com.francetelecom.clara.cloud.catalog.product", "Jonas Tar", "5.2.4-G02R02C03", "tar.gz");
	

	MavenReference mvnRefMysql5521_G09R00C01 = new MavenReference("com.francetelecom.clara.cloud.catalog.product", "cloud-products-software-mysql",
			"5.5.21-G09R00C01", "tar");
	MavenReference mvnRefPgsql913_G01R00C02 = new MavenReference("com.francetelecom.clara.cloud.catalog.product", "cloud-products-software-postgresql",
			"9.1.3-G01R00C02", "tar");


}
