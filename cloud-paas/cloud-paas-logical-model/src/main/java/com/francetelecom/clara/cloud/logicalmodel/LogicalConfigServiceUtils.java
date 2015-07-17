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

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;

import com.francetelecom.clara.cloud.commons.BaseEqualsToStringObject;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.ValidatorUtil;
import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException.ErrorType;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

/**
 * Utility class to help parsing and writing structured config content. It forbids duplicate keys or value lists.
 *
 * Unlike {@link org.apache.commons.configuration.PropertiesConfiguration} which behaves as "If a key is used more than once, the values are appended like if they were on the same line separated with commas.",
 * this class will throw exceptions in such cases.
 */
public class LogicalConfigServiceUtils implements Serializable {

    private static final long serialVersionUID = 6093491341610695862L;

	private static final Function<ConfigEntry, String> GET_KEY = new Function<ConfigEntry, String>() {
		@Override
		public String apply(ConfigEntry configEntry) {
			return configEntry.getKey();
		}
	};
	
    public String dumpConfigContentToString(StructuredLogicalConfigServiceContent content) throws InvalidConfigServiceException {
        StringWriter stringWriter = new StringWriter();
        dumpConfigContent(content, stringWriter);
        return stringWriter.toString();
    }

    public void dumpConfigContent(StructuredLogicalConfigServiceContent content, Writer writer) throws InvalidConfigServiceException {
        try {
            ValidatorUtil.validate(content);
        } catch (TechnicalException e) {
            throw new InvalidConfigServiceException("Invalid structured content:" + e, e);
        }
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        PropertiesConfigurationLayout layout = propertiesConfiguration.getLayout();
        layout.setLineSeparator("\n");
        String headerComment = content.getHeaderComment();
        if (headerComment != null) {
            layout.setHeaderComment(headerComment);
        }
        for (ConfigEntry configEntry : content.configEntries) {
            String key = configEntry.getKey();
            propertiesConfiguration.addProperty(key, configEntry.getValue());
            String comment = configEntry.getComment();
            layout.setSeparator(key, "=");
            if (comment != null) {
                layout.setComment(key, comment);
            }
        }
        try {
            propertiesConfiguration.save(writer);
        } catch (ConfigurationException e) {
            throw new InvalidConfigServiceException("Invalid structured content or output:" + e, e);
        }
    }


    public StructuredLogicalConfigServiceContent parseConfigContent(String content) throws InvalidConfigServiceException {
        return parseConfigContent(new StringReader(content));
    }

    /**
     *
     *
     * @param reader
     * @return
     */
    public StructuredLogicalConfigServiceContent parseConfigContent(Reader reader) throws InvalidConfigServiceException {
        List<ConfigEntry> parsedEntries = new ArrayList<ConfigEntry>();
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();

        try {
            propertiesConfiguration.load(reader);
        } catch (ConfigurationException e) {
            InvalidConfigServiceException invalidConfigServiceException = new InvalidConfigServiceException("Invalid config content. Caught:" + e, e);
            throw invalidConfigServiceException;
        }
        PropertiesConfigurationLayout layout = propertiesConfiguration.getLayout();
        String headerComment = layout.getHeaderComment();

        Set<String> keys = layout.getKeys();
        Set<String> duplicates = new HashSet<String>();
        for (String key : keys) {
            String comment = layout.getComment(key);
            if (comment != null) {
                comment = escapesPoundsInComments(comment);
            }
            if (! layout.isSingleLine(key)) {
                //reject the duplicate key
            	duplicates.add(key);
            } else {
                String value = propertiesConfiguration.getString(key);
                parsedEntries.add(new ConfigEntry(key, value, comment));
            }
        }
        
        if (duplicates.size() > 0) {
        	InvalidConfigServiceException invalidConfigServiceException = new InvalidConfigServiceException("Collisions! " + duplicates);
        	invalidConfigServiceException.setType(ErrorType.DUPLICATE_KEYS);
        	invalidConfigServiceException.getDuplicateKeys().addAll(duplicates);
        	throw invalidConfigServiceException;
        }
        
        List<ConfigEntry> configEntries = parsedEntries;
        StructuredLogicalConfigServiceContent structuredLogicalConfigServiceContent = new StructuredLogicalConfigServiceContent(headerComment, configEntries);
        return structuredLogicalConfigServiceContent;
    }

    protected String escapesPoundsInComments(String rawComment) {
        return Pattern.compile("^[#!]", Pattern.MULTILINE).matcher(rawComment).replaceAll("");
    }

    /**
     * Provides structured typed access to a configuration string formatted in java.util.Properties format.
     */
    public static class StructuredLogicalConfigServiceContent extends BaseEqualsToStringObject implements Serializable {
        private static final long serialVersionUID = -3588073237349664245L;
        /**
         * Allows for null comment
         */
        private String headerComment;

        /**
         * Allows for empty list
         */
        @NotNull
        @Valid
        private List<ConfigEntry> configEntries;

        public StructuredLogicalConfigServiceContent(String headerComment, List<ConfigEntry> configEntries) {
            this.headerComment = headerComment;
            this.configEntries = configEntries;
        }

        public String getHeaderComment() {
            return headerComment;
        }

        public void setHeaderComment(String headerComment) {
            this.headerComment = headerComment;
        }

        public List<ConfigEntry> getConfigEntries() {
            return configEntries;
        }

        public void setConfigEntries(List<ConfigEntry> configEntries) {
            this.configEntries = configEntries;
        }

		public Collection<String> listKeys() {
			return Collections2.transform(configEntries, GET_KEY);			
		}

    }

    /**
     * Represents individual entries in a a configuration string formatted in java.util.Properties format: comments, key, and value.
     */
    public static class ConfigEntry extends BaseEqualsToStringObject implements Serializable {
        private static final long serialVersionUID = 5371986407117742995L;
        /**
         * Allows for null comment
         */
        private String comment;
        @NotNull
        private String key;
        @NotNull
        private String value;

        public ConfigEntry(String key, String value, String comment) {
            this.comment = comment;
            this.key = key;
            this.value = value;
        }

        /**
         * Note that comments don't have a # or ! prefix
         * @return
         */
        public String getComment() {
            return comment;
        }

        /**
         * Assigns the comment
         * @param comment a single or multiline comment without # or ! prefix
         */
        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
    
    /**
     * Load a file from it's path, parse it's config content and return a Set of all keys from it.<br>
     * @param filePath
     * @return
     * @throws InvalidConfigServiceException
     */
	public Set<String> loadKeysFromFile(String filePath) throws InvalidConfigServiceException{
		InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filePath));
		StructuredLogicalConfigServiceContent releaseConfigContent = parseConfigContent(inputStreamReader);
		return Sets.newTreeSet(Collections2.transform(releaseConfigContent.getConfigEntries(), GET_KEY));
	}
}
