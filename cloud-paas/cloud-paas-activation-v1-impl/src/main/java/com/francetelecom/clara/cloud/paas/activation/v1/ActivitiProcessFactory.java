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
package com.francetelecom.clara.cloud.paas.activation.v1;

import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.XaasSubscription;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.francetelecom.clara.cloud.paas.activation.v1.ActivitiProcessUtils.NodeTask;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Activiti process factory
 */
public class ActivitiProcessFactory {

    private static Logger logger = LoggerFactory.getLogger(ActivitiProcessFactory.class.getName());

    static final String TASK_COUNT_KEY = "[TCK]";
    static final String NODE_SUFFIX_CONDITIONNAL = "-okornot";

    @Autowired
    private ActivationPluginStrategy pluginStrategy;

    private boolean canParrallel = false;

    protected Process createActivateProcess(final TechnicalDeploymentInstance tdi) throws JAXBException {

        // Creates the root element
        Process process = new Process();
        process.setId("TDI-" + tdi.getId() + "-" + System.currentTimeMillis());
        process.setName("Activate virtual applicance " + tdi.getTechnicalDeployment().getName());
        process.setExecutable(Boolean.TRUE);

        // Start node
        StartEvent start = new StartEvent();
        start.setId("start");
        process.addFlowElement(start);

        // End node
        EndEvent end = new EndEvent();
        end.setId("end");
        process.addFlowElement(end);

        ParallelGateway afterStartGw = new ParallelGateway();
        afterStartGw.setId("afterStartGw");
        afterStartGw.getIncomingFlows().add(new SequenceFlow(start.getId(), afterStartGw.getId()));

        ParallelGateway middleGw = new ParallelGateway();
        middleGw.setId("middleGw");

        ParallelGateway middleBisGw = new ParallelGateway();
        middleBisGw.setId("middleBisGw");

        ParallelGateway beforeEndGw = new ParallelGateway();
        beforeEndGw.setId("beforeEndGw");
        beforeEndGw.getOutgoingFlows().add(new SequenceFlow(beforeEndGw.getId(), end.getId()));

        int taskCount = generateProcessServerService(tdi, ActivationStepEnum.INIT.getName(), process, afterStartGw, middleGw, ActivationStepEnum.INIT, 1);

        taskCount += generateProcessServerService(tdi, ActivationStepEnum.ACTIVATE.getName(), process, middleGw, middleBisGw, ActivationStepEnum.ACTIVATE, taskCount + 1);

        taskCount += generateProcessServerService(tdi, ActivationStepEnum.FIRSTSTART.getName(), process, middleBisGw, beforeEndGw, ActivationStepEnum.FIRSTSTART, taskCount + 1);

        process.addFlowElement(afterStartGw);
        process.addFlowElement(beforeEndGw);
        process.addFlowElement(middleGw);
        process.addFlowElement(middleBisGw);

        ActivitiProcessUtils.simplifyProcess(process, taskCount);
        return process;
    }

    private Process createVappProcess(final TechnicalDeploymentInstance tdi, ActivationStepEnum processType) throws JAXBException {

        // Creates the root element
        //ProcessDefinition definitions = new ProcessDefinitionEntity();
        //definitions.setTargetNamespace("Activation");
        Process process = new Process();
        process.setId("TDI-" + tdi.getId() + "-" + System.currentTimeMillis());
        process.setName("Start virtual applicance " + tdi.getName());
        //definitions.getRootElement().add(factory.createProcess(process));

        // Start node
        StartEvent start = new StartEvent();
        start.setId("start");
        process.addFlowElement(start);

        // End node
        EndEvent end = new EndEvent();
        end.setId("end");
        process.addFlowElement(end);

        ParallelGateway afterStartGw = new ParallelGateway();
        afterStartGw.setId("afterStartGw");
        afterStartGw.getIncomingFlows().add(new SequenceFlow(start.getId(), afterStartGw.getId()));

        ParallelGateway beforeEndGw = new ParallelGateway();
        beforeEndGw.setId("beforeEndGw");
        beforeEndGw.getOutgoingFlows().add(new SequenceFlow(beforeEndGw.getId(), end.getId()));

        int taskCount;
        if (processType == ActivationStepEnum.ACTIVATE || processType == ActivationStepEnum.START) {
            taskCount = generateProcessServerService(tdi, processType.getName(), process, afterStartGw, beforeEndGw, processType, 1);
        } else {
            taskCount = generateProcessServerServiceReverse(tdi, processType.getName(), process, afterStartGw, beforeEndGw, processType, 1);
        }

        process.addFlowElement(afterStartGw);
        process.addFlowElement(beforeEndGw);

        ActivitiProcessUtils.simplifyProcess(process, taskCount);
        return process;
    }

    /**
     * @param processType
     * @param tdi
     * @return
     * @throws JAXBException
     * @throws IOException
     */
    Process generateProcessFromTDI(ActivationStepEnum processType, TechnicalDeploymentInstance tdi) throws JAXBException, IOException {

        // Creates the root element
        //TDefinitions definitions = factory.createTDefinitions();
        //definitions.setTargetNamespace("Activation");
        Process process;
        if (processType == ActivationStepEnum.ACTIVATE) {
            process = createActivateProcess(tdi);
        } else {
            process = createVappProcess(tdi, processType);
        }
        addBoundaryErrorEvent(tdi, process, processType);
        //definitions.getRootElement().add(factory.createProcess(process));
        /*
        writeProcess(definitions, outputFile);
        boolean dumpWorkflowXml = false;
        if (dumpWorkflowXml) {
            logger.debug("*** workflow bpmn20 XML :\n{}", FileUtils.readFileToString(outputFile));
        }
        logger.debug("*** workflow id=" + process.getId() + " is done***");
        return process.getId();
        */
        return process;
    }

    /**
     * Generate BPMN 2.0 XML
     * @param definitions Java representation of process
     * @param outputFile File to write
     */
  /*
    void writeProcess(TDefinitions definitions, File outputFile) throws JAXBException {
        ObjectFactory factory = new ObjectFactory();
        JAXBContext jc = JAXBContext.newInstance( "com.francetelecom.clara.cloud.paas.activation.bpmn20" );
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal( factory.createDefinitions(definitions), outputFile);
    }
*/

    /**
     * Transform the process into a subprocess so errors can be catched and redirected to
     * a failure task.
     *
     * @param tdi     The TDI
     * @param process The process
     */
    void addBoundaryErrorEvent(final TechnicalDeploymentInstance tdi, Process process, ActivationStepEnum processType) {

        // Subprocess
        SubProcess subprocess = new SubProcess();
        subprocess.setId("sub-" + process.getId());
        subprocess.getFlowElements().addAll(process.getFlowElements());
        process.getFlowElements().clear();
        process.addFlowElement(subprocess);

        // Start node
        StartEvent start = new StartEvent();
        start.setId("startglobal");
        process.addFlowElement(start);

        // End node
        EndEvent end = new EndEvent();
        end.setId("endglobal");
        process.addFlowElement(end);

        // End error node
        EndEvent enderror = new EndEvent();
        enderror.setId("enderror");
        process.addFlowElement(enderror);

        // Boundary (catch)
        BoundaryEvent boundary = new BoundaryEvent();
        boundary.setId("catch-error");
        boundary.setAttachedToRefId(subprocess.getId());
        boundary.setAttachedToRef(subprocess);
        ErrorEventDefinition failureEvent = new ErrorEventDefinition();
        failureEvent.setErrorCode("failure");
        boundary.getEventDefinitions().add(failureEvent);
        process.addFlowElement(boundary);

        // Create the service task that manage the failure
        ServiceTask failureTask = new ServiceTask();
        failureTask.setId("failureTask");
        failureTask.setName("Process failure");
        //failureTask.setExpression("#{wrapper.execute(activationStep, '"+failureTask.getId()+"', entityId, entityClass, errMessage)}");
        failureTask.setImplementation("#{wrapper.failed(execution.processInstanceId, '" + processType.getName() + "', '" + tdi.getId() + "', errMessage)}");
        failureTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
        process.addFlowElement(failureTask);

        // Create the service task that manage the success
        ServiceTask successTask = new ServiceTask();
        successTask.setId("successTask");
        successTask.setName("Process success");
        successTask.setImplementation("#{wrapper.success(execution.processInstanceId, '" + processType.getName() + "', '" + tdi.getId() + "')}");
        successTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
        process.addFlowElement(successTask);

        // Link : start -> subprocess
        SequenceFlow start2sub = new SequenceFlow();
        start2sub.setId("start2sub");
        start2sub.setSourceRef(start.getId());
        start2sub.setTargetRef(subprocess.getId());
        process.addFlowElement(start2sub);

        // Link : subprocess -> successTask
        SequenceFlow sub2successTask = new SequenceFlow();
        sub2successTask.setId("sub2successTask");
        sub2successTask.setSourceRef(subprocess.getId());
        sub2successTask.setTargetRef(successTask.getId());
        process.addFlowElement(sub2successTask);

        // Link : successTask -> end
        SequenceFlow successTask2end = new SequenceFlow();
        successTask2end.setId("successTask2end");
        successTask2end.setSourceRef(successTask.getId());
        successTask2end.setTargetRef(end.getId());
        process.addFlowElement(successTask2end);

        // Link : boundary -> failureTask
        SequenceFlow catch2failed = new SequenceFlow();
        catch2failed.setId("catch2failed");
        catch2failed.setSourceRef(boundary.getId());
        catch2failed.setTargetRef(failureTask.getId());
        process.addFlowElement(catch2failed);

        // Link : failureTask -> endError
        SequenceFlow failed2end = new SequenceFlow();
        failed2end.setId("failed2end");
        failed2end.setSourceRef(failureTask.getId());
        failed2end.setTargetRef(enderror.getId());
        process.addFlowElement(failed2end);
    }

    private int generateProcessServerService(final TechnicalDeploymentInstance tdi, String serviceName, Process process, ParallelGateway startGW, ParallelGateway endGW, ActivationStepEnum activationStep, int taskStartIndex) throws JAXBException {

        // Compute Server dependancies
        Map<String, NodeTask> nodes = new HashMap<String, NodeTask>();
        Map<String, Set<NodeTask>> cache = new HashMap<String, Set<NodeTask>>();
        for (XaasSubscription subs : tdi.getTechnicalDeployment().listXaasSubscriptionTemplates()) {
            ActivitiProcessUtils.addService(nodes, cache, subs, canParrallel, activationStep, pluginStrategy);
        }

        ActivitiProcessUtils.logSequence(nodes, activationStep);

        int index = taskStartIndex;
        for (NodeTask node : nodes.values()) {
            generateTask(tdi, serviceName, process, node, false, index);
            index++;
        }

        HashMap<String, FlowElement> elements = new HashMap<String, FlowElement>();
        for (FlowElement flowElement : process.getFlowElements()) {
            elements.put(flowElement.getId(), flowElement);
        }

        for (NodeTask node : nodes.values()) {
            for (NodeTask nextNode : node.dependOnMe) {
                SequenceFlow link = new SequenceFlow();
                link.setId(node.outputId + "-to-" + nextNode.inputId);
                link.setSourceRef(elements.get(node.outputId).getId());
                link.setTargetRef(elements.get(nextNode.inputId).getId());
                if (node.outputId.endsWith(NODE_SUFFIX_CONDITIONNAL)) {
                    link.setConditionExpression("${errCode == 0}");
                }
                process.addFlowElement(link);
            }
            if (node.dependOn.size() == 0) {
                // There is no dependancy so link to the start node
                startGW.getOutgoingFlows().add(new SequenceFlow(startGW.getId(), node.inputId));
            }
            if (node.dependOnMe.size() == 0) {
                // No server depends on this server so link to the end node
                endGW.getIncomingFlows().add(new SequenceFlow(node.outputId, endGW.getId()));
            }
        }
        return nodes.size();
    }

    private int generateProcessServerServiceReverse(final TechnicalDeploymentInstance tdi, String serviceName, Process process, ParallelGateway startGW, ParallelGateway endGW, ActivationStepEnum activationStep, int taskStartIndex) throws JAXBException {

        // Compute Server dependancies
        HashMap<String, NodeTask> nodes = new HashMap<String, NodeTask>();
        Map<String, Set<NodeTask>> cache = new HashMap<String, Set<NodeTask>>();
        for (XaasSubscription subs : tdi.getTechnicalDeployment().listXaasSubscriptionTemplates()) {
            ActivitiProcessUtils.addService(nodes, cache, subs, canParrallel, activationStep, pluginStrategy);
        }

        ActivitiProcessUtils.logSequence(nodes, activationStep);

        int index = taskStartIndex;
        for (NodeTask node : nodes.values()) {
            generateTask(tdi, serviceName, process, node, true, index);
            index++;
        }

        for (NodeTask node : nodes.values()) {
            for (NodeTask nextNode : node.dependOn) {
                SequenceFlow link = new SequenceFlow();
                link.setId(node.outputId + "-to-" + nextNode.inputId);
                link.setSourceRef(node.outputId);
                link.setTargetRef(nextNode.inputId);
                if (nextNode.outputId.endsWith(NODE_SUFFIX_CONDITIONNAL)) {
                    link.setConditionExpression("${errCode == 0}");
                }
                process.addFlowElement(link);
            }
            if (node.dependOnMe.size() == 0) {
                // There is no dependancy so link to the start node
                startGW.getOutgoingFlows().add(new SequenceFlow(startGW.getId(), node.inputId));
            }
            if (node.dependOn.size() == 0) {
                // No server depends on this server so link to the end node
                endGW.getIncomingFlows().add(new SequenceFlow(node.outputId, endGW.getId()));
            }
        }
        return nodes.size();
    }

    private void generateTask(final TechnicalDeploymentInstance tdi, String serviceName, Process process, NodeTask node, boolean reverse, int indexTask) {
        logger.debug("Generating task " + serviceName + " for node " + node.item.getClass().getSimpleName() + "#" + node.item.getId() + " in=" + node.dependOn.size() + "out=" + node.dependOnMe.size());

        // Create the service task
        ServiceTask task = new ServiceTask();
        task.setId(ActivitiProcessUtils.generateId(serviceName, node));
        task.setName(serviceName);
        // Item task
        task.setName(serviceName + " " + node.item.getClass().getSimpleName());
        task.setImplementation("${wrapper.execute(execution.processInstanceId, '" + serviceName + "', '" + task.getId() + "', " + tdi.getId() + ", " + node.item.getId() + ", '" + node.item.getClass().getName() + "', " + indexTask + ", " + TASK_COUNT_KEY + ")}");
        task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);

        process.addFlowElement(task);

        // Create the task that will wait until task is finished correctly or not
        ReceiveTask receiveTask = new ReceiveTask();
        receiveTask.setId(task.getId() + "-end");
        process.addFlowElement(receiveTask);

        // Link : ServiceTask -> ReceiveTask
        SequenceFlow task2wait = new SequenceFlow();
        task2wait.setId(task.getId() + "2wait");
        task2wait.setSourceRef(task.getId());
        task2wait.setTargetRef(receiveTask.getId());
        process.addFlowElement(task2wait);

        // ExclusiveGateway : success or error?
        ExclusiveGateway succesOrError = new ExclusiveGateway();
        succesOrError.setId(task.getId() + NODE_SUFFIX_CONDITIONNAL);
        process.addFlowElement(succesOrError);

        // Link : ReceiveTask -> ExclusiveGateway
        SequenceFlow wait2test = new SequenceFlow();
        wait2test.setId(task.getId() + "2test");
        wait2test.setSourceRef(receiveTask.getId());
        wait2test.setTargetRef(succesOrError.getId());
        process.addFlowElement(wait2test);

        // End node (error)
        EndEvent endError = new EndEvent();
        endError.setId(task.getId() + "-error-event");
        ErrorEventDefinition errEvent = new ErrorEventDefinition();
        errEvent.setErrorCode("failure");
        endError.addEventDefinition(errEvent);
        process.addFlowElement(endError);

        // Link : error
        SequenceFlow errorLink = new SequenceFlow();
        errorLink.setId(task.getId() + "-ko-link");
        errorLink.setSourceRef(succesOrError.getId());
        errorLink.setTargetRef(endError.getId());
        errorLink.setConditionExpression("${errCode > 0}");
        process.addFlowElement(errorLink);

        boolean multipleInput = node.dependOn.size() > 1;
        boolean multipleOutput = node.dependOnMe.size() > 1;
        if (reverse) {
            multipleInput = node.dependOnMe.size() > 1;
            multipleOutput = node.dependOn.size() > 1;
        }
        if (multipleOutput) {
            // Parallele gateway : success
            ParallelGateway succesNode = new ParallelGateway();
            succesNode.setId(task.getId() + "-ok");
            process.addFlowElement(succesNode);
            // Link: success (with multiple output nodes)
            SequenceFlow successLink = new SequenceFlow();
            successLink.setId(task.getId() + "-ok-link");
            successLink.setSourceRef(succesOrError.getId());
            successLink.setTargetRef(succesNode.getId());
            successLink.setConditionExpression("${errCode == 0}");
            process.addFlowElement(successLink);
            // Set the outputId of the node, here the parallele gateway
            node.outputId = succesNode.getId();
        } else {
            // Set the outputId of the node, here the exclusive gateway
            node.outputId = succesOrError.getId();
        }
        if (multipleInput) {
            // Parallele gateway : input
            ParallelGateway inputNode = new ParallelGateway();
            inputNode.setId(task.getId() + "-in");
            process.addFlowElement(inputNode);
            // Link: in -> task (with multiple input nodes)
            SequenceFlow inputLink = new SequenceFlow();
            inputLink.setId(task.getId() + "-in-link");
            inputLink.setSourceRef(inputNode.getId());
            inputLink.setTargetRef(task.getId());
            process.addFlowElement(inputLink);
            // Set the inputId of the node, here the parallele gateway
            node.inputId = inputNode.getId();
        } else {
            // Set the inputId of the node, here the service task
            node.inputId = task.getId();
        }
    }

    public void setCanParrallel(boolean canParrallel) {
        this.canParrallel = canParrallel;
    }

}
