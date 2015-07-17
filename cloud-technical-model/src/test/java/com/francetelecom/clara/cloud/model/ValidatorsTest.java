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
package com.francetelecom.clara.cloud.model;

import com.francetelecom.clara.cloud.commons.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Misc tests for reserved chars string escaping
 */
@RunWith(JUnit4.class)
public class ValidatorsTest {

    private static Logger logger=LoggerFactory.getLogger(ValidatorsTest.class.getName());

    @Test
    public void testFileSystemRegexp() {
        logger.debug("current ValidatorUtil.FILESYSTEM_PATTERN="+ValidatorUtil.FILESYSTEM_PATTERN);
        assertValidPath("abc");
        assertValidSanitization("..abc", "__abc");
        assertValidSanitization("../abc", null);
        assertValidSanitization("/abc", null);
        assertValidSanitization("?abc", "_abc");
        assertValidSanitization("\\abc", "_abc");
        assertValidSanitization("c:abc", null);
        assertValidSanitization("c:abc", null);
    }

    private void assertValidPath(String path) {
        assertTrue("invalid path=" + path, path.matches(ValidatorUtil.FILESYSTEM_PATTERN));
    }
    private void assertInvalidPath(String path) {
        assertFalse("invalid path=" + path, path.matches(ValidatorUtil.FILESYSTEM_PATTERN));
    }

    private void assertValidSanitization(String pathToEscape, String expectedPath) {
        assertInvalidPath(pathToEscape);
        String sanitizedString = ValidatorUtil.removeForbiddenChars(pathToEscape, ValidatorUtil.FORBIDDEN_CHARS);
        assertValidPath(sanitizedString);
        if (expectedPath != null) {
            assertEquals(expectedPath, sanitizedString);
        }
    }
}
