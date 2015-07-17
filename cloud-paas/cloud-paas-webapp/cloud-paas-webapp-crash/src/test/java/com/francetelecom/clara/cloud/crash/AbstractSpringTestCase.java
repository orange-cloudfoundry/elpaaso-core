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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.crsh.BaseProcessContext;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellResponse;
import org.crsh.ssh.SSHPlugin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.UrlResource;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDaoTestUtils;
import com.francetelecom.clara.cloud.scalability.ManageScalability;

public abstract class AbstractSpringTestCase {
    private static final String REFERENCE_DIRECTORY = "src/test/resources/ref/";
    public static final int CRASH_CONSOLE_WIDTH = 180;
    protected static Logger LOG = LoggerFactory.getLogger(AbstractSpringTestCase.class);
    private Shell shell;


    protected GenericApplicationContext context;

    protected abstract String getContextXmlFilename();

    @Before
    public void setupCRaSHUsingSpring() throws Exception {

        String contextXmlFilename = getContextXmlFilename();
        URL xml = AbstractSpringTestCase.class.getResource(contextXmlFilename);
        Assert.assertNotNull(xml);

        //
        context = new GenericXmlApplicationContext(new UrlResource(xml));
        context.start();

        //
        PaasSpringWebBootstrap bootstrap = context.getBean(PaasSpringWebBootstrap.class);

        // Test a bit
        ShellFactory factory = bootstrap.getContext().getPlugin(ShellFactory.class);
        this.shell = factory.create(null);
        Assert.assertNotNull(shell);

        // Mock MvnRepoDao.resolveUrl()
        // @link
        // http://elpaaso_shp/index.php/HowTo_mock_MvnRepoDao
        MvnRepoDao mvnRepoDaoMock = (MvnRepoDao) context.getBean("mvnDao");
        MvnRepoDaoTestUtils.mockResolveUrlDoNothing(mvnRepoDaoMock);
    }

	@After
	public void stopSshdAfterTest() {
		PaasSpringWebBootstrap bootstrap = context.getBean(PaasSpringWebBootstrap.class);
		SSHPlugin sshPlugin = bootstrap.getContext().getPlugin(SSHPlugin.class);
		sshPlugin.destroy();

	}

    protected void populateEnv() throws BusinessException {
        ManageScalability manageScalability= context.getBean(ManageScalability.class);
        manageScalability.populate("NNNDDDSSGGG", "testLogLinks", 1, 1, 1);
    }

    protected BaseProcessContext assertCommandOk(String command) {
        ShellProcess process = shell.createProcess(command);
        Assert.assertNotNull(process);
        BaseProcessContext baseProcessContext = BaseProcessContext.create(process);
        baseProcessContext.setWidth(CRASH_CONSOLE_WIDTH);
        BaseProcessContext pc = baseProcessContext.execute();
        Assert.assertTrue(pc.getResponse() instanceof ShellResponse.Ok);
        return pc;
    }

    protected BaseProcessContext assertCommandOk(String command, String expectedOutput) {
        BaseProcessContext pc = assertCommandOk(command);
        String r = pc.getOutput();
        Assert.assertEquals(expectedOutput, r);
        return pc;
    }

    protected BaseProcessContext assertCommandOkFromReference(String command, String referenceFilename) throws IOException {
        String filePath = REFERENCE_DIRECTORY+referenceFilename;
        String expectedContent = FileUtils.readFileToString(new File(filePath));

        return assertCommandOk(command, expectedContent);
    }

    protected BaseProcessContext verboseAssertCommandOkFromReference(String command, String referenceFilename) throws IOException {
        BaseProcessContext pc = assertCommandOkFromReference(command, referenceFilename);
        displayCommandOutput(command, pc);
        return pc;
    }

    protected BaseProcessContext verboseAssertCommandOkAndOutputContains(String command, boolean shouldContains, String expectedPartialOutput) {
        BaseProcessContext pc = assertCommandOkAndOutputContains(command, shouldContains, expectedPartialOutput);
        displayCommandOutput(command, pc);
        return pc;
    }

    void displayCommandOutput(String command, BaseProcessContext pc) {
        String r = pc.getOutput();
        String verboseMessage = "\n'" + command + "' output :\n"
                + "----------------\n"
                + r;
        LOG.info(verboseMessage);
    }

    protected BaseProcessContext assertCommandOkAndOutputContains(String command, boolean shouldContains, String expectedPartialOutput) {
        BaseProcessContext pc = assertCommandOk(command);
        String r = pc.getOutput();
        String assertionMessage = "\n'" + command + "' output :\n"
                                + "----------------\n"
                                + r + " \n\n"
                                + "should " + (shouldContains ? "" : " not ") + "contains :\n"
                                + expectedPartialOutput;
        Assert.assertTrue(assertionMessage, r.contains(expectedPartialOutput) == shouldContains);
        return pc;
    }


}
