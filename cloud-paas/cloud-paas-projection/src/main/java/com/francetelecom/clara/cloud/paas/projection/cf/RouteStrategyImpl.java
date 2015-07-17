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
package com.francetelecom.clara.cloud.paas.projection.cf;

import com.francetelecom.clara.cloud.commons.FqdnHelper;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.google.common.net.InternetDomainName;

public class RouteStrategyImpl implements RouteStrategy {
    public String routeNameSuffix;
    public String paasInstanceId;

    public RouteStrategyImpl(String routeNameSuffix, String paasInstanceId) {
        setRouteNameSuffix(routeNameSuffix);
        setPaasInstanceId(paasInstanceId);
    }

    // ex "webgui1-uattrunk-vdrqa.redacted-domain.org"
    @Override
    public String buildRouteTemplate(ApplicationRelease applicationRelease, LogicalWebGUIService webGUIService) {
        return buildRouteTemplate(applicationRelease.getApplication().getLabel(), applicationRelease.getReleaseVersion(), webGUIService);
    }

    @Override
    public String buildRouteTemplate(String applicationName, String releaseVersion, LogicalWebGUIService webGUIService) {
        StringBuilder sb = new StringBuilder();
        sb.append(FqdnHelper.sanitizeAndTruncatePart(webGUIService.getLabel(), 10));
        sb.append('-');
        sb.append(FqdnHelper.sanitizeAndTruncatePart(applicationName, 10));
        sb.append(FqdnHelper.sanitizeAndTruncatePart(releaseVersion, 10));
        sb.append('-');
        sb.append(FqdnHelper.sanitizeAndTruncatePart(paasInstanceId, 6)); // paas.instance.id property in credentials.properties
        sb.append('.');
        sb.append(routeNameSuffix); //known to be valid, checked in setter
        return FqdnHelper.truncateUnsupportedCharsToValidHost(sb.toString(), "app." + routeNameSuffix);
    }

    public void setRouteNameSuffix(String routeNameSuffix) {
        InternetDomainName.from(routeNameSuffix); //Throws IllegalArgumentException is suffix is invalid
        int maxSuffixLength = 253 - 63 - 1; //reserve 63 chars for the subdomains
        if (routeNameSuffix.length() > maxSuffixLength) {
            throw new IllegalArgumentException("Too large configured route suffix, need to be smaller than " + maxSuffixLength + " currently: " + routeNameSuffix.length() + " suffix is:" + routeNameSuffix);
        }
        this.routeNameSuffix = routeNameSuffix;
    }

    public void setPaasInstanceId(String paasInstanceId) {
        this.paasInstanceId = paasInstanceId;
    }


}