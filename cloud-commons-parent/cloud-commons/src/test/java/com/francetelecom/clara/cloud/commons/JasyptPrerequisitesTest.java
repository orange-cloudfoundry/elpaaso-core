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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Optional;
import java.util.Set;

public class JasyptPrerequisitesTest {

    @Test(expected = NullPointerException.class)
    public void should_fail_when_passPhraseEnvName_key_is_null() {
        new JasyptPrerequisites(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_fail_when_passPhraseEnvName_key_is_empty() {
        new JasyptPrerequisites("", "");
    }

    @Test(expected = TechnicalException.class)
    public void should_fail_when_passPhraseEnvName_and_no_jndi_property_are_defined() throws NamingException {
        SimpleNamingContextBuilder.emptyActivatedContextBuilder();

        new JasyptPrerequisites("PAAS_PHRASE", "jasypt.secret");
    }

    @Test(expected = TechnicalException.class)
    public void should_fail_when_passPhraseEnvName_property_is_not_defined_and_jndi_secret_is_empty() throws NamingException {
        SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        InitialContext context = new InitialContext();
        context.bind("jasypt.secret", "");

        new JasyptPrerequisites("PAAS_PHRASE", "jasypt.secret");
    }

    @Test
    public void should_pass_when_passPhraseEnvName_environment_property_is_defined() {
        final Set<String> envKeys = System.getenv().keySet();
        final Optional<String> firstKeyWithNotNullValue = envKeys.stream().filter(envKey -> System.getenv().get(envKey) != null && System.getenv().get(envKey) != "").findFirst();
        new JasyptPrerequisites(firstKeyWithNotNullValue.get(), "nimportequoi");
    }

    @Test
    public void should_pass_when_passPhraseEnvName_property_is_not_defined_but_a_jndi_secret_is_found() throws NamingException {
        SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        InitialContext context = new InitialContext();
        context.bind("jasypt.secret", "secret");

        new JasyptPrerequisites("PAAS_PHRASE", "jasypt.secret");
    }

}
