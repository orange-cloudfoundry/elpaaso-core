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
package com.francetelecom.clara.cloud.presentation.designer.panels;

import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigServiceUtils;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.presentation.common.AjaxFallbackCustomDataTable;
import com.francetelecom.clara.cloud.presentation.common.PageTemplate;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 15/11/11
 * Time: 14:29
 * To change this template use File | Settings | File Templates.
 *
 * Panel which sums up the config entries used in the logical deployment
 */
public class DesignerArchitectureConfigSetPanel extends Panel {

    /**
     * Logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(DesignerArchitectureConfigSetPanel.class);

    /**
     * DesignerHelperPage parentPage
     */
    private DesignerHelperPage parentPage;

    private List<DetailedConfigEntry> entries;

    private final static LogicalConfigServiceUtils utils = new LogicalConfigServiceUtils();

    public boolean hasError = false;

    public DesignerArchitectureConfigSetPanel(String id, DesignerHelperPage parentPage, PageParameters params) {
        super(id);

        this.parentPage = parentPage;

        initComponents();
    }

    private StringResourceModel getStringResourceModel(String key) {
        // BVA fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
        return new StringResourceModel(key, this, null);
    }

    private void initComponents() {

        entries = new ArrayList<DetailedConfigEntry>();

        createConfigEntryList();

        List<IColumn<DetailedConfigEntry, String>> columns = new ArrayList<IColumn<DetailedConfigEntry, String>>();

        columns.add(new PropertyColumn<DetailedConfigEntry, String>(getStringResourceModel("portal.designer.config.summary.header.execNodeName"), "execNodeName"));
        columns.add(new PropertyColumn<DetailedConfigEntry, String>(getStringResourceModel("portal.designer.config.summary.header.configSetName"), "configSetName"));
        columns.add(new PropertyColumn<DetailedConfigEntry, String>(getStringResourceModel("portal.designer.config.summary.header.key"), "key"));
        columns.add(new PropertyColumn<DetailedConfigEntry, String>(getStringResourceModel("portal.designer.config.summary.header.value"), "value"));

        columns.add(new PropertyColumn<DetailedConfigEntry, String>(getStringResourceModel("portal.designer.config.summary.header.comment"), "comment") {
            private static final long serialVersionUID = 1321984147078227956L;
            @Override
            public void populateItem(Item<ICellPopulator<DetailedConfigEntry>> item, String componentId, IModel<DetailedConfigEntry> rowModel) {
                MultiLineLabel label = new MultiLineLabel(componentId, getDataModel(rowModel));
                label.add(new AttributeAppender("class", "preserve_whitespace"));
                item.add(label);
            }
        });

        AjaxFallbackCustomDataTable<DetailedConfigEntry, String> table = new AjaxFallbackCustomDataTable<>("configList", columns, new ConfigEntryProvider(), PageTemplate.ROWS_PER_PAGE);

        add(table);

        if (entries.size() == 0) {
            this.setVisible(false);
        }

    }

    /**
     * Creates set list of config entries (with their detail : config set and logical execution node related)
     * @throws com.francetelecom.clara.cloud.commons.BusinessException exception raised when constraints (e.g. duplicate keys) are not fulfilled
     */
    private void createConfigEntryList() {
        logger.debug("creating config entry list");
        entries.clear();

        for (ProcessingNode node : parentPage.getLogicalDeployment().listProcessingNodes()) {

            for (LogicalConfigService service : node.listLogicalServices(LogicalConfigService.class)) {
                String tmpContent = service.getConfigSetContent();
                LogicalConfigServiceUtils.StructuredLogicalConfigServiceContent structuredContent = null;
                try {
                    structuredContent = utils.parseConfigContent(tmpContent);
                } catch (InvalidConfigServiceException e) {
                    hasError = true;
                }
                if (structuredContent != null) {
                    for (LogicalConfigServiceUtils.ConfigEntry entry : structuredContent.getConfigEntries()) {
                        DetailedConfigEntry detailedConfigEntry = new DetailedConfigEntry();
                        // We delete the # of the comment, if the comment exists
                        String comment = entry.getComment() != null ? entry.getComment() : "";
                        detailedConfigEntry.setComment(comment);
                        detailedConfigEntry.setKey(getConfigEntryFullKey(service, entry));
                        detailedConfigEntry.setValue(entry.getValue());
                        detailedConfigEntry.setConfigSetName(service.getLabel());
                        detailedConfigEntry.setExecNodeName(node.getLabel());
                        entries.add(detailedConfigEntry);
                    }
                }
            }
        }
    }

	/**
	 * build the full name a config key by prefixing the key with the jndiPrefix
	 * @param service
	 * @param entry
	 * @return
	 */
	protected String getConfigEntryFullKey(LogicalConfigService service,
			LogicalConfigServiceUtils.ConfigEntry entry) {
        if (service.getKeyPrefix() == null) {
            return entry.getKey();
		} else {
            return service.getKeyPrefix() + entry.getKey();
        }
	}

    /**
     * Provider for the AjaxFallbackDefaultDataTable
     */
    class ConfigEntryProvider extends SortableDataProvider<DetailedConfigEntry, String> {

        private static final long serialVersionUID = -607185222587506578L;

        public ConfigEntryProvider() {
            setSort("key", SortOrder.ASCENDING);
        }

        @Override
        public Iterator<DetailedConfigEntry> iterator(long first, long count) {
            long realCount = count < entries.size() - first ? count : entries.size() - first;
            return entries.subList((int) first, (int) (first + realCount)).iterator();
        }

        @Override
        public long size() {
            return entries.size();
        }

        @Override
        public IModel<DetailedConfigEntry> model(DetailedConfigEntry object) {
            return new Model<DetailedConfigEntry>(object);
        }
    }


    /**
     * ConfigEntry, with details on its JeeProcessing and on its LogicalConfigService (only name is needed)
     */
    class DetailedConfigEntry implements Serializable {

        private static final long serialVersionUID = -1626944421388088928L;

        private String comment;
        private String key;
        private String value;
        private String configSetName;
        private String execNodeName;

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getConfigSetName() {
            return configSetName;
        }

        public void setConfigSetName(String configSetName) {
            this.configSetName = configSetName;
        }

        public String getExecNodeName() {
            return execNodeName;
        }

        public void setExecNodeName(String execNodeName) {
            this.execNodeName = execNodeName;
        }
    }
}
