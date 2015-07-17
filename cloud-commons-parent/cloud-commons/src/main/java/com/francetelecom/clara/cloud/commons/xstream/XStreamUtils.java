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
package com.francetelecom.clara.cloud.commons.xstream;

import com.francetelecom.clara.cloud.commons.xstream.logback.AppenderDto;
import com.francetelecom.clara.cloud.commons.xstream.logback.AppenderRefDto;
import com.francetelecom.clara.cloud.commons.xstream.logback.ConfigurationDto;
import com.francetelecom.clara.cloud.commons.xstream.logback.EncoderDto;
import com.francetelecom.clara.cloud.commons.xstream.logback.LoggerDto;
import com.francetelecom.clara.cloud.commons.xstream.logback.RollingPolicyDto;
import com.francetelecom.clara.cloud.commons.xstream.logback.RootDto;
import com.francetelecom.clara.cloud.commons.xstream.logback.TriggeringPolicyDto;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentCollectionConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedSetConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.hibernate.mapper.HibernateMapper;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Utility class for factoring out XStream configuration
 */
public class XStreamUtils {
    public static XStream instanciateXstreamForHibernate() {
        XStream xstream1 =new XStream() {
            protected MapperWrapper wrapMapper(final MapperWrapper next) {
                return new HibernateMapper(next);
            }
        };
        xstream1.registerConverter(new HibernateProxyConverter());
        xstream1.registerConverter(new HibernatePersistentCollectionConverter(xstream1.getMapper()));
        xstream1.registerConverter(new HibernatePersistentMapConverter(xstream1.getMapper()));
        xstream1.registerConverter(new HibernatePersistentSortedMapConverter(xstream1.getMapper()));
        xstream1.registerConverter(new HibernatePersistentSortedSetConverter(xstream1.getMapper()));
        return xstream1;
    }

    /**
     * use xstream API to convert logback DTO classes to a valid xml file. see
     * http://xstream.codehaus.org/alias-tutorial.html
     * 
     * @return the configured xstream
     */
    public static XStream instanciateXstreamForLogback() {
        XStream xstream = new XStream(new DomDriver());

        xstream.aliasAttribute("class", "clazz");

        xstream.alias("configuration", ConfigurationDto.class);
        xstream.useAttributeFor(ConfigurationDto.class, "debug");
        xstream.useAttributeFor(ConfigurationDto.class, "scan");
        xstream.useAttributeFor(ConfigurationDto.class, "scanPeriod");

        xstream.alias("appender", AppenderDto.class);
        xstream.addImplicitCollection(ConfigurationDto.class, "appenders", AppenderDto.class);
        xstream.useAttributeFor(AppenderDto.class, "name");
        xstream.useAttributeFor(AppenderDto.class, "clazz");
        xstream.omitField(AppenderDto.class, "configuration");

        xstream.alias("encoder", EncoderDto.class);
        xstream.omitField(EncoderDto.class, "appender");

        xstream.alias("rollingPolicy", RollingPolicyDto.class);
        xstream.useAttributeFor(RollingPolicyDto.class, "clazz");
        xstream.omitField(RollingPolicyDto.class, "appender");

        xstream.alias("triggeringPolicy", TriggeringPolicyDto.class);
        xstream.useAttributeFor(TriggeringPolicyDto.class, "clazz");
        xstream.omitField(TriggeringPolicyDto.class, "appender");

        xstream.alias("logger", LoggerDto.class);
        xstream.addImplicitCollection(ConfigurationDto.class, "loggers", LoggerDto.class);
        xstream.useAttributeFor(LoggerDto.class, "name");
        xstream.useAttributeFor(LoggerDto.class, "level");
        xstream.omitField(LoggerDto.class, "configuration");

        xstream.alias("root", RootDto.class);
        xstream.addImplicitCollection(RootDto.class, "appenderRefs", AppenderRefDto.class);
        xstream.useAttributeFor(RootDto.class, "level");
        xstream.omitField(RootDto.class, "configuration");

        xstream.alias("appender-ref", AppenderRefDto.class);
        xstream.useAttributeFor(AppenderRefDto.class, "ref");
        xstream.omitField(AppenderRefDto.class, "root");
        xstream.omitField(AppenderRefDto.class, "appender");

        return xstream;
    }

}
