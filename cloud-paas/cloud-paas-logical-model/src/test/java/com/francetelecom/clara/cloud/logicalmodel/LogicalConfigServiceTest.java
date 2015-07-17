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
package com.francetelecom.clara.cloud.logicalmodel;

import com.francetelecom.clara.cloud.PersistenceTestUtil;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException.ErrorType;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.ConfigLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SpringooLogicalModelCatalog;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the LogicalConfigService parsing and consistency checking.<br>
 * TODO: find a way to simulate invalid unicode characters easily. Get some inspiration in https://issues.apache.org/jira/browse/LANG-100 ?<br>
 * @author skwg9735
 */
@ContextConfiguration(locations = "LogicalModelTest-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class  LogicalConfigServiceTest {

	public static final LogicalConfigServiceUtils LOGICAL_CONFIG_SERVICE_UTILS = new LogicalConfigServiceUtils();
    @Autowired
    SpringooLogicalModelCatalog utilSpringooIntegration;
	@Autowired
	ConfigLogicalModelCatalog configLogicalModelCatalog;

    @Autowired
    PersistenceTestUtil persistenceTestUtil;

    //syntaxic sugar
    private HashSet<String> IGNORED_DUPLICATES;
    private StringBuffer IGNORE_COLLISIONS;

    public LogicalConfigServiceTest() {
    }

    @Before
    public void setUp() throws Exception {
        IGNORED_DUPLICATES = new HashSet<String>();
        IGNORE_COLLISIONS = new StringBuffer();
    }

    /**
     * Tests that the long string content properly serializes.
     * TODO: test with postgresql in addition to default hsqldb
     * @throws BusinessException
     */
    @Test
    @DirtiesContext
    public void testPersistenceOfLongConfigSetContent() throws BusinessException {
        createLongConfigSetContent(LogicalConfigService.MAX_CONFIG_SET_CHARS -1, true);
    }

	@Test
	public void testConfigSetTooLargeSizeDetected() throws BusinessException {
        try {
            createLongConfigSetContent(LogicalConfigService.MAX_CONFIG_SET_CHARS + 1, false);
			Assert.assertFalse("Should have failed", true);
		} catch (InvalidConfigServiceException e) {
			Assert.assertEquals(ErrorType.TOO_LONG, e.getType());
			Assert.assertEquals(LogicalConfigService.MAX_CONFIG_SET_CHARS, e.getMaxLength());
		}
	}

    private void createLongConfigSetContent(int size, boolean testPersistence) throws BusinessException {
        String frontEndConfigSetContent = "#Comment too long ?";
        frontEndConfigSetContent = StringUtils.rightPad(frontEndConfigSetContent, size, '?');
        createConfigSetLogicalModelCatalog(frontEndConfigSetContent, testPersistence);
    }

    @Test
	public void testDuplicateKeysInSingleConfigSetDetected() throws BusinessException {
		try {
			String frontEndConfigSetContent = "Key1=value1\n" + "Key1=value2\n";
            LogicalDeployment ld = configLogicalModelCatalog.createLogicalModel("TestInvalidUnicodeCharacters");
            Set<LogicalConfigService> frontEndConfigService = ld.listLogicalServices(LogicalConfigService.class, ConfigLogicalModelCatalog.FRONT_END_CONFIG);
            assert frontEndConfigService.size() ==1;

            LogicalConfigService configService = frontEndConfigService.iterator().next();
            configService.setConfigSetContent(frontEndConfigSetContent);

            configService.checkForDuplicatesWithinService();
            Assert.fail("expected exception to be caught");
        } catch (InvalidConfigServiceException e) {
			Assert.assertEquals(ErrorType.DUPLICATE_KEYS, e.getType());
			Assert.assertEquals(1, e.getDuplicateKeys().size());
			Assert.assertEquals("Key1", e.getDuplicateKeys().iterator().next());
		}
	}

    /**
     * Checks that duplicates among configsets are detected, but that the full list of properties can still
     * be displayed to help end-users diagnose these duplicates
     * @throws BusinessException
     */
	@Test
	public void testDuplicateKeysAmongConfigSetsDetectedAndDisplayed() throws BusinessException {
        String frontEndConfigSetContent = "Key1=value1\n" + "Key2=value2\n";
        String backendConfigSetContent = "";
        String wholeAppConfigSetContent = "Key2=value1\n" + "Key3=value2\n";
        LogicalDeployment ld = configLogicalModelCatalog.createLogicalModel("TestInvalidUnicodeCharacters");

        Set<LogicalConfigService> backConfig = ld.listLogicalServices(LogicalConfigService.class, ConfigLogicalModelCatalog.BACK_END_CONFIG);
        assert backConfig.size() ==1;
        backConfig.iterator().next().setConfigSetContent(backendConfigSetContent);

        Set<LogicalConfigService> frontConfig = ld.listLogicalServices(LogicalConfigService.class, ConfigLogicalModelCatalog.FRONT_END_CONFIG);
        assert frontConfig.size() ==1;
        frontConfig.iterator().next().setConfigSetContent(frontEndConfigSetContent);

        Set<LogicalConfigService> wholeConfig = ld.listLogicalServices(LogicalConfigService.class, ConfigLogicalModelCatalog.WHOLE_APP_CONFIG);
        assert wholeConfig.size() ==1;
        wholeConfig.iterator().next().setConfigSetContent(wholeAppConfigSetContent);

        try {
			ld.checkOverallConsistency();
			Assert.assertFalse("Should have failed", true);
		} catch (LogicalModelNotConsistentException ie) {
            boolean foundMatchingConfigException = false;
            for (BusinessException businessException : ie.getErrors()) {
                if (businessException instanceof InvalidConfigServiceException) {
                    InvalidConfigServiceException e = (InvalidConfigServiceException) businessException;
                    Assert.assertEquals(ErrorType.DUPLICATE_KEYS, e.getType());
                    Assert.assertEquals(1, e.getDuplicateKeys().size());
                    Assert.assertEquals("configKey2", e.getDuplicateKeys().iterator().next());
                    foundMatchingConfigException = true;
                }
            }
            assertTrue("Did not catch an InvalidConfigServiceException", foundMatchingConfigException);
        }

        //Test how the UI can display these duplicated keys to report them
        StringBuilder sb = new StringBuilder();
        List<ProcessingNode> processingNodes = ld.listProcessingNodes();
        for (ProcessingNode processingNode : processingNodes) {
            Map<String,String> mergedProperties = new HashMap<String,String>();
            String executionNodeLabel = processingNode.getLabel();
            List<LogicalConfigService> logicalConfigServices = processingNode.listLogicalServices(LogicalConfigService.class);
            for (LogicalConfigService logicalConfigService : logicalConfigServices) {

                LogicalConfigServiceUtils.StructuredLogicalConfigServiceContent structuredLogicalConfigServiceContent = LOGICAL_CONFIG_SERVICE_UTILS.parseConfigContent(logicalConfigService.getConfigSetContent());
                for (LogicalConfigServiceUtils.ConfigEntry configEntry : structuredLogicalConfigServiceContent.getConfigEntries()) {
                    String previousValue = mergedProperties.put(configEntry.getKey(), configEntry.getValue());
                    sb.append("JEEProcess=" + executionNodeLabel + ", ConfigSet=" + logicalConfigService.getLabel() + ", key=" + logicalConfigService.getKeyPrefix() + configEntry.getKey() + ", value=" + configEntry.getValue() + ", isDuplicate=" + (previousValue != null) + ", comment=" + configEntry.getComment() + "\n");
                }
            }
        }
        String conf = sb.toString();
        Assertions.assertThat(conf).contains("JEEProcess=BackEnd, ConfigSet=WholeAppConfig, key=configKey2, value=value1, isDuplicate=false, comment=null");
        Assertions.assertThat(conf).contains("JEEProcess=BackEnd, ConfigSet=WholeAppConfig, key=configKey3, value=value2, isDuplicate=false, comment=null");
        Assertions.assertThat(conf).contains("JEEProcess=FrontEnd, ConfigSet=FrontEndConfig, key=configKey1, value=value1, isDuplicate=false, comment=null");
        Assertions.assertThat(conf).contains("JEEProcess=FrontEnd, ConfigSet=FrontEndConfig, key=configKey2, value=value2, isDuplicate=false, comment=null");
        Assertions.assertThat(conf).contains("JEEProcess=FrontEnd, ConfigSet=WholeAppConfig, key=configKey2, value=value1, isDuplicate=true, comment=null");
        Assertions.assertThat(conf).contains("JEEProcess=FrontEnd, ConfigSet=WholeAppConfig, key=configKey3, value=value2, isDuplicate=false, comment=null");
    }


	@Test
	public void testTooManyKeysConfigSetsDetected() throws BusinessException {
		try {
			StringBuffer sb = new StringBuffer();
			for (int keyIndex = 0; keyIndex < ProcessingNode.MAX_CONFIG_SET_ENTRIES_PER_EXEC_NODE + 1; keyIndex++) {
				sb.append("key" + keyIndex + "=" + "value" + keyIndex + "\n");
			}
			String frontEndConfigSetContent = sb.toString();
			createConfigSetLogicalModelCatalog(frontEndConfigSetContent, false);
			Assert.assertFalse("Should have failed", true);
		} catch (LogicalModelNotConsistentException ie) {
            boolean foundMatchingConfigException = false;
            for (BusinessException businessException : ie.getErrors()) {
                if (businessException instanceof InvalidConfigServiceException) {
                    InvalidConfigServiceException e = (InvalidConfigServiceException) businessException;
                    Assert.assertEquals(ErrorType.TOO_MANY_ENTRIES, e.getType());
                    Assert.assertEquals(ProcessingNode.MAX_CONFIG_SET_ENTRIES_PER_EXEC_NODE, e.getMaxEntryCount());
                    Assert.assertEquals(ProcessingNode.MAX_CONFIG_SET_ENTRIES_PER_EXEC_NODE + 3, e.getEntryCount());
                    foundMatchingConfigException = true;
                }
            }
            assertTrue("Did not catch an InvalidConfigServiceException", foundMatchingConfigException);

        }
	}

	@Test
    public void testEmptyKeyPrefix() throws BusinessException {
        LogicalConfigService lcs = createConfigSetLogicalModelCatalog("toto=titi", false, "");
		Properties mergedProperties = new Properties();
        lcs.mergeAndCheckForDuplicateKeys(mergedProperties, IGNORED_DUPLICATES, IGNORE_COLLISIONS);
		Assert.assertNotNull(mergedProperties.getProperty("toto"));
		Assert.assertEquals(mergedProperties.getProperty("toto"), "titi");
	}

    @Test()
	public void empty_key_name_should_be_supported() throws BusinessException {
		LogicalConfigService lcs = createConfigSetLogicalModelCatalog("#comment\n=some value", false, "prefix.");
        Properties mergedProperties = new Properties();
        lcs.mergeAndCheckForDuplicateKeys(mergedProperties, IGNORED_DUPLICATES, IGNORE_COLLISIONS);
        Assert.assertEquals("expected empty key to return specified value", "some value", mergedProperties.getProperty("prefix."));
	}

    @Test()
	public void empty_key_name_should_be_supported_without_prefix() throws BusinessException {
		LogicalConfigService lcs = createConfigSetLogicalModelCatalog("#comment\n=some value", false, "");
        Properties mergedProperties = new Properties();
        lcs.mergeAndCheckForDuplicateKeys(mergedProperties, IGNORED_DUPLICATES, IGNORE_COLLISIONS);
        Assert.assertEquals("expected empty key to return specified value", "some value", mergedProperties.getProperty(""));
	}

    @Test
    public void entries_with_empty_values_are_supported() throws BusinessException {
        LogicalConfigService lcs = createConfigSetLogicalModelCatalog("#comment\nkeyWithEmptyValue=", false, "prefix.");
        Properties mergedProperties = new Properties();
        lcs.mergeAndCheckForDuplicateKeys(mergedProperties, IGNORED_DUPLICATES, IGNORE_COLLISIONS);
        Assert.assertTrue("expected keyWithEmptyValue to be present in list of keys", mergedProperties.containsKey("prefix.keyWithEmptyValue"));
        Assert.assertEquals("expected empty value to return empty string", "", mergedProperties.getProperty("prefix.keyWithEmptyValue"));
    }

    @Test
	public void testConfigServiceUtils() throws ConfigurationException, InvalidConfigServiceException {
        //given
		String header =
                "#HeaderComment1\n" +
                "#HeaderComment2";
		String propertiesContent = header + "\n\n" + "#Comment1.1\n" + "Key1=value1\n" + "# Comment2 Line1\n" + "# Comment2 Line2\n" + "Key2=value2\n";

        //when
		LogicalConfigServiceUtils.StructuredLogicalConfigServiceContent parsedContent = LOGICAL_CONFIG_SERVICE_UTILS.parseConfigContent(propertiesContent);

        //then
		List<LogicalConfigServiceUtils.ConfigEntry> expectedEntries = new ArrayList<LogicalConfigServiceUtils.ConfigEntry>();
		expectedEntries.add(new LogicalConfigServiceUtils.ConfigEntry("Key1", "value1", "Comment1.1"));
		expectedEntries.add(new LogicalConfigServiceUtils.ConfigEntry("Key2", "value2", " Comment2 Line1\n Comment2 Line2"));
		LogicalConfigServiceUtils.StructuredLogicalConfigServiceContent expectedContent = new LogicalConfigServiceUtils.StructuredLogicalConfigServiceContent(
				header, expectedEntries);

		assertEquals(expectedContent, parsedContent);

		// Then dump back
        //when
		String dumpedProperties = LOGICAL_CONFIG_SERVICE_UTILS.dumpConfigContentToString(expectedContent);

        //Then: dumped content normalizes comments with a prefix space if missing (i.e. not strictly symetric)
        String expectedNormalizedDumpedContent =
                "#HeaderComment1\n" +
                "#HeaderComment2\n" +
                "\n" +
                "# Comment1.1\n" +
                "Key1=value1\n" +
                "#  Comment2 Line1\n" +
                "#  Comment2 Line2\n" +
                "Key2=value2\n";
		assertEquals(expectedNormalizedDumpedContent, dumpedProperties);
	}

    @Test
    public void parses_comments_received_from_apache_commons() {
        //removes pound as prefix
        assertCommentEscaped("comment", "#comment");
        assertCommentEscaped("comment1\ncomment2", "#comment1\n#comment2");
        assertCommentEscaped("comment1\n\rcomment2", "#comment1\n\r#comment2");
        assertCommentEscaped("comment1 with # embedded in between\n\rcomment2", "#comment1 with # embedded in between\n\r#comment2");
    }

    private void assertCommentEscaped(String expectedEscape, String rawComment) {
        assertEquals(expectedEscape, LOGICAL_CONFIG_SERVICE_UTILS.escapesPoundsInComments(rawComment));
    }

    @Test
	public void testConfigServiceUtils_when_no_header_comments() throws ConfigurationException, InvalidConfigServiceException {
		String propertiesContent = "#Comment1.1\n" + "Key1=value1\n";

		LogicalConfigServiceUtils.StructuredLogicalConfigServiceContent parsedContent = LOGICAL_CONFIG_SERVICE_UTILS.parseConfigContent(propertiesContent);
		List<LogicalConfigServiceUtils.ConfigEntry> expectedEntries = new ArrayList<LogicalConfigServiceUtils.ConfigEntry>();
		expectedEntries.add(new LogicalConfigServiceUtils.ConfigEntry("Key1", "value1", "Comment1.1"));
		LogicalConfigServiceUtils.StructuredLogicalConfigServiceContent expectedContent = new LogicalConfigServiceUtils.StructuredLogicalConfigServiceContent(
				null, expectedEntries);

		assertEquals(expectedContent, parsedContent);
    }

	private LogicalConfigService createConfigSetLogicalModelCatalog(String frontEndConfigSetContent, boolean testPersistence) throws InvalidConfigServiceException, LogicalModelNotConsistentException {
		return createConfigSetLogicalModelCatalog(frontEndConfigSetContent, testPersistence, "");
	}

    private LogicalConfigService createConfigSetLogicalModelCatalog(String frontEndConfigSetContent, boolean testPersistence, String keyPrefix) throws InvalidConfigServiceException, LogicalModelNotConsistentException {
        LogicalDeployment ld = configLogicalModelCatalog.createLogicalModel("TestInvalidUnicodeCharacters");

        Set<LogicalConfigService> config = ld.listLogicalServices(LogicalConfigService.class, ConfigLogicalModelCatalog.FRONT_END_CONFIG);
        assert config.size() ==1;

        LogicalConfigService lcs = config.iterator().next();
        lcs.setConfigSetContent(frontEndConfigSetContent);
        lcs.setKeyPrefix(keyPrefix);

        if (testPersistence) {
            persistenceTestUtil.persistObjects(ld);
            LogicalDeployment reloadedLd = persistenceTestUtil.reloadLogicalDeployment(ld);
            assertEquals(ld, reloadedLd);
        }
		ld.checkOverallConsistency();
		return lcs;
	}
    @Test(expected=IllegalArgumentException.class)
    public void keyPrefix_cannot_be_null() throws BusinessException {
        LogicalConfigService lcs = createConfigSetLogicalModelCatalog("toto=titi", false, null);
        lcs.setKeyPrefix(null);
		
	}


}
