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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.Constants;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Properties;

/**
 * A jndi-aware extension of the Spring PropertyPlaceholderConfigurer (v2.5.6).
 * By default this resolves properties from jndi first, then any referenced
 * property files and then falls back to System properties and the System
 * Environment.
 * <p>
 * It also performs property expansion on values passed in as Locations in case
 * the resource locations themselves have placeHolder values that need to be
 * resolved from jndi or system properties
 * </p>
 * 
 * <p>
 * An example usage in which we have two property files referenced via a
 * configDirectory parameter injected as a jndi or system property
 * </p>
 * 
 * <pre>
 *  &lt;!--
 *   Expose jndi, system and config properties to bean definitions. This expects a jndi or system
 *   property configDirectory to our directory of configuration files
 *  --&gt;
 *  &lt;bean
 *    id=&quot;propertyPlaceholderConfigurer&quot;
 *    class=&quot;org.springframework.beans.factory.config.JndiAwarePropertyPlaceholderConfigurer&quot;
 *    init-method=&quot;initialize&quot;&gt;
 *    &lt;property
 *      name=&quot;locations&quot;&gt;
 *      &lt;list&gt;
 *        &lt;value&gt;file:${configDirectory}/../common.properties
 *        &lt;/value&gt;
 *        &lt;value&gt;file:${configDirectory}/application.properties
 *        &lt;/value&gt;
 *      &lt;/list&gt;
 *    &lt;/property&gt;
 *  &lt;/bean&gt;
 * </pre>
 *
 * @Author arthur.branham@morganstanley.com
 * @see <a href="arthur.branham%2540morganstanley.com">Spring ticket SPR-3030 from Arthur</a>
 */
public class JndiAwarePropertyPlaceholderConfigurer
        extends PropertyPlaceholderConfigurer {
    private static final Constants pConstants = new Constants(PropertyPlaceholderConfigurer.class);

    /**
     * by default we will search for jndi values and if found these will
     * override any other values
     */
    private int jndiPropertiesMode = SYSTEM_PROPERTIES_MODE_OVERRIDE;

    /**
     * if searchJndiEnvironment is set to true then we will search the jndi
     * environment for properties
     */
    private boolean searchJndiEnvironment = true;

    /**
     * Set to true to expect the "java:comp/env/" prefix
     */
    private boolean resourceRef = false;

    /**
     * Takes an array or Resources and for any of type UrlResource, resolves any
     * properties in the URL. Note that as we haven't loaded the property fiels
     * at this stage we are resolving properties against the system and jndi
     * sets (if any)
     * 
     * @param locations
     */
    @SuppressWarnings("unchecked")
    private void processLocationValues(Resource[] locations) {
        if (locations != null) {
            Properties props = new Properties();
            HashSet visitedPlaceholders = new HashSet();
            for (int i = 0; i < locations.length; i++) {
                if (locations[i] instanceof UrlResource) {
                    UrlResource file = (UrlResource) locations[i];
                    String path;
                    try {
                        path = file.getURL()
                                .toString();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String value = parseStringValue(path, props, visitedPlaceholders);
                    if (!StringUtils.equals(path, value)) {
                        UrlResource newFile;
                        try {
                            newFile = new UrlResource(value);
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                        locations[i] = newFile;
                    }
                }
            }
        }
    }

    /**
     * Resolve the given placeholder using the given properties. Default
     * implementation simply checks for an environment entry for a corresponding
     * property key.
     * <p>
     * Subclasses can override this for customized placeholder-to-key mappings
     * or custom resolution strategies, possibly just using the given lookup as
     * a fallback.
     * 
     * @param placeholder
     *            the placeholder to resolve
     * @return the resolved value, of <code>null</code> if none
     */
    protected String resolveJndiProperty(String placeholder) {
        InitialContext initialContext = null;
        try {
            initialContext = new InitialContext();
            try {

                String prefix;
                if (resourceRef) {
                    prefix = "java:comp/env/";
                } else {
                    prefix = "";
                }
                return (String) initialContext.lookup(prefix + placeholder);
            } catch (NameNotFoundException e) {
                return null;
            } catch (NamingException e) {
                return null;
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (initialContext != null) {
                try {
                    initialContext.close();
                } catch (NamingException e) {
                }
            }
        }
    }

    /**
     * Override of PropertyPlaceholderConfigurer.resolvePlaceholder to handle
     * jndi property lookup.
     * 
     * <p>
     * Warning: note that we are directly accessing the instance variable
     * jndiPropertiesMode rather than accepting it as an input parameter (simply
     * to avoid having to rewrite the calling method)
     * </p>
     * <p>
     * The overrid/fallback mode of jndi properties relative to property-file
     * values can be controlled by settign the jndiPropertiesMode property but
     * the behaviour relative to system properties is hardcoded below. ie In
     * both override and fallback mode a jndi property beats a system property.
     */
    @Override
    protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
        String propVal = null;
        if (systemPropertiesMode == SYSTEM_PROPERTIES_MODE_OVERRIDE) {
            propVal = resolveSystemProperty(placeholder);
        }
        if (searchJndiEnvironment && this.jndiPropertiesMode == SYSTEM_PROPERTIES_MODE_OVERRIDE) {
            // in override mode a jndi property can override a system property
            propVal = resolveJndiProperty(placeholder);
        }
        if (propVal == null) {
            propVal = resolvePlaceholder(placeholder, props);
        }
        if (propVal == null && searchJndiEnvironment && jndiPropertiesMode == SYSTEM_PROPERTIES_MODE_FALLBACK) {
            // in fallback mode a jndi property takes precedence over a system
            // property
            propVal = resolveJndiProperty(placeholder);
        }
        if (propVal == null && systemPropertiesMode == SYSTEM_PROPERTIES_MODE_FALLBACK) {
            propVal = resolveSystemProperty(placeholder);
        }
        return propVal;
    }

    /**
     * Set how to check jndi properties: as fallback, as override, or never. For
     * example, will resolve ${user.dir} to the "user.dir" jndi property.
     * <p>
     * The default is "override": jndi always wins
     * 
     * @see #SYSTEM_PROPERTIES_MODE_NEVER
     * @see #SYSTEM_PROPERTIES_MODE_FALLBACK
     * @see #SYSTEM_PROPERTIES_MODE_OVERRIDE
     * @see #setJndiPropertiesModeName
     */
    public void setJndiPropertiesMode(int jndiPropertiesMode) {
        this.jndiPropertiesMode = jndiPropertiesMode;
    }

    /**
     * Set the jndi property mode by the name of the corresponding constant,
     * e.g. "SYSTEM_PROPERTIES_MODE_OVERRIDE".
     * 
     * @param constantName
     *            name of the constant
     * @throws java.lang.IllegalArgumentException
     *             if an invalid constant was specified
     * @see #setJndiPropertiesMode
     */
    public void setJndiPropertiesModeName(String constantName) throws IllegalArgumentException {
        this.jndiPropertiesMode = pConstants.asNumber(constantName)
                .intValue();
    }

    /**
     * Set a location of a properties file to be loaded.
     * <p>
     * Can point to a classic properties file or to an XML file that follows JDK
     * 1.5's properties XML format.
     */
    @Override
    public void setLocation(Resource location) {
        this.tempLocations = new Resource[] { location };
    }

    private Resource[] tempLocations;

    /**
     * Set locations of properties files to be loaded.
     * <p>
     * Can point to classic properties files or to XML files that follow JDK
     * 1.5's properties XML format.
     * <p>
     * Note: Properties defined in later files will override properties defined
     * earlier files, in case of overlapping keys. Hence, make sure that the
     * most specific files are the last ones in the given list of locations.
     */
    @Override
    public void setLocations(Resource[] locations) {
        tempLocations = locations;
    }

    public void setSearchJndiEnvironment(boolean searchJndiEnvironment) {
        this.searchJndiEnvironment = searchJndiEnvironment;
    }

    /**
     * resolve locations and let our superclass know about them
     */
    public void initialize() {
        processLocationValues(this.tempLocations);
        super.setLocations(tempLocations);
    }

    public boolean isResourceRef() {
        return resourceRef;
    }

    public void setResourceRef(boolean resourceRef) {
        this.resourceRef = resourceRef;
    }
}
