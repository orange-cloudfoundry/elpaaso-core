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

import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.commons.GuiMapping;
import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException.ErrorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

//import com.francetelecom.clara.cloud.logicalmodel.validators.ConfigDuplicateKeys;

/**
 * Config service.
 *
 * An application dev team defines the configuration contract of the application in the logical model:
 * <p>
 * By being associated to a ConfigSet, an executionNode exposes contract of configureable keys it supports. This contract precises the intended syntax and associated semantic with each configuration entry. Such contract is offered to users that will instanciate this Application Release into environments.
 * The contract is composed of a set of:
 * <p> <ul>
 * <li>key name: a plain unicode string. Should be unique per ConfigSet
 * <li>key value: the default value for the key. May be empty in which case the empty string "" is returned.
 * Recommended best practice is to provide sound default value for the current production environment.
 * <li>human readeable comments that comment each key, value, their intended syntax and associated semantic
 * </ul>
 * <p>
 * The contract is provided as Unicode character stream, that conforms to the java.util.Properties.load () specifications
 * <p>
 * Note that a ConfigSet content should not exceed 50 KB, and number of keys for an ExecutionNode should not exceed 300 keys.
 */
@XmlRootElement
@Entity
@Table(name="CONFIG_SERVICE")
@GuiClassMapping(serviceCatalogName = "Config", serviceCatalogNameKey = "configuration", status = GuiClassMapping.StatusType.SUPPORTED, isExternal = false)
public class LogicalConfigService extends LogicalService {

    private static final long serialVersionUID = 2L;

    public static final int MAX_CONFIG_SET_CHARS = 50000;
    /**
     * The config set content in the format of the
     * {@link java.util.Properties#load(java.io.Reader)}
     */
    @Size(min = 0, max = MAX_CONFIG_SET_CHARS)
    @NotNull
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
//    @ConfigDuplicateKeys
    private String configSetContent;

    private static Logger logger=LoggerFactory.getLogger(LogicalConfigService.class.getName());

    private static final LogicalConfigServiceUtils LOGICAL_CONFIG_SERVICE_UTILS = new LogicalConfigServiceUtils();

    /**
     * Preferred key prefix to be looked up by the application. This allows the application
     * to distinguish among multiple LogicalConfigService subscriptions.
     *
     * TODO: plan to migrate to the {@link LogicalNodeServiceAssociation}
     * @see javax.naming.Name
     * @see javax.naming.Context#lookup(String)
     */
    //Because some applications does not have keyPrefix defined, this param became optionnal
//    @NotNull
//    @Size(min = 0)
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
    private String keyPrefix="";

    /** default constructor for JPA */
    public LogicalConfigService() {
    }

    /**
     * Constructs a new config service
     * @throws InvalidConfigServiceException if the maximum size of number of keys is reached
     * @deprecated Should not be called anymore, use empty constructor instead
     * followed by {@link LogicalDeployment#addLogicalService(LogicalService)}
     */
    public LogicalConfigService(String label, LogicalDeployment logicalDeployment, String configSetContent) throws InvalidConfigServiceException {
        super(label, logicalDeployment);
        setConfigSetContent(configSetContent);
    }

    public String getConfigSetContent() {
        return configSetContent;
    }

    /**
     * Assigns the content of a config service
     * @throws InvalidConfigServiceException if the maximum size of number of keys is reached
     */
    public void setConfigSetContent(String configSetContent) throws InvalidConfigServiceException {
        int length = configSetContent.length();
        if (length > MAX_CONFIG_SET_CHARS) {
        	InvalidConfigServiceException invalidConfigServiceException = new InvalidConfigServiceException("Invalid size for configSet=" + getLabel() + length + "exceeds max " + MAX_CONFIG_SET_CHARS);
        	invalidConfigServiceException.setType(ErrorType.TOO_LONG);
        	invalidConfigServiceException.setMaxLength(MAX_CONFIG_SET_CHARS);
            throw invalidConfigServiceException;
        }
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(configSetContent));
        } catch (IOException e) {
            String message = "Incorrect properties format for configSet=" + getLabel();
            logger.info(message, e);
        	InvalidConfigServiceException invalidConfigServiceException = new InvalidConfigServiceException(e.getMessage());
        	invalidConfigServiceException.setType(ErrorType.SYNTAX_ERROR);
            throw invalidConfigServiceException;
        }
        this.configSetContent = configSetContent;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
    	Assert.notNull(keyPrefix, "cannot set keyPrefix. keyPrefix should not be null.");
        this.keyPrefix = keyPrefix;
    }

    /**
     * Utility method to check for key duplicates within this service
     * @throws InvalidConfigServiceException if there are duplicate keys.
     */
    public void checkForDuplicatesWithinService() throws InvalidConfigServiceException {
        LOGICAL_CONFIG_SERVICE_UTILS.parseConfigContent(this.getConfigSetContent());
    }

    /**
     * Utility method to merge this service entries into the specified properties instance, and collect duplicate keys
     * @param mergedProperties A {@link Properties} into which to add all entries of the current service
     * @param duplicates Will collect the duplicate keys found
     * @param collisions An english human readeable diagnostic of the collisions
     */
    protected void mergeAndCheckForDuplicateKeys(Properties mergedProperties, Set<String> duplicates, StringBuffer collisions) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(this.getConfigSetContent()));
        } catch (IOException e) {
            logger.error("Unexpected ConfigService format issues, expected to be caught in setConfigSetContent() method, caught:" + e, e);
            assert false: "ConfigService parsing errors supposed to be caught in constructor+setter";
        }
        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        for (Map.Entry<Object, Object>entry: entries){
            String key = (this.getKeyPrefix() == null ? "" : this.getKeyPrefix()) + entry.getKey();
            Object entryObjectValue = entry.getValue();
            assert entryObjectValue instanceof String : "unexpected properties value of type:" + entryObjectValue.getClass();
            String value = (String) entryObjectValue;
            Object previousValue = mergedProperties.setProperty(key, value);
            if (previousValue != null) {
                duplicates.add(key.toString());
                collisions.append("Collision between keys. In set [" + this.getLabel() + "], key=[" + key + "] and value=[" + value+ "] collides with [" + previousValue + "]\n");
            }
        }
    }

}
