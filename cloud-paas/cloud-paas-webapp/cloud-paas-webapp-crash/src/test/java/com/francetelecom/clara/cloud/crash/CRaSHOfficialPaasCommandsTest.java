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
package com.francetelecom.clara.cloud.crash;

import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.service.OpsService;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentOpsDetailsDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * test of the paas official crash commands
 */
public class CRaSHOfficialPaasCommandsTest extends AbstractSpringTestCase {
    protected static Logger LOG = LoggerFactory.getLogger(CRaSHOfficialPaasCommandsTest.class);

    private ManageEnvironment manageEnvironment;
    private OpsService opsService;

    @Override
    protected String getContextXmlFilename() {
        return "crash-manage-bean-mocked-context.xml";
    }

    @Before
    public void setUp() {
        manageEnvironment = (ManageEnvironment) context.getBean("manageEnvironment");
        opsService = (OpsService) context.getBean("opsService");
    }

    @Test
    public void should_provide_ops_official_command_help() {
        assertCommandOkAndOutputContains("help ops", true, "paas ops commands");
    }

    @Test
    public void should_provide_ops_info_official_command() throws IOException {
        // GIVEN
        String command = "ops info";
        doReturn("2013-06-07 11:45:43.624+0200").when(opsService).getServerDate();
        doReturn("1.6.0-SNAPSHOT").when(opsService).getBuildVersion();
        doReturn("07/06/2013-10:41").when(opsService).getBuildDate();
        doReturn("dwvd1206").when(opsService).getBuildUser();

        // WHEN/THEN
        verboseAssertCommandOkFromReference(command, "ops_info.ref");
    }

    @Test
    public void should_provide_ops_envs_official_command() throws IOException {
        // GIVEN
        String command = "ops envs";
        EnvironmentDto envA = createEnvMock("environmentA");
        EnvironmentDto envB = createEnvMockCreated("environmentB");
        List<EnvironmentDto> envs = Arrays.asList(envA, envB);
        doReturn(envs).when(manageEnvironment).findEnvironments(anyInt(), anyInt(), anyString(), anyString());

        // WHEN/THEN
        verboseAssertCommandOkFromReference(command, "ops_envs.ref");
    }


    @Test
    public void should_provide_ops_env_official_command() throws IOException, ObjectNotFoundException {
        // GIVEN
        String envUid = "1234";
        String command = "ops env " + envUid;
        EnvironmentOpsDetailsDto envOpsDetail = creanteEnvOpsDetailsMock(envUid);
        doReturn(envOpsDetail).when(manageEnvironment).findEnvironmentOpsDetailsByUID(envUid);

        // WHEN/THEN
        verboseAssertCommandOkFromReference(command, "ops_env.ref");
    }

    private EnvironmentOpsDetailsDto creanteEnvOpsDetailsMock(String envUid) throws MalformedURLException {
        EnvironmentOpsDetailsDto envOpsDetails = mock(EnvironmentOpsDetailsDto.class);
        doReturn("applicationA").when(envOpsDetails).getApplicationLabel();
        doReturn("releaseR").when(envOpsDetails).getReleaseVersion();
        doReturn(envUid).when(envOpsDetails).getUid();
        doReturn("devOpsWonderEnv").when(envOpsDetails).getLabel();
        Calendar calInstance = Calendar.getInstance();
        calInstance.set(2013, 06, 07, 15, 39, 00);
        doReturn(calInstance.getTime()).when(envOpsDetails).getCreationDate();
        doReturn("DonVitoCorleone").when(envOpsDetails).getOwnerName();
        doReturn("vite@patate.fr").when(envOpsDetails).getOwnerId();
        LinkDto overallsLinkDto = mock(LinkDto.class);
        doReturn(new URL("http://splunk.is.everywhere.org/query?withoutFilter=33452")).when(overallsLinkDto).getUrl();
        doReturn(overallsLinkDto).when(envOpsDetails).getEnvironmentOverallsLinkDto();
        EnvironmentOpsDetailsDto.VMAccessDto vmA = createEnvOpsDetailsVmMock("dev-vmAsample");
        EnvironmentOpsDetailsDto.VMAccessDto vmB = createEnvOpsDetailsVmMock("dev-vmBsample");
        doReturn(Arrays.asList(vmA, vmB)).when(envOpsDetails).listVMAccesses();
        LinkDto link = mock(LinkDto.class);
        doReturn(new URL("http://remote.iaas.fr:8080/my-app/portal/")).when(link).getUrl();
        doReturn(LinkDto.LinkTypeEnum.ACCESS_LINK).when(link).getLinkType();
        doReturn(Arrays.asList(link)).when(envOpsDetails).getSpecificLinkDto(any(LinkDto.LinkTypeEnum.class));
        return envOpsDetails;
    }

    private EnvironmentOpsDetailsDto.VMAccessDto createEnvOpsDetailsVmMock(String vmHostname) {
        EnvironmentOpsDetailsDto.VMAccessDto vmA = mock(EnvironmentOpsDetailsDto.VMAccessDto.class);
        doReturn("1.2.3.4").when(vmA).getIp();
        doReturn("root").when(vmA).getUser();
        doReturn("root").when(vmA).getPassword();
        doReturn("11114345232").when(vmA).getIaasId();
        doReturn(vmHostname).when(vmA).getHostname();
        return vmA;
    }

    private EnvironmentDto createEnvMock(String envLabel) {
        Calendar calInstance = Calendar.getInstance();
        calInstance.set(2013, 06, 07, 11, 23, 00);
        EnvironmentDto env = mock(EnvironmentDto.class);
        doReturn(envLabel).when(env).getLabel();
        doReturn(calInstance.getTime()).when(env).getCreationDate();
        doReturn("PaasCommandsTest").when(env).getOwnerName();
        doReturn(EnvironmentDto.EnvironmentStatusEnum.CREATING).when(env).getStatus();
        doReturn("This process is running now...").when(env).getStatusMessage();
        doReturn("f37e6b6f-cad4-4d4b-8256-1bddbaa12e62").when(env).getUid();
        doReturn(23).when(env).getStatusPercent();
        return env;
    }
    private EnvironmentDto createEnvMockCreated(String envLabel) {
        Calendar calInstance = Calendar.getInstance();
        calInstance.set(2013, 06, 07, 11, 32, 00);
        EnvironmentDto env = mock(EnvironmentDto.class);
        doReturn(envLabel).when(env).getLabel();
        doReturn(calInstance.getTime()).when(env).getCreationDate();
        doReturn("PaasCommandsTestBis").when(env).getOwnerName();
        doReturn(EnvironmentDto.EnvironmentStatusEnum.CREATED).when(env).getStatus();
        doReturn(null).when(env).getStatusMessage();
        doReturn("ca3ae8b1-ad01-40a3-a472-89d13364bfb6").when(env).getUid();
        doReturn(100).when(env).getStatusPercent();
        doReturn("my wuderfull test").when(env).getComment();
        return env;
    }


    // "paas listenvs -running"
}
