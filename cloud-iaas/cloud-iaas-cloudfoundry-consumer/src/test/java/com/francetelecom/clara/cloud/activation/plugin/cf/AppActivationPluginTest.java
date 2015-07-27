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
package com.francetelecom.clara.cloud.activation.plugin.cf;

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.AppActivationService;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.tasks.Failure;
import com.francetelecom.clara.cloud.commons.tasks.Success;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.model.ModelItemRepository;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.francetelecom.clara.cloud.techmodel.cf.*;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.SimpleUserProvidedService;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class AppActivationPluginTest {

	private AppActivationPlugin plugin;

	@Mock
	AppActivationService appActivationService;
	@Mock
	MvnRepoDao mvnRepoDao;
	@Mock
	ModelItemRepository modelItemRepository;

	App joyn;
	
	@Mock
	Route route;
	
	@Mock
	MavenReference resolvedMavenReference;
	@Mock
	MavenReference unresolvedMavenReference;
    @Mock
    AppRepository appRepository;

	@Before
	public void setUp() {
        plugin = new AppActivationPlugin(appActivationService, modelItemRepository, mvnRepoDao, appRepository);

		TechnicalDeployment td = new TechnicalDeployment("depl");

		Space space = new Space(td);
		space.activate(SpaceName.randomSpaceNameWithSuffix("env1"));


		SimpleUserProvidedService joyndb = new SimpleUserProvidedService("postgres-joyndb", "postgres://user:password@hostname:1234/joyndb", td, space);

		joyn = new App(td, space, resolvedMavenReference, "joyn");
		joyn.bindService(joyndb);
		joyn.mapRoute(route);

	}

	@Test
	public void accepts_only_cf_subs() {
		assertThat(plugin.accept(App.class, ActivationStepEnum.ACTIVATE)).isTrue();
		assertThat(plugin.accept(ModelItemRepository.class, ActivationStepEnum.ACTIVATE)).isFalse();
	}

	@Test
	public void ignores_init_step() {
        assertThat(plugin.init(joyn)).isNotNull();
    }

    @Test
    public void fail_to_activate_app_if_app_does_not_exist() throws Exception {
        TaskStatus status = plugin.activate(1, App.class, new ActivationTestContext());

        Assertions.assertThat(status.hasFailed()).isEqualTo(true);
    }

    @Test
    public void fail_to_delete_app_if_app_does_not_exist() throws Exception {
        TaskStatus status = plugin.delete(1, App.class);

        Assertions.assertThat(status.hasFailed()).isEqualTo(true);
    }

    @Test
    public void fail_to_stop_app_if_app_does_not_exist() throws Exception {
        TaskStatus status = plugin.stop(1, App.class);

        Assertions.assertThat(status.hasFailed()).isEqualTo(true);
    }

    @Test
    public void activate_resolves_maven_url_and_delegates_to_consummer() throws Exception {
        // given
		joyn = Mockito.mock(App.class);
		when(mvnRepoDao.resolveUrl(any(MavenReference.class))).thenReturn(resolvedMavenReference);
		when(resolvedMavenReference.getAccessUrl()).thenReturn(new URL("http://nexus.com"));
		when(resolvedMavenReference.getAccessUrl()).thenReturn(new URL("http://nexus.com"));
		when(joyn.getAppBinaries()).thenReturn(unresolvedMavenReference);
        Mockito.when(appRepository.findOne(1)).thenReturn(joyn);
        Set<Route> routes = new HashSet<>();
        routes.add(route);
		when(joyn.getRoutes()).thenReturn(routes);

		// when
        plugin.activate(1, App.class, new ActivationTestContext());

		// then
		verify(mvnRepoDao).resolveUrl(any(MavenReference.class));

        verify(appActivationService).activate(any(App.class));
        // expansion
		verify(joyn).updateAppBinaries(any(MavenReference.class)); // maven url
																	// resolutions
	}

	@Test
    public void app_should_be_ACTIVATED_after_activation() throws Exception {
        // given
		when(mvnRepoDao.resolveUrl(any(MavenReference.class))).thenReturn(resolvedMavenReference);
		when(resolvedMavenReference.getAccessUrl()).thenReturn(new URL("http://nexus.com"));
		when(resolvedMavenReference.getAccessUrl()).thenReturn(new URL("http://nexus.com"));
        Mockito.when(appRepository.findOne(1)).thenReturn(joyn);

        Mockito.when(appActivationService.activate(any(App.class))).thenReturn(UUID.randomUUID());

		// when
        TaskStatus taskStatus = plugin.activate(1, App.class, new ActivationTestContext());

		// then
		assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.FINISHED_OK);
        assertThat(joyn.isActivated()).isTrue();
        verify(appActivationService).activate(any(App.class));
    }

    @Test
	public void should_fail_to_delete_when_app_activation_fails() {
        TechnicalDeployment td = new TechnicalDeployment("depl");
        Space space = new Space(td);
        space.activate(SpaceName.randomSpaceNameWithSuffix("env1"));
        App app = new App(td, space, resolvedMavenReference, "joyn");
        app.activate(UUID.randomUUID());
        Mockito.when(appRepository.findOne(1)).thenReturn(app);
        Mockito.doThrow(new TechnicalException("failed")).when(appActivationService).delete(any(App.class));

		// when
        TaskStatus taskStatus = plugin.delete(1, App.class);

		// then
		assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.FINISHED_FAILED);
	}

	@Test
    public void should_succeed_to_delete_when_app_activation_succeeds() {
        TechnicalDeployment td = new TechnicalDeployment("depl");

        Space space = new Space(td);
        space.activate(SpaceName.randomSpaceNameWithSuffix("env1"));
        App app = new App(td, space, resolvedMavenReference, "joyn");
        app.activate(UUID.randomUUID());
        Mockito.when(appRepository.findOne(1)).thenReturn(app);

        // when
        TaskStatus taskStatus = plugin.delete(1, App.class);

		// then
		assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.FINISHED_OK);
		verify(appActivationService).delete(any(App.class));
	}

	@Test
	public void should_delete_when_app_is_activated() {
		TechnicalDeployment td = new TechnicalDeployment("depl");

		Space space = new Space(td);
		space.activate(SpaceName.randomSpaceNameWithSuffix("env1"));
		App app = new App(td, space, resolvedMavenReference, "joyn");
		app.activate(UUID.randomUUID());
		Mockito.when(appRepository.findOne(1)).thenReturn(app);

		// when
		TaskStatus taskStatus = plugin.delete(1, App.class);

		// then
		verify(appActivationService).delete(any(App.class));
	}

	@Test
	public void should_delete_when_app_is_in_unknown_state() {
		TechnicalDeployment td = new TechnicalDeployment("depl");

		Space space = new Space(td);
		space.activate(SpaceName.randomSpaceNameWithSuffix("env1"));
		App app = new App(td, space, resolvedMavenReference, "joyn");
		app.failed();
		Mockito.when(appRepository.findOne(1)).thenReturn(app);

		// when
		TaskStatus taskStatus = plugin.delete(1, App.class);

		// then
		verify(appActivationService).delete(any(App.class));
	}

	@Test
	public void should_fail_to_stop_when_app_activation_fails() {
        Mockito.when(appRepository.findOne(1)).thenReturn(joyn);
        Mockito.doThrow(new TechnicalException("failed")).when(appActivationService).stop(any(App.class));

		// when
        TaskStatus taskStatus = plugin.stop(1, App.class);

		// then
		assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.FINISHED_FAILED);
		verify(appActivationService).stop(any(App.class));
	}

	@Test
    public void should_succeed_to_stop_when_app_activation_succeeds() {

        //Mockito.when(appActivationService.stop(any(App.class))).thenReturn(new Success());
        Mockito.when(appRepository.findOne(1)).thenReturn(joyn);


        // when
        TaskStatus taskStatus = plugin.stop(1, App.class);

		// then
		assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.FINISHED_OK);
		verify(appActivationService).stop(any(App.class));
	}

	@Test
	public void should_fail_to_start_when_app_activation_fails() {
        Mockito.when(appRepository.findOne(1)).thenReturn(joyn);
        Mockito.when(appActivationService.start(any(App.class))).thenReturn(new Failure("failed"));

		// when
        TaskStatus taskStatus = plugin.start(1, App.class);

		// then
		assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.FINISHED_FAILED);
		verify(appActivationService).start(any(App.class));
	}

	@Test
    public void should_succeed_to_start_when_app_activation_succeeds() {
        Mockito.when(appRepository.findOne(1)).thenReturn(joyn);
        Mockito.when(appActivationService.start(any(App.class))).thenReturn(new Success());

		// when
        TaskStatus taskStatus = plugin.start(1, App.class);

		// then
		assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.FINISHED_OK);
		verify(appActivationService).start(any(App.class));
	}

	@Test
	public void should_fail_to_firststart_when_app_activation_fails() {
        Mockito.when(appRepository.findOne(1)).thenReturn(joyn);
        Mockito.when(appActivationService.start(any(App.class))).thenReturn(new Failure("failed"));

		// when
        TaskStatus taskStatus = plugin.firststart(1, App.class);

		// then
		assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.FINISHED_FAILED);
		verify(appActivationService).start(any(App.class));
	}

	@Test
    public void should_succeed_to_firststart_when_app_activation_succeeds() {
        Mockito.when(appRepository.findOne(1)).thenReturn(joyn);
        Mockito.when(appActivationService.start(any(App.class))).thenReturn(new Success());

		// when
        TaskStatus taskStatus = plugin.firststart(1, App.class);

		// then
		assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.FINISHED_OK);
		verify(appActivationService).start(any(App.class));
	}

	/*
	 * @Test public void
	 * get_current_task_status_peeks_app_start_status_to_successful_completion()
	 * { //given when(cfApp1.getInstanceCount()).thenReturn(5);
	 * when(cfApp2.getInstanceCount()).thenReturn(5);
	 * 
	 * //when TaskStatus taskStatus = plugin.firststart(tdi, cfSubscription);
	 * 
	 * //then
	 * assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.STARTED);
	 * assertThat(taskStatus.listSubtasks()).hasSize(2);
	 * assertThat(taskStatus.getPercent()).isEqualTo(0);
	 * 
	 * //given when(cfAdapter.peekAppStartStatus(anyInt(),
	 * anyString())).thenReturn(1);
	 * 
	 * //when taskStatus = plugin.giveCurrentTaskStatus(taskStatus);
	 * 
	 * //then
	 * assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.STARTED);
	 * assertThat(taskStatus.getPercent()).isEqualTo(20); //one instance for
	 * each app = 2 instances out of 10=20%
	 * 
	 * //given when(cfAdapter.peekAppStartStatus(anyInt(),
	 * anyString())).thenReturn(5);
	 * 
	 * //when taskStatus = plugin.giveCurrentTaskStatus(taskStatus);
	 * 
	 * //then
	 * assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.FINISHED_OK
	 * ); assertThat(taskStatus.getPercent()).isEqualTo(100);
	 * 
	 * }
	 */

	/*
	 * @Test public void
	 * get_current_task_status_peeks_app_start_status_to_timeout() { //given
	 * when(cfApp1.getInstanceCount()).thenReturn(1);
	 * when(cfApp2.getInstanceCount()).thenReturn(1);
	 * 
	 * //when TaskStatus taskStatus = plugin.firststart(tdi, cfSubscription);
	 * 
	 * //then
	 * assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.STARTED);
	 * 
	 * //given when(service.getAppStatus(any(TaskStatus.class)).thenReturn(new
	 * Failure("failed")));
	 * 
	 * //when taskStatus = plugin.giveCurrentTaskStatus(taskStatus);
	 * 
	 * //then assertThat(taskStatus.getTaskStatus()).isEqualTo(TaskStatusEnum.
	 * FINISHED_FAILED); }
	 */

	@Test
	public void checking_java_int_conversions() {
		long elapsedMs = 10000000;
		int singleAppStartTimeoutS = 1000;
		assertThat(elapsedMs > 1000L * (long) singleAppStartTimeoutS).isTrue();
		assertThat(elapsedMs).isEqualTo(10000L * (long) singleAppStartTimeoutS);
	}
}
