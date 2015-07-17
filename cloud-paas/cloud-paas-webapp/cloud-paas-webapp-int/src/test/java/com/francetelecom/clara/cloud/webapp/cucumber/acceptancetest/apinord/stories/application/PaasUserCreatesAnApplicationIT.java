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
package com.francetelecom.clara.cloud.webapp.cucumber.acceptancetest.apinord.stories.application;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.springframework.beans.factory.annotation.Value;


@RunWith(Cucumber.class)
@CucumberOptions(format = { "html:target/cucumber-api-application-html-report", "json:target/cucumber-api-application-json-report.json" }, glue = "com.francetelecom.clara.cloud.webapp.cucumber.acceptancetest.apinord.stories.application")
public class PaasUserCreatesAnApplicationIT {
}