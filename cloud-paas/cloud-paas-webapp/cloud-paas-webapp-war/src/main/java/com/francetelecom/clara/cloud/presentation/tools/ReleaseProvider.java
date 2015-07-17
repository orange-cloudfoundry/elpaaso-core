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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;

/**
 * Created by IntelliJ IDEA. User: Thomas Escalle - tawe8231 Entity :
 * FT/OLNC/RD/MAPS/MEP/MSE Date: 28/02/12
 */
public class ReleaseProvider extends SortableDataProvider<ApplicationRelease, String> {

	private static final long serialVersionUID = -5232260242289073148L;

	//private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ReleaseProvider.class);
	
	private String searchCriteria;
	private List<ApplicationRelease> releasesList;
	

	private transient List<ApplicationRelease> filtered;
	
	public ReleaseProvider(String searchCriteria, List<ApplicationRelease> releasesList) {
		this.searchCriteria = searchCriteria;
		this.releasesList = releasesList;
		setSort(new SortParam<String>("label", true));
	}

	
	@Override
	public Iterator<ApplicationRelease> iterator(long first, long count) {
		                		
		List<ApplicationRelease> filteredData = getFiltered();
		Collections.sort(filteredData, new Comparator<ApplicationRelease>() {
			@Override
			public int compare(ApplicationRelease o1, ApplicationRelease o2) {
				String sortField = getSort().getProperty();
				int dir = getSort().isAscending() ? 1 : -1;				
				if ("state".equals(sortField)) {
					return dir * (o1.getState().name().toUpperCase().compareTo(o2.getState().name().toUpperCase()));
				} else if ("description".equals(getSort().getProperty())) {	
					return dir * (o1.getDescription().toUpperCase().compareTo(o2.getDescription().toUpperCase()));
				} else {
					String release1 = o1.getApplication().getLabel() + " - " + o1.getReleaseVersion();
					String release2 = o2.getApplication().getLabel() + " - " + o2.getReleaseVersion();
					return dir * release1.toUpperCase().compareTo(release2.toUpperCase());
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
	public IModel<ApplicationRelease> model(ApplicationRelease object) {
		return Model.of(object);
	}
	
	// tri + search field		
	private List<ApplicationRelease> getFiltered() {
		if (filtered == null) {
			filtered = filter();
		}				
		return filtered;
	}

	private List<ApplicationRelease> filter() {
		List<ApplicationRelease> filtered = new ArrayList<ApplicationRelease>(releasesList);
		if (searchCriteria != null && !searchCriteria.equalsIgnoreCase("")) {
			String upper = searchCriteria.toUpperCase();
			Iterator<ApplicationRelease> it = filtered.iterator();
			ApplicationRelease ar = null;
			String label = "";
			while (it.hasNext()) {
				ar = it.next();
				label = ar.getApplication().getLabel() + " - " + ar.getReleaseVersion();
				if (label.toUpperCase().indexOf(upper) < 0
						&& ar.getState().name().toUpperCase().indexOf(upper) < 0
						&& ar.getDescription().toUpperCase().indexOf(upper) < 0
						//&& env.getComment().toUpperCase().indexOf(upper) < 0
						) {
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
