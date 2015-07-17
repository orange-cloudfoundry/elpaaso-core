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
package com.francetelecom.clara.cloud.presentation.tools;

import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.*;

public class EnvironmentDtoProvider extends SortableDataProvider<EnvironmentDto, String> {

	private static final long serialVersionUID = 1455084794375389713L;
	//private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(EnvironmentDtoProvider.class);
	
    private String searchCriteria;
    private List<EnvironmentDto> envList;
	private transient List<EnvironmentDto> filtered;
	
    public EnvironmentDtoProvider(String searchCriteria, List<EnvironmentDto> envList) {
        this.searchCriteria = searchCriteria;
		this.envList = envList;
        setSort(new SortParam<String>(Environment.CREATION_DATE, false));
    }

    
    @Override
    public Iterator<EnvironmentDto> iterator(long first, long count) {
        
        List<EnvironmentDto> filteredData = getFiltered();
        Collections.sort(filteredData, new Comparator<EnvironmentDto>() {
            @Override
            public int compare(EnvironmentDto o1, EnvironmentDto o2) {
            	int dir = getSort().isAscending() ? 1 : -1;
            	if ("label".equals(getSort().getProperty())) {
					return dir * (o1.getLabel().toUpperCase().compareTo(o2.getLabel().toUpperCase()));
				} else if ("applicationReleaseLabel".equals(getSort().getProperty())) {
					String releaseName1 = o1.getApplicationLabel() + " - " + o1.getReleaseVersion();
					String releaseName2 = o2.getApplicationLabel() + " - " + o2.getReleaseVersion();
					return dir * (releaseName1.toUpperCase().compareTo(releaseName2.toUpperCase()));
				} else if ("type".equals(getSort().getProperty())) {						
					return dir * (o1.getType().name().toUpperCase().compareTo(o2.getType().name().toUpperCase()));
				} else if ("ownerName".equals(getSort().getProperty())) {						
					return dir * (o1.getOwnerId().toUpperCase().compareTo(o2.getOwnerId().toUpperCase()));
				} else if ("status".equals(getSort().getProperty())) {						
					return dir * (o1.getStatus().name().toUpperCase().compareTo(o2.getStatus().name().toUpperCase()));
				} else {
					return dir * (o1.getCreationDate().compareTo(o2.getCreationDate()));
				} 
            	
            }
        });
 
		int f = new Long(first).intValue();
		int c = new Long(Math.min(first + count, filteredData.size())).intValue();
		return filteredData.subList(f, c).iterator();

    }

	
	@Override
	public long size() {
		return getFiltered().size();
	}


	@Override
	public IModel<EnvironmentDto> model(EnvironmentDto object) {
		return Model.of(object);
	}
	
	// sort + search field		
	private List<EnvironmentDto> getFiltered() {
		if (filtered == null) {
			filtered = filter();
		}				
		return filtered;
	}


	private List<EnvironmentDto> filter() {
		List<EnvironmentDto> filtered = new ArrayList<EnvironmentDto>(envList);
		if (searchCriteria != null && !searchCriteria.equalsIgnoreCase("")) {
			String upper = searchCriteria.toUpperCase();
			EnvironmentDto env = null;
			Iterator<EnvironmentDto> it = filtered.iterator();
			String releaseName = "";
			while (it.hasNext()) {
				env = it.next();	
				releaseName = env.getApplicationLabel() + " - " + env.getReleaseVersion();
				if (releaseName.toUpperCase().indexOf(upper) < 0
						&& env.getLabel().toUpperCase().indexOf(upper) < 0
						//&& env.getComment().toUpperCase().indexOf(upper) < 0
						&& env.getOwnerId().toUpperCase().indexOf(upper) < 0
						&& env.getType().name().toUpperCase().indexOf(upper) < 0
						&& env.getStatus().name().toUpperCase().indexOf(upper) < 0) {
					it.remove();
				}
			}
		}
		return filtered;
	}

	@Override
	public void detach() {
		filtered = null;
		super.detach();
	}


}