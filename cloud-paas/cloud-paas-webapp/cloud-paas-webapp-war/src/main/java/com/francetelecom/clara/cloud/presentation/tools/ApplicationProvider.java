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

import com.francetelecom.clara.cloud.coremodel.Application;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 20/02/12
 */

public class ApplicationProvider extends SortableDataProvider<Application, String> {

    private static final long serialVersionUID = -607185222587506578L;

	private String searchCriteria;
	private List<Application> applicationsList;
	private transient List<Application> filtered;


    public ApplicationProvider(String searchCriteria, List<Application> applicationsList) {
    	this.searchCriteria = searchCriteria;
		this.applicationsList = applicationsList;
        setSort(new SortParam<String>("label", true));
    }

    @Override
    public Iterator<Application> iterator(long first, long count) {
    	
    	List<Application> filteredData = getFiltered();
		Collections.sort(filteredData, new Comparator<Application>() {
			@Override
			public int compare(Application o1, Application o2) {
				String sortField = getSort().getProperty();
				int dir = getSort().isAscending() ? 1 : -1;				
				if ("label".equals(sortField)) {
					return dir * (o1.getLabel().toUpperCase().compareTo(o2.getLabel().toUpperCase()));
				} else if ("code".equals(sortField)) {
					return dir * (o1.getCode().toUpperCase().compareTo(o2.getCode().toUpperCase()));
				} else if ("description".equals(getSort().getProperty())) {	
					return dir * (o1.getDescription().toUpperCase().compareTo(o2.getDescription().toUpperCase()));
				} else {
					return dir * Boolean.compare(o1.isPublic(), o2.isPublic());
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
	public IModel<Application> model(Application object) {
		return Model.of(object);
	}
	
	// sort + search field		
	private List<Application> getFiltered() {
		if (filtered == null) {
			filtered = filter();
		}				
		return filtered;
	}

	private List<Application> filter() {
		List<Application> filtered = new ArrayList<Application>(applicationsList);
		if (searchCriteria != null && !searchCriteria.equalsIgnoreCase("")) {
			String upper = searchCriteria.toUpperCase();
			Iterator<Application> it = filtered.iterator();
			Application app = null;
			while (it.hasNext()) {
				app = it.next();
				if (app.getLabel().toUpperCase().indexOf(upper) < 0
						&& app.getCode().toUpperCase().indexOf(upper) < 0
						&& app.getDescription().toUpperCase().indexOf(upper) < 0
						//&& app.getComment().toUpperCase().indexOf(upper) < 0
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
