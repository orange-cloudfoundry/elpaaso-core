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
package com.francetelecom.clara.cloud.logicalmodel;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class LogicalDeploymentTest {

	LogicalDeployment ld = new LogicalDeployment();

    @Test
    public void findLogicalServiceByLabel_returns_requested_instance() {
        //given
        LogicalService sa1 = new LogicalServiceTestTypeA(ld, "service 1");
        LogicalService sa2 = new LogicalServiceTestTypeA(ld, "service 2");
        LogicalService sa3 = new LogicalServiceTestTypeA(ld, "service 3");

        //when
        LogicalService service = ld.findLogicalService("service 3");

        assertThat(service).isEqualTo(sa3);

    }
    @Test(expected = IllegalArgumentException.class)
    public void findLogicalServiceByLabel_throws_IAE_if_requested_instance_missing() {
        //given an empty ld

        //when
        LogicalService service = ld.findLogicalService("service 3");
    }

	@Test
	public void list_logical_services_by_type_should_be_sorted_by_subtype_and_label() {
	
		LogicalService sb2 = new LogicalServiceTestTypeB(ld, "zz service");
		LogicalService sb1 = new LogicalServiceTestTypeB(ld, "aa service");
		LogicalService sa2 = new LogicalServiceTestTypeA(ld, "service 2");
		LogicalService sa3 = new LogicalServiceTestTypeA(ld, "service 3");
		LogicalService sa1 = new LogicalServiceTestTypeA(ld, "service 1");
		
		Set<LogicalServiceTest> serviceServices = ld.listLogicalServices(LogicalServiceTest.class);

		// we need to convert returned set as a list to assert list are equal
		List<Object> servicesList = Arrays.asList(serviceServices.toArray());
		assertThat(servicesList).isEqualTo(Arrays.asList(sa1, sa2, sa3, sb1, sb2));
	}
	
	@Test
	public void list_logical_services_should_be_sorted_by_type_and_label() {
		
		LogicalService sb2 = new LogicalServiceTestTypeB(ld, "zz service");
		LogicalService sb1 = new LogicalServiceTestTypeB(ld, "aa service");		
		LogicalService sa2 = new LogicalServiceTestTypeA(ld, "service 2");
		LogicalService sa3 = new LogicalServiceTestTypeA(ld, "service 3");
		LogicalService sa1 = new LogicalServiceTestTypeA(ld, "service 1");

		List<LogicalService> services = ld.listLogicalServices();
		
		assertThat(services).isEqualTo(Arrays.asList(sa1,sa2, sa3, sb1, sb2));
	}
	
	@Test
	public void list_execution_nodes_should_be_sorted_by_type_and_label() {
		
		ProcessingNode execNodeB2 = new LogicalExecutionNodeTestTypeB(ld, "zz node");
		ProcessingNode execNodeB1 = new LogicalExecutionNodeTestTypeB(ld, "aa node");		
		ProcessingNode execNodeA2 = new LogicalExecutionNodeTestTypeA(ld, "node 2");
		ProcessingNode execNodeA3 = new LogicalExecutionNodeTestTypeA(ld, "node 3");
		ProcessingNode execNodeA1 = new LogicalExecutionNodeTestTypeA(ld, "node 1");

		List<ProcessingNode> processingNodes = ld.listProcessingNodes();
		
		assertThat(processingNodes).isEqualTo(Arrays.asList(execNodeA1,execNodeA2, execNodeA3, execNodeB1, execNodeB2));
	}

	@Test
	public void list_execution_nodes_by_type_should_be_sorted_by_subtype_and_label() {
		ProcessingNode execNodeB2 = new LogicalExecutionNodeTestTypeB(ld, "zz node");
		ProcessingNode execNodeB1 = new LogicalExecutionNodeTestTypeB(ld, "aa node");		
		ProcessingNode execNodeA2 = new LogicalExecutionNodeTestTypeA(ld, "node 2");
		ProcessingNode execNodeA3 = new LogicalExecutionNodeTestTypeA(ld, "node 3");
		ProcessingNode execNodeA1 = new LogicalExecutionNodeTestTypeA(ld, "node 1");

		List<LogicalExecutionNodeTest> services = ld.listProcessingNodes(LogicalExecutionNodeTest.class);
		
		assertThat(services).isEqualTo(Arrays.asList(execNodeA1,execNodeA2, execNodeA3, execNodeB1, execNodeB2));
	}

	// LogicalService test classes
	
	class LogicalServiceTest extends LogicalService {

		private static final long serialVersionUID = 2058901305261017505L;

		LogicalServiceTest(LogicalDeployment ld, String label) {
			setLabel(label);
			ld.addLogicalService(this);
		}
		// simplify toString()
		public String toString() { return label; }		
	}
	
	class LogicalServiceTestTypeA extends LogicalServiceTest {
		private static final long serialVersionUID = 8728985488017570335L;

		LogicalServiceTestTypeA(LogicalDeployment ld, String label) {
			super(ld, label);
		}
	}

	class LogicalServiceTestTypeB extends LogicalServiceTest {
		private static final long serialVersionUID = -8396511925099166138L;

		LogicalServiceTestTypeB(LogicalDeployment ld, String label) {
			super(ld, label);
		}
	}
	
	// JeeProcessing test classes
	
	class LogicalExecutionNodeTest extends ProcessingNode {
		private static final long serialVersionUID = -1056208070223994032L;

		LogicalExecutionNodeTest(LogicalDeployment ld, String label) {
			super(label, ld);
		}
		// simplify toString()
		public String toString() { return label; }		
	}
	
	class LogicalExecutionNodeTestTypeA extends LogicalExecutionNodeTest {
		private static final long serialVersionUID = -1031622706161968856L;

		LogicalExecutionNodeTestTypeA(LogicalDeployment ld, String label) {
			super(ld, label);
		}
	}

	class LogicalExecutionNodeTestTypeB extends LogicalExecutionNodeTest {
		private static final long serialVersionUID = -6898349595490712170L;

		LogicalExecutionNodeTestTypeB(LogicalDeployment ld, String label) {
			super(ld, label);
		}
	}

}
