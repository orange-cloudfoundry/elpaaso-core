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
package com.francetelecom.clara.cloud.paas.constraint;

/**
 * This class contains per application projection customization rules. 
 * They allow to capture specifics about an application to adapt projection to those specifics. 
 */
public class ApplicationCustomizationRule {

	// Per app- Architectural constants
	public int memoryMoPerDbConnection = 10; 
	public int memoryKoPerSession = 3000; 
	public int minJvmMemoryMb = 128;
	
	public int getMemoryMoPerDbConnection() {
		return memoryMoPerDbConnection;
	}
	public void setMemoryMoPerDbConnection(int memoryMoPerDbConnection) {
		this.memoryMoPerDbConnection = memoryMoPerDbConnection;
	}
	public int getMemoryKoPerSession() {
		return memoryKoPerSession;
	}
	public void setMemoryKoPerSession(int memoryKoPerSession) {
		this.memoryKoPerSession = memoryKoPerSession;
	}
	public int getMinJvmMemoryMb() {
		return minJvmMemoryMb;
	}
	public void setMinJvmMemoryMb(int minJvmMemoryMb) {
		this.minJvmMemoryMb = minJvmMemoryMb;
	}
	/**
	 * return a clone of the object
	 */
	public ApplicationCustomizationRule clone() {
		// TODO: use a more generic framework for cloning
		ApplicationCustomizationRule clone = new ApplicationCustomizationRule();
		clone.memoryKoPerSession = this.memoryKoPerSession;
		clone.memoryMoPerDbConnection = this.memoryMoPerDbConnection;
		return clone;
	}
}
