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
package com.francetelecom.clara.cloud.techmodel.cf;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.SimpleUserProvidedService;
import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;


public class AppTest {

    MavenReference appBinaries = new MavenReference("com.francetelecom.clara.prototype.springoojpa", "springoojpa-ear", "6.1.0", "ear");
    App app = new App(new TechnicalDeployment("name"), new Space(new TechnicalDeployment("name")), appBinaries, "myAppName");

    @Test
    public void hasDefaultNullBuildPackUrl() {
        assertThat(app.getBuildPackUrl()).isNull();
    }

    @Test
    public void requiresValidAppName() {
        //See constructor
        assertThat(app.getAppName()).isEqualTo("myAppName");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullAppName() {
        new App(new TechnicalDeployment("name"), new Space(new TechnicalDeployment("name")), appBinaries, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyAppName() {
        new App(new TechnicalDeployment("name"), new Space(new TechnicalDeployment("name")), appBinaries, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullAppBinaries() {
        new App(new TechnicalDeployment("name"), new Space(new TechnicalDeployment("name")), null, "myAppName");
    }

    @Test
    public void requiresMavenReference() {
        //See constructor
        assertThat(app.getAppBinaries()).isEqualTo(appBinaries);
    }

    @Test
    public void hasDefaultEmptyRoutes() {
        assertThat(app.getRoutes()).isEmpty();
    }

    @Test
    public void hasDefaultMemoryUsage() {
        assertThat(app.getRamMb()).isEqualTo(256);
    }

    @Test
    public void hasDefaultDiskUsage() {
        assertThat(app.getDiskSizeMb()).isEqualTo(1024);
    }

    @Test
    public void hasDefaultInstanceCount() {
        assertThat(app.getInstanceCount()).isEqualTo(1);
    }

    @Test
    public void constructorsSupportsOptionalValues() {
        String buildPackUrl = "https://github.com/Orange-OpenSource/vcap-java-client.git";
        app = new App(new TechnicalDeployment("name"), new Space(new TechnicalDeployment("name")), "myappName", appBinaries, buildPackUrl, 256, 2);

        assertThat(app.getInstanceCount()).isEqualTo(2);
        assertThat(app.getBuildPackUrl()).isEqualTo(buildPackUrl);
    }

    @Test
    public void supports_bind_services() {
        //given
        TechnicalDeployment td = new TechnicalDeployment("");
        SimpleUserProvidedService userProvidedService = new SimpleUserProvidedService("frontend-db", "http://localhost", td, new Space(td));

        //when
        app.bindService(userProvidedService);

        //then
        assertThat(app.getServiceNames()).containsOnly("frontend-db");
    }

    @Test
    public void delegates_dependency_resolver_to_service() {
        //given
        TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        App joyn = new App(td, space, appBinaries, "myAppName");
        SimpleUserProvidedService userProvidedService = new SimpleUserProvidedService("frontend-db", "http://localhost", td, space);
        joyn.bindService(userProvidedService);

        //when
        Set<DependantModelItem> dependantModelItems = joyn.listDepedencies();

        //then
        int aSpaceWithAnOrgAndACups=3;
        assertThat(dependantModelItems.size()).isEqualTo(aSpaceWithAnOrgAndACups);

    }

    @Test(expected = IllegalArgumentException.class)
    public void require_external_id_on_activation() throws Exception {
        MavenReference mavenReference = MavenReference.fromGavString("foo.groupid:foo.artifactid:foo.version");
        Space space = new Space(new TechnicalDeployment("name"));
        App joyn = new App(new TechnicalDeployment("name"), space, mavenReference, "joyn");

        joyn.activate(null);

    }

    @Test
    public void should_set_external_id_on_activation() throws Exception {
        MavenReference mavenReference = MavenReference.fromGavString("foo.groupid:foo.artifactid:foo.version");
        Space space = new Space(new TechnicalDeployment("name"));
        App joyn = new App(new TechnicalDeployment("name"), space, mavenReference, "joyn");

        UUID externalId = UUID.randomUUID();

        joyn.activate(externalId);

        Assertions.assertThat(joyn.getExternalReference()).isEqualTo(externalId.toString());
    }

    @Test
    public void should_be_activated_after_activation() throws Exception {
        MavenReference mavenReference = MavenReference.fromGavString("foo.groupid:foo.artifactid:foo.version");
        Space space = new Space(new TechnicalDeployment("name"));
        App joyn = new App(new TechnicalDeployment("name"), space, mavenReference, "joyn");

        UUID externalId = UUID.randomUUID();

        joyn.activate(externalId);

        Assertions.assertThat(joyn.isActivated()).isTrue();
    }

    @Test
    public void accepts_env_variables() {
        app.setEnvVariable("key1", "value1");

        assertThat(app.getEnvVarValue("key1")).isEqualTo("value1");
    }

    @Test
    public void env_vars_have_single_value_for_a_same_key() {
        //given
        app.setEnvVariable("key1", "value1");
        app.setEnvVariable("key1", "value2");

        //when
        assertThat(app.getEnvVarValue("key1")).isEqualTo("value2");
        assertThat(app.listEnvVariables()).hasSize(1);
    }

    @Test
    public void supports_fetching_env_var_by_keys() {
        //given
        app.setEnvVariable("key1", "value1");
        app.setEnvVariable("key2", "value2");

        //when
        assertThat(app.getEnvVarValue("key1")).isEqualTo("value1");
        assertThat(app.getEnvVarValue("no.such.key")).isNull();
    }

    @Test
    public void should_be_removed_after_deletion() throws Exception {
        MavenReference mavenReference = MavenReference.fromGavString("foo.groupid:foo.artifactid:foo.version");
        Space space = new Space(new TechnicalDeployment("name"));
        App joyn = new App(new TechnicalDeployment("name"), space, mavenReference, "joyn");

        UUID externalId = UUID.randomUUID();

        joyn.delete();

        Assertions.assertThat(joyn.isRemoved()).isTrue();
    }

    @Test
    public void should_be_stopped_after_stop() throws Exception {
        MavenReference mavenReference = MavenReference.fromGavString("foo.groupid:foo.artifactid:foo.version");
        Space space = new Space(new TechnicalDeployment("name"));
        App joyn = new App(new TechnicalDeployment("name"), space, mavenReference, "joyn");

        joyn.stop();

        Assertions.assertThat(joyn.isStopped()).isTrue();
    }

}
