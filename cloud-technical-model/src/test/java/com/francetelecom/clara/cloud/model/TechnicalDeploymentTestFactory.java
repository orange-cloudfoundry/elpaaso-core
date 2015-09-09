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

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Route;
import com.francetelecom.clara.cloud.techmodel.cf.RouteUri;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;

/**
 * Exposes static methods to create technical deployments for tests
 */
public class TechnicalDeploymentTestFactory {

    /**
     * Create the a wicket JPÄ technical deployment
     *
     * @param appName                the app name
     * @param mavenArtefactGavString maven artefact GAV string
     * @return The technical deployment
     */
    public TechnicalDeployment createWicketJpaTD(String appName, String mavenArtefactGavString) {


        TechnicalDeployment td = new TechnicalDeployment("test-td");
        Space space = new Space();

        MavenReference mavenReference = MavenReference.fromGavString(mavenArtefactGavString);

        App app = new App(space, mavenReference, appName);
        final ManagedService dbaasService = new ManagedService("o-dbaas", "MYSQL_1G", appName + "-db", space);
        app.bindService(dbaasService);
        final ManagedService logService = new ManagedService("o-logs", "splunk", appName + "-log", space);
        app.bindService(logService);
        Route route1 = new Route(new RouteUri("uri1"), "", space);
        app.mapRoute(route1);

        td.add(space);
        td.add(app);
        td.add(route1);
        td.add(dbaasService);
        td.add(logService);

        return td;
    }


}
