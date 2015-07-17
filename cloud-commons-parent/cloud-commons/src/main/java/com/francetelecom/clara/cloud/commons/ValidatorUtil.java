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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;



/**
 * utility class to check JSR303 validator spec
 * 
 * 
 * @author apog7416
 *
 */
public class ValidatorUtil {

	private static Logger logger=LoggerFactory.getLogger(ValidatorUtil.class.getName());

    public static char[] FORBIDDEN_CHARS = {'^', '\\', '?', '%', '*', ':', '|', '"', '\'', '<', '>', '.', ':', ';', '/', ' ', '&', '(', ')'  };

    /**
     * Allowed pattern for characters within a file system path. Added a mix of Windows and Unix characters
     * See http://en.wikipedia.org/wiki/Filename#Reserved_characters_and_words
     * To be used within @Pattern annotation, this needs to be a constant, so it is manually copied from
     * {@link #buildPatternsFromForbiddenChars(char[])}
     */
    public static final String FILESYSTEM_PATTERN = "[^\\^\\\\\\?\\%\\*\\:\\|\\\"\\'\\<\\>\\.\\:\\;\\/\\ \\&\\(\\)]+";
    //public static final String FILESYSTEM_PATTERN = buildPatternsFromForbiddenChars(FORBIDDEN_CHARS);


    public static String sanitizeStringForFileSystemPath(String path) {
        if (!path.matches(FILESYSTEM_PATTERN)) {
            path = removeForbiddenChars(path, FORBIDDEN_CHARS);
        }
        return path;
    }

    public static String buildPatternsFromForbiddenChars(char[] forbiddenChars) {
        StringBuilder stringBuilder = new StringBuilder(forbiddenChars.length*3);
        stringBuilder.append("[^");
        for (char forbiddenChar : forbiddenChars) {
            stringBuilder.append("\\");
            stringBuilder.append(forbiddenChar);
        }
        stringBuilder.append("]+");
        return stringBuilder.toString();
    }

    public static String removeForbiddenChars(String path, char [] forbiddenChars) {
        String sanitizedString = path;
        for (char forbiddenChar : forbiddenChars) {
            sanitizedString = sanitizedString.replace(forbiddenChar, '_');
        }
        return sanitizedString;
    }


    private static <T> Set<ConstraintViolation<T>> jsr303ModelValidation(T toCheck, StringBuffer stringBuffer) {
        //JSR 303 model validation
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        Set<ConstraintViolation<T>> violations = validator.validate(toCheck);
        for (ConstraintViolation<T> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            String message1 = "invalid value for: '" + propertyPath + "': " + message;
            String message2 = "Invalid bean [" + toCheck + "] violating  condition for bean: " + propertyPath + ": " + message + " whereas it has value: [" + violation.getInvalidValue() + "]";
            logger.info(message1);
            logger.info(message2);
            stringBuffer.append(message1);
            stringBuffer.append("\n");
            stringBuffer.append(message2);
            stringBuffer.append("\n");
        }
        return violations;
    }

    public static  <T> void validate(T toCheck) throws TechnicalException {
        StringBuffer stringBuffer = new StringBuffer();
        Set<ConstraintViolation<T>> violations = jsr303ModelValidation(toCheck, stringBuffer);
        if (! violations.isEmpty()) {
            throw new TechnicalException("Constraint violated on bean. " + stringBuffer.toString());
        }
    }

    /**
     * provide a way to throw a business exception on validation error (user input case)
     * @param toCheck
     * @param <T>
     * @throws BusinessException
     */
    public static  <T> void validateBusiness(T toCheck) throws BusinessException {
        StringBuffer stringBuffer = new StringBuffer();
        Set<ConstraintViolation<T>> violations = jsr303ModelValidation(toCheck, stringBuffer);
        if (! violations.isEmpty()) {
            throw new BusinessException("Constraint violated on bean. " + stringBuffer.toString());
        }
    }
}
