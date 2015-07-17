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
package com.francetelecom.clara.cloud.environment.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.util.Assert;

import com.francetelecom.clara.cloud.core.service.SecurityUtils;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum;

public class EnvironmentMapper {

	private EnvironmentDto toEnvironmentDto(Environment environment, boolean writable) {
		String environmentUid = environment.getUID();
		String environmentInternalName = environment.getInternalName();
		String environmentLabel = environment.getLabel();
		String applicationLabel = environment.getApplicationRelease().getApplication().getLabel();
		String releaseUid = environment.getApplicationRelease().getUID();
		String releaseVersion = environment.getApplicationRelease().getReleaseVersion();
		String ownerSsoid = environment.getPaasUser().getSsoId().getValue();
		String ownerFirstName = environment.getPaasUser().getFirstName();
		Date environmentCreationDate = environment.getCreationDate();
		EnvironmentTypeEnum environmentType = EnvironmentTypeEnum.valueOf(environment.getType().name());
		EnvironmentStatusEnum environmentStatus = EnvironmentStatusEnum.valueOf(environment.getStatus().name());
		String environmentStatusMessage = environment.getStatusMessage();
		int environmentStatusPercent = environment.getStatusPercent();
		String environmentComment = environment.getComment();
		String technicalDeploymentUid = environment.getTechnicalDeploymentInstance().getTechnicalDeployment().getName();

		EnvironmentDto dto = new EnvironmentDto(environmentUid, environmentInternalName, environmentLabel, applicationLabel, releaseUid, releaseVersion, ownerSsoid,
				ownerFirstName, environmentCreationDate, environmentType, environmentStatus, environmentStatusMessage, environmentStatusPercent,
				environmentComment, technicalDeploymentUid);
		
		dto.setEditable(writable);
		
		return dto;
	}
	
	public EnvironmentDto toEnvironmentDto(Environment environment) {
		Assert.notNull(environment,"cannot convert environment. Environment <"+environment+"> is not valid");
		return toEnvironmentDto(environment, SecurityUtils.hasWritePermissionFor(environment));
	}

	public List<EnvironmentDto> toEnvironmentDtoList(List<Environment> environments) {
		List<EnvironmentDto> dtos = new ArrayList<EnvironmentDto>();
		if (environments != null) {
			for (Environment environment : environments) {
				dtos.add(toEnvironmentDto(environment, SecurityUtils.hasWritePermissionFor(environment)));
			}
		}
		return dtos;
	}

}
