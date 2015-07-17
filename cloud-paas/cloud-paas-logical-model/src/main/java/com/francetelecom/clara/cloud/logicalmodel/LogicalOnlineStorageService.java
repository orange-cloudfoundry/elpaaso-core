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
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Online object storage service.
 * <p>
 * Provides a Java API to store objects (binary files or multimedia content).
 * that are also accessible through HTTP (for both upload and download
 * accesses).
 * <p>
 * This is similar in concept to Amazon S3 service and is offered as a
 * JClouds-compatible currently maps to the France Telecom Store storage
 * enabler.
 * <p>
 * To use the JCloud Blob store API within your application deployed in the
 * paas:
 * <ol>
 * <li>
 * Embed the JClouds blob store provider into your application artefact, this
 * will in turn pull the jClouds blob store API as a transient dependency.
 * <p>
 * <pre>
 *  {@code
 *  <dependency>
 *      <groupId>com.orange.clara.cloud</groupId>
 *     <artifactId>storage-client-jclouds-impl</artifactId>
 *     <version>1.0.1</version>
 *  </dependency>
 *  }
 * </pre>
 * <p>
 * This is available from the Paas maven repository:
 * <p>
 *  {@code
 *             <repository>
 *                 <id>paas.public</id>
 *                 <name>Paas Public Group</name>
 *                 <url>http://ORANGE_MAVEN_REPO/proxy/content/groups/public/</url>
 *             </repository>
 * }
 *
 *     </li>
 * <li>
 * Then use the Clouds Blob Store API, by looking up the credentials looked up
 * in JNDI. See
 * <a href="http://www.jclouds.org/documentation/userguide/blobstore-guide/">
 * blobstore-guide</a> Sample programming model:
 *
 * <pre>
 * {
 * 	&#064;code
 * 	// Load config parameters from JNDI and place them into a set of well-known
 * 	// keys for use by the Store provider
 * 	InitialContext initialContext = new InitialContext();
 *
 * 	String jndiPrefix = &quot;&quot;; // Replace with any prefix that were provisionned in
 * 							// app architecture
 *
 * 	String IDENTITY_KEY = &quot;identity&quot;;
 * 	String CREDENTIAL_KEY = &quot;credential&quot;;
 * 	String ENDPOINT_KEY = &quot;endpoint&quot;;
 * 	String CONTAINER_KEY = &quot;container&quot;;
 *
 * 	String identityValue = (String) initialContext.lookup(jndiPrefix + &quot;/&quot; + IDENTITY_KEY);
 * 	String credentialValue = (String) initialContext.lookup(jndiPrefix + &quot;/&quot; + CREDENTIAL_KEY);
 * 	String endpointValue = (String) initialContext.lookup(jndiPrefix + &quot;/&quot; + ENDPOINT_KEY);
 * 	String containerValue = (String) initialContext.lookup(jndiPrefix + &quot;/&quot; + CONTAINER_KEY);
 *
 * 	Properties storeProviderInitProperties = new Properties();
 * 	storeProviderInitProperties.put(IDENTITY_KEY, identityValue);
 * 	storeProviderInitProperties.put(CREDENTIAL_KEY, credentialValue);
 * 	storeProviderInitProperties.put(ENDPOINT_KEY, endpointValue);
 *
 * 	// Instanciate the context based on the looked up properties
 * 	BlobStoreContext context = new BlobStoreContextFactory().createContext(&quot;store&quot;, storeProviderInitProperties);
 * 	BlobStore blobStore = context.getBlobStore();
 *
 * 	// All blobs should be located in a the single per-environment default
 * 	// container whose name is provided in JNDI
 * 	String containerNameToUse = (String) initialContext.lookup(CONTAINER_KEY);
 * 	boolean newContainerCreated = blobStore.createContainerInLocation(null, containerNameToUse);
 *
 * 	// Write a new Blob
 * 	Blob blob = blobStore.blobBuilder(&quot;anyBlobName&quot;).build();
 * 	blob.setPayload(&quot;An example string content for a blob payload. Streams or byte[] can be used instead&quot;);
 * 	String blobETag = blobStore.putBlob(containerNameToUse, blob);
 *
 * 	// Read a blob content
 * 	Blob readBlobContent = blobStore.getBlob(containerNameToUse, &quot;anyBlobName&quot;);
 * 	Payload readBlobContentPayload = readBlobContent.getPayload();
 *
 * 	// The blob is also accessible by HTTP using a private or public URI
 * 	URI uri = readBlobContent.getMetadata().getUri();
 * 	URI publicUri = readBlobContent.getMetadata().getPublicUri();
 *
 * 	// remove blob
 * 	blobStore.removeBlob(containerNameToUse, blobName);
 *
 * }
 * </pre>
 * <p>
 * For further examples and tests see the sample application, whose sources code
 * is available as maven artefact:
 * <p>
 * <pre>
 * {@code
 * <groupId>com.orange.clara.cloud</groupId>
 * <artifactId>echo-ws-consumer-war</artifactId>
 *
 * }
 * </pre>
 * <p>
 * </li>
 * </ol>
 * <p>
 * See also https://github.com/jclouds/jclouds/blob/master/README.txt
 * <p>
 * TODO: add link to Java API javadoc TODO: add reference to sample client
 */
@XmlRootElement
@Entity
@Table(name = "STORAGE_SERVICE")
@GuiClassMapping(serviceCatalogName = "Blob store", serviceCatalogNameKey = "blob.store", status = GuiClassMapping.StatusType.BETA, isExternal = false)
public class LogicalOnlineStorageService extends LogicalService {


    /**
     * Reserved storage capacity in MB.
     */
    @Min(value = 1, message = "capacity needs to be positive integer value")
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
    private int storageCapacityMb = 10;

    /**
     * Prefered prefix JNDI keys to be looked up by the application. This allows the application
     * to distinguish among multiple LogicalOnlineStorageService subscriptions. The prefix should not be left empty.
     * If the prefix is "petpictures" then
     * the available keys will be "petpictures/identity".
     * The injected properties are <ol>
     * <li>"endpoint"</li>
     * <li>"identity"</li>
     * <li>"credential"</li>
     * <li>"provider"</li>
     * </ol>
     */
    @NotNull
    @Size(min = 0)
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
    private String serviceName = "storage";

    public LogicalOnlineStorageService() {
    }

    /**
     * @deprecated Should not be called anymore, use empty constructor instead
     * followed by {@link LogicalDeployment#addLogicalService(LogicalService)}
     */
    public LogicalOnlineStorageService(String label, LogicalDeployment logicalDeployment) {
        super(label, logicalDeployment);
    }

    public int getStorageCapacityMb() {
        return storageCapacityMb;
    }

    public void setStorageCapacityMb(int storageCapacityMb) {
        Assert.isTrue(storageCapacityMb > 1, "Unable to set storage capacity with value <" + storageCapacityMb + ">. Storage capacity should be greater that 1.");
        this.storageCapacityMb = storageCapacityMb;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        Assert.hasText(serviceName, "Unable to set service name with value <" + serviceName + ">. Service name should not be with empty value.");
        this.serviceName = serviceName;
    }
}
