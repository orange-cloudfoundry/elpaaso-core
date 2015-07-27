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
package com.francetelecom.clara.cloud.deployment.logical.service;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalNodeServiceAssociation;
import com.francetelecom.clara.cloud.logicalmodel.LogicalService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class LogicalDeploymentImportExport {

    private LogicalDeploymentClonerImpl logicalDeploymentCloner;

    public String exportDump(LogicalDeployment logicalDeployment) {
        //when I request to dump as Xml
        return logicalDeploymentCloner.dumpToXml(logicalDeployment);
    }

    public void importDump(LogicalDeployment originalLd, String dump) {
        LogicalDeployment importedLd = logicalDeploymentCloner.preprocessAndImportDump(dump);

        renameDuplicatesLogicalItems(originalLd, importedLd);

        //Note: we can't use a set since Association.equals() only care about ExecNodes and ignore services.
        // As a result, it'd truncate some services.
        //Since we're using a list, we might end up with some duplicates we'll have to handle next.
        List<LogicalNodeServiceAssociation> associations = new ArrayList<>();

        collectAndRemoveAssociations(importedLd, associations);

        removeNodesAndServices(importedLd);

        addNodesServicesAssociations(originalLd, associations);
    }

    private void removeNodesAndServices(LogicalDeployment importedLd) {
        try {
            importedLd.removeAllLogicalService();
        } catch (BusinessException e) {
            throw new TechnicalException("Unexpected failed to remove all services: " + e, e);
        }
        importedLd.removeAllProcessingNodes();
    }

    private void collectAndRemoveAssociations(LogicalDeployment importedLd, List<LogicalNodeServiceAssociation> associations) {
        List<LogicalService> logicalServices = new ArrayList<>(importedLd.listLogicalServices()); //clone the list to be able to modify it
        for (LogicalService logicalService : logicalServices) {
            while (!logicalService.listLogicalServicesAssociations().isEmpty()) {
                LogicalNodeServiceAssociation association = logicalService.listLogicalServicesAssociations().get(0);
                associations.add(association);
ProcessingNode executionNode = association.getProcessingNode();
                executionNode.removeLogicalServiceUsage(association);
            }
        }
    }

    protected void addNodesServicesAssociations(LogicalDeployment originalLd, List<LogicalNodeServiceAssociation> associations) {
        for (LogicalNodeServiceAssociation association : associations) {
        	ProcessingNode executionNode = association.getProcessingNode();
            LogicalService service = association.getLogicalService();

            try {
                originalLd.addExecutionNode(executionNode);
            } catch (Exception e) {
                //Ignore duplicate exec nodes
            }
            try {
                originalLd.addLogicalService(service);
            } catch (Exception e) {
                //Ignore duplicate services
            }

            if (!executionNode.listLogicalServices().contains(service)) {
                executionNode.addLogicalServiceUsage(service, association.getAccessType());
            }
        }
    }


    public void setLogicalDeploymentCloner(LogicalDeploymentClonerImpl logicalDeploymentCloner) {
        this.logicalDeploymentCloner = logicalDeploymentCloner;
    }

    public void renameDuplicatesLogicalItems(LogicalDeployment original, LogicalDeployment duplicate) {
        for (ProcessingNode candidateExecNode : duplicate.listProcessingNodes()) {
            renameDuplicateExecNode(original, candidateExecNode);
        }
        for (LogicalService candidateService : duplicate.listLogicalServices()) {
            renameDuplicateService(original, candidateService);
        }
    }

    private void renameDuplicateService(LogicalDeployment original, LogicalService candidateService) {
        boolean isDuplicate;
        int suffixIndex=2;
        String originalLabel = candidateService.getLabel();
        do {
            try {
                original.findLogicalService(candidateService.getLabel());
                isDuplicate = true;
                candidateService.setLabel(originalLabel + "-" + suffixIndex);
                suffixIndex ++;
            } catch (IllegalArgumentException e) {
                isDuplicate = false;
            }
        } while(isDuplicate);
    }

    private void renameDuplicateExecNode(LogicalDeployment original, ProcessingNode candidateExecNode) {
        boolean isDuplicate;
        int suffixIndex=2;
        String originalLabel = candidateExecNode.getLabel();
        do {
            try {
                original.findProcessingNode(candidateExecNode.getLabel());
                isDuplicate = true;
                candidateExecNode.setLabel(originalLabel + "-" + suffixIndex);
                suffixIndex ++;
            } catch (IllegalArgumentException e) {
                isDuplicate = false;
            }
        } while (isDuplicate);
    }
}
