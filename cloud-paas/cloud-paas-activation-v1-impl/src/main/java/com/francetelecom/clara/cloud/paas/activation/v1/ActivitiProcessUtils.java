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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.google.common.base.Objects;

/**
 * Convenient methods to help activiti process factory.
 */
final class ActivitiProcessUtils {

	private static Logger logger = LoggerFactory.getLogger(ActivitiProcessUtils.class.getName());
	
	static class NodeTask {
		Set<NodeTask> dependOn = new HashSet<NodeTask>();
		Set<NodeTask> dependOnMe = new HashSet<NodeTask>();
		DependantModelItem item = null;
		String outputId = null;
		String inputId = null;

        @Override
        public String toString() {
            return Objects.toStringHelper(this)

                    // .add("dependOn", dependOn)
                    .add("dependOnMe", dependOnMe)
                    // .add("item", item)
                    //.add("outputId", outputId)
                    //.add("inputId", inputId)
                    .addValue("name="+(item != null ? item.getName():""))
                    .toString();
        }
    }

	static String generateId(String serviceName, NodeTask node) {
		return serviceName+"-"+node.item.hashCode()+"-si";
	}

	/**
	 * Creates a tree of nodes representing tasks in the process
	 * @param nodes Map of node: key is item name
	 * @param cache A map that is used as a cache
	 * @param item The entity to add to the tree
	 * @param canParrallel true if task can be paralellize (set to false if not sure)
	 * @param step The activation step
	 * @param pluginStrategy The plugin strategy
	 * @return A tree of nodes representing tasks in the process
	 */
	static Set<NodeTask> addService(Map<String, NodeTask> nodes, Map<String, Set<NodeTask>> cache, DependantModelItem item, boolean canParrallel, ActivationStepEnum step, ActivationPluginStrategy pluginStrategy) {
        if (item == null) {
            throw new TechnicalException("Unable to create activation step (tree node) from a null model item");
        }
        logger.trace("add service item {}={}", item.getClass().getSimpleName(), item);
		Set<NodeTask> returnNodes = cache.get(item.getName());
		if (returnNodes == null) {
			returnNodes = new HashSet<NodeTask>();
			boolean ignoreNode = pluginStrategy.getPlugin(item.getClass(), step) == null;
			if (ignoreNode) {
				for (DependantModelItem dependancy : item.listDepedencies()) {
					returnNodes.addAll(addService(nodes, cache, dependancy, canParrallel, step, pluginStrategy));
				}
			}
			else {
				// Create a node for this service
				NodeTask node = new NodeTask();
				node.item = item;
				
				NodeTask lastDependNode = null;
				int lastDependNodeIndex = -1;
				for (DependantModelItem dependancy : item.listDepedencies()) {
					// Go thru all items and get corresponding dependant nodes
					// We must do this first loop to be sure that all dependancies
					// have been inserted before computing the right place to insert our
					// node
					addService(nodes, cache, dependancy, canParrallel, step, pluginStrategy);
				}
				for (DependantModelItem dependancy : item.listDepedencies()) {
					// get corresponding dependant nodes (cache will be used)
					Set<NodeTask> dependantNodes = addService(nodes, cache, dependancy, canParrallel, step, pluginStrategy);
					for (NodeTask dependNode : dependantNodes) {
						// Check that this service is not us (it can be)
						if (!dependNode.item.equals(item)) {
							if (canParrallel) {
								// Inform the node that we depend on him
								dependNode.dependOnMe.add(node);
								// Add a dependancy to the service we depend on
								node.dependOn.add(dependNode);
							}
							else { 
								// Find position where to insert the node in the sequencial tree
								int dependNodeIndex = findIndexOf(nodes, dependNode);
								if (dependNodeIndex > lastDependNodeIndex) {
									lastDependNodeIndex = dependNodeIndex;
									lastDependNode = dependNode;
								}
							}
						}
					}
				}
				if (!canParrallel) {
					if (lastDependNode == null) {
						lastDependNode = findFirst(nodes);
					}
					if (lastDependNode != null) {
						// Note, invarriant is "for all node, dependOnMe.size() equals to 0 or 1 AND dependOn.size() equals to 0 or 1"
						if (lastDependNode.dependOnMe.size() == 0) {
							// Inform the node that we depend on him
							lastDependNode.dependOnMe.add(node);
							logger.debug("Insert "+node.item.getClass().getSimpleName()+"#"+node.item.getId()+"[name="+node.item.getName()+"] after "+lastDependNode.item.getClass().getSimpleName()+"#"+lastDependNode.item.getId());
						}
						else {
							// Insert the node between this current node and the dependOnMe node
							NodeTask other = lastDependNode.dependOnMe.iterator().next();
							lastDependNode.dependOnMe.clear();
							lastDependNode.dependOnMe.add(node);
							other.dependOn.clear();
							other.dependOn.add(node);
							node.dependOnMe.add(other);
							logger.debug("Insert "+node.item.getClass().getSimpleName()+"#"+node.item.getId()+"[name="+node.item.getName()+"] between "+lastDependNode.item.getClass().getSimpleName()+"#"+lastDependNode.item.getId()+" and "+other.item.getClass().getSimpleName()+"#"+other.item.getId());
						}
						// Add a dependancy to the service we depend on
						node.dependOn.add(lastDependNode);
					}
				}
                logger.trace("putting item{}={}", item.getClass().getSimpleName(), item);
                nodes.put(item.getName(), node);
				returnNodes.add(node);
			}
			cache.put(item.getName(), returnNodes);
		}
		return returnNodes;
	}
	
	/**
	 * Return position of the node into the tree which is sequencial (dependOn and dependOnMe have only 0 or 1 element)
	 * @param nodes List of nodes
	 * @param dependNode Node to position
	 * @return index of node (starts at zero)
	 */
	public static int findIndexOf(Map<String, NodeTask> nodes, NodeTask dependNode) {
		int index = 0;
		if (dependNode.dependOn.size() > 0) {
			index = 1 + findIndexOf(nodes, dependNode.dependOn.iterator().next());
		}
		logger.debug("Position of "+dependNode.item.getClass().getSimpleName()+"#"+dependNode.item.getId()+" => "+index);
		return index;
	}

	/**
	 * Return the first node of the sequential tree (dependOn and dependOnMe have only 0 or 1 element)
	 * @param nodes List of nodes
	 * @return The first node
	 */
	static NodeTask findFirst(Map<String, NodeTask> nodes) {
		NodeTask first = null;
		for (NodeTask node : nodes.values()) {
			if (node.dependOn.size() == 0) {
				first = node;
				break;
			}
		}
		return first;
	}
	
	public static void logSequence(Map<String, NodeTask> nodes, ActivationStepEnum step) {
        if (!logger.isDebugEnabled()) {
            return;
        }
		NodeTask first = findFirst(nodes);
		logger.debug("nodes sequence for step {} : {}", step, displayDependOnMe(first));
	}

	private static String displayDependOnMe(NodeTask node) {
        if (node == null) {
            return "";
        }
        String ret;
        if (node.item != null) {
		   ret = " -> " + node.item.getClass().getSimpleName() + "#" + node.item.getId();
        } else {
           ret = " -> (no item) ";
        }
		if (node.dependOnMe != null && node.dependOnMe.iterator().hasNext()) {
			ret += displayDependOnMe(node.dependOnMe.iterator().next());
		}
		return ret;
	}

	static String generateGatewayId(Collection<NodeTask> nodes) {
		StringBuffer id = new StringBuffer();
		for (NodeTask node : nodes) {
			id.append(node.item.hashCode());
			id.append("_");
		}
		return id.toString();
	}
	
	/**
	 * Remove useless gateway (1 incoming and 1 outgoing, replace by sequenceflow).
	 * Use activiti syntax for gateways (do not use outgoing and incoming tag, use sequenceflow).
	 * Replace subsequent sequenceflow by only one sequenceflow: A -> -> B become A -> B.
	 * @param process Process to simplify
	 * @param taskCount Total task count
	 */
	static void simplifyProcess(Process process, int taskCount) {
		HashMap<String, FlowElement> elements = new HashMap<String, FlowElement>();
		ListIterator<FlowElement> it;
		
		for (FlowElement flowElement: process.getFlowElements()) {
			elements.put(flowElement.getId(), flowElement);
		}
		
		it = ((List<FlowElement>) process.getFlowElements()).listIterator();
		while (it.hasNext()) {
			FlowElement flowElement = it.next();
			if (flowElement instanceof ParallelGateway) {
				ParallelGateway gw = (ParallelGateway) flowElement; 
				if (gw.getIncomingFlows().size() == 1 && gw.getOutgoingFlows().size() == 1) {
					// This can be replaced by a sequenceFlow tag
					SequenceFlow flow = new SequenceFlow();
					flow.setId(gw.getId());
					flow.setSourceRef(gw.getIncomingFlows().get(0).getSourceRef());
					flow.setTargetRef(gw.getOutgoingFlows().get(0).getTargetRef());
					if (gw.getIncomingFlows().get(0).getSourceRef().endsWith(ActivitiProcessFactory.NODE_SUFFIX_CONDITIONNAL)) {
						flow.setConditionExpression("${errCode == 0}");
					}
					// Remove old gateway and replace it by the new element
					it.set(flow);
				}
				else if (gw.getIncomingFlows().size() > 0 && gw.getOutgoingFlows().size() > 0) {
					// Activiti special notation: do not use outgoing and incoming tag, use sequenceflow
					ParallelGateway tParallelGateway = new ParallelGateway();
					tParallelGateway.setId(gw.getId());
					// Remove old gateway and replace it by the new element
					it.set(tParallelGateway);
					for (SequenceFlow in : gw.getIncomingFlows()) {
						SequenceFlow flow = new SequenceFlow();
						flow.setId("gw-"+in.toString()+"-"+gw.getId());
						flow.setSourceRef(in.getId());
						flow.setTargetRef(tParallelGateway.getId());
						if (in.toString().endsWith(ActivitiProcessFactory.NODE_SUFFIX_CONDITIONNAL)) {
							flow.setConditionExpression("${errCode == 0}");
						}
						it.add(flow);
					}
					for (SequenceFlow out : gw.getOutgoingFlows()) {
						SequenceFlow flow = new SequenceFlow();
						flow.setId("gw-"+gw.getId()+"-"+out.toString());
						flow.setSourceRef(tParallelGateway.getId());
						flow.setTargetRef(out.getId());
						it.add(flow);
					}
				}
			}
			else if (flowElement instanceof ServiceTask) {
				ServiceTask task = (ServiceTask) flowElement;
				task.setImplementation(task.getImplementation().replace(ActivitiProcessFactory.TASK_COUNT_KEY, String.valueOf(taskCount)));
			}

		}
		// Now, replace subsequent sequenceflow by only one sequenceflow
		// A -> -> B become A -> B
		boolean continueSimplification = true;
		while (continueSimplification) {
			continueSimplification = false;
			elements.clear();
			it = ((List<FlowElement>) process.getFlowElements()).listIterator();
			while (it.hasNext()) {
				FlowElement flowElement = it.next();
				if (flowElement instanceof SequenceFlow) {
					SequenceFlow flow = (SequenceFlow) flowElement;
					Assert.hasText(flow.getSourceRef(),"sourceRef is null flow="+flow.getId());
					FlowElement source = elements.get(flow.getSourceRef());
					if (source instanceof SequenceFlow) {
						// Invalid, must do a short cut
						((SequenceFlow)source).setTargetRef(flow.getTargetRef());
						it.remove();
						continueSimplification = true;
					}
				}
			}
		}
	}

}
