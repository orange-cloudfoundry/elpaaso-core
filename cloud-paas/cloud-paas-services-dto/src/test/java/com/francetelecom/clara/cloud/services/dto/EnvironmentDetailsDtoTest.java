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

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum;
import com.francetelecom.clara.cloud.services.dto.LinkDto.LinkTypeEnum;

public class EnvironmentDetailsDtoTest {

	private EnvironmentDetailsDto enviromentDetailsDto;

	@Before
	public void setup() throws MalformedURLException {
		LinkDto accessLink = new LinkDto();
		accessLink.setUrl(new URL("http://10.11.12.13"));
		accessLink.setLinkType(LinkTypeEnum.ACCESS_LINK);

		LinkDto metricsLink = new LinkDto();
		metricsLink.setUrl(new URL("http://13.12.11.10"));
		metricsLink.setLinkType(LinkTypeEnum.METRICS_LINK);

		LinkDto logsLink = new LinkDto();
		logsLink.setUrl(new URL("http://10.10.10.10"));
		logsLink.setLinkType(LinkTypeEnum.LOGS_LINK);
		
		List<LinkDto> linkList = new ArrayList<LinkDto>();
		linkList.addAll(Arrays.asList(accessLink, metricsLink));
		Map<String, List<LinkDto>> linkDtoMap = new HashMap<String, List<LinkDto>>();
		linkDtoMap.put("tdiName", linkList);
		linkDtoMap.put("someKey", Arrays.asList(logsLink));

		enviromentDetailsDto = new EnvironmentDetailsDto("name", "fabulous-seb_dev", "label", "applicationLabel", "releaseUID", "releaseVersion", "ownerId",
				"ownerName", new Date(), EnvironmentTypeEnum.DEVELOPMENT, EnvironmentStatusEnum.CREATED, "statusMessage", 100, "comment", "tdiName");
		enviromentDetailsDto.setLinkDtoMap(linkDtoMap);
	}

	@Test
	public void URL_should_be_fetched_from_type_and_tdi_name() throws MalformedURLException {
		// Given
		LinkTypeEnum type = LinkTypeEnum.ACCESS_LINK;

		// When
		URL urlLinkFromType = enviromentDetailsDto.getURLLinkFromType(type);

		// Then
		assertThat(urlLinkFromType).isEqualTo(new URL("http://10.11.12.13"));
	}
	
	@Test
	public void URL_should_be_null_if_no_link_matches_tdi_name() throws MalformedURLException {
		// Given
		LinkTypeEnum type = LinkTypeEnum.LOGS_LINK;

		// When
		URL urlLinkFromType = enviromentDetailsDto.getURLLinkFromType(type);

		// Then
		assertNull(urlLinkFromType);
	}
}
