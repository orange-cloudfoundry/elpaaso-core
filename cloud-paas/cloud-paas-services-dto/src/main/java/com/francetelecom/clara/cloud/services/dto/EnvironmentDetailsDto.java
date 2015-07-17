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
package com.francetelecom.clara.cloud.services.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.francetelecom.clara.cloud.services.dto.LinkDto.LinkTypeEnum;


/**
 * Provides further details about an environment suiteable for a an app-ops team.
 */
public class EnvironmentDetailsDto extends EnvironmentDto {

    private static final long serialVersionUID = 1803974744056273295L;

    /**
     * Map of the different LinkDto with logicalModelItem.getName() as key and a list of associated links as values.
     */
    private Map<String, List<LinkDto>> linkDtoMap;

	/**
	 * Creates an EnvironmentDto which carry environment informations for presentation layer.
	 * @param uid environment internal uid
	 * @param internalName environment internal name
	 * @param label A human-readable name of the environment given as parameter
	 * @param applicationLabel The application label that is deployed on the environment
	 * @param releaseUID the UID of the application release deployed on the environment
	 * @param releaseVersion The application release version that is deployed on the environment
	 * @param ownerId The primary owner identifier of the environment
	 * @param ownerName The primary owner name of the environment
	 * @param creationDate Creation date of the environment
	 * @param type Environment type
	 * @param status Environment status
	 * @param statusMessage Environment status message (error)
	 * @param statusPercent Environment status progress in percentage, -1 if not set
	 * @param comment Environment comment
	 * @param tdiTdName TechnicalDeploymentInstance TDName
	 */
	public EnvironmentDetailsDto(String uid, String internalName, String label, String applicationLabel, String releaseUID, String releaseVersion, String ownerId, String ownerName, Date creationDate, EnvironmentTypeEnum type, EnvironmentStatusEnum status, String statusMessage, int statusPercent, String comment, String tdiTdName) {
		super(uid, internalName, label, applicationLabel, releaseUID, releaseVersion, ownerId, ownerName, creationDate, type, status, statusMessage, statusPercent, comment, tdiTdName);
        linkDtoMap = new HashMap<String, List<LinkDto>>();
	}

    public Map<String, List<LinkDto>> getLinkDtoMap() {
        return linkDtoMap;
    }

    public void setLinkDtoMap(Map<String, List<LinkDto>> linkDtoMap) {
        this.linkDtoMap = linkDtoMap;
    }

    @Override
	public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

	public URL getURLLinkFromType(LinkTypeEnum type) {
		if (linkDtoMap != null) {
		    List<LinkDto> linkDtosList = linkDtoMap.get(getTdiTdName());

		    if (linkDtosList != null) {
		        for (LinkDto linkDto : linkDtosList) {

		            if (linkDto.getLinkType() == type) {
		                return linkDto.getUrl();
		            }
		        }
		    }
		}
		return null;
	}
	
    public List<LinkDto> getSpecificLinkDto(LinkDto.LinkTypeEnum linkType) {

        List<LinkDto> specificListLinkDto = new ArrayList<LinkDto>();

        for (List<LinkDto> linkDtos : linkDtoMap.values()) {
            for (LinkDto link : linkDtos) {
                if (link.getLinkType() == linkType) {
                    specificListLinkDto.add(link);
                }
            }
        }

        return Collections.unmodifiableList(specificListLinkDto);
    }

    public LinkDto getEnvironmentOverallsLinkDto() {
        if (linkDtoMap == null) {
            return null;
        }
        List<LinkDto> linkDtosList = linkDtoMap.get(getTdiTdName());
        if (linkDtosList == null) {
            return null;
        }
        for (LinkDto linkDto : linkDtosList) {
            if (linkDto.getLinkType() == LinkDto.LinkTypeEnum.LOGS_LINK) {
                return linkDto;
            }
        }
        return null;
    }
}
