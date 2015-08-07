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
package com.francetelecom.clara.cloud.application.impl;

import com.francetelecom.clara.cloud.deployment.logical.service.LogicalDeploymentImportExport;
import com.francetelecom.clara.cloud.logicalmodel.CFJavaProcessing;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class LogicalDeploymentImportExportTest {

    @Autowired
    LogicalDeploymentImportExport ldImportExport;
    @Autowired
    @Qualifier(value = "wicketooLogicalModelCatalog")
    SampleAppFactory wicketooFactory;
    @Autowired
    @Qualifier(value = "pwmLogicalModelCatalog")
    SampleAppFactory pwmFactory;
    @Autowired
    @Qualifier(value = "elPaaSoTomcatLogicalModelCatalog")
    SampleAppFactory elpaasoFactory;


    @Test
    public void architecture_can_be_dumped_in_xml_format() {
        //given an existing wicketoo architecture
        LogicalDeployment wicketoo = wicketooFactory.populateLogicalDeployment(null);
        //given a pwm architecture just dumped in xml format: : single CFJavaProcessing without db
        LogicalDeployment pwmLd = pwmFactory.populateLogicalDeployment(null);
        ProcessingNode pwmProcessing = pwmLd.listProcessingNodes(CFJavaProcessing.class).get(0);
        final String pwmDump = ldImportExport.exportDump(pwmLd);

        //then I can find some useful data in the xml, such as exec node label, and maven ref
        assertThat(pwmDump).contains(pwmProcessing.getLabel());
        assertThat(pwmDump).contains(pwmProcessing.getSoftwareReference().getArtifactId());
    }

    @Test
    public void allows_to_import_a_pwm_into_an_existing_wicketoo_architecture() {
        //given an existing wicketoo architecture
        LogicalDeployment wicketoo = wicketooFactory.populateLogicalDeployment(null);
        //given a pwm architecture just dumped in xml format: : single CFJavaProcessing without db
        LogicalDeployment pwmLd = pwmFactory.populateLogicalDeployment(null);
        final String pwmDump = ldImportExport.exportDump(pwmLd);


        //when I import the pwm xml dump
        ldImportExport.importDump(wicketoo, pwmDump);


        //then my merged architecture has a new phpmysqladmin jeerprocesssing,
        // not yet associated to match RDB
        LogicalDeployment expectedLd = wicketooFactory.populateLogicalDeployment(null);
        pwmFactory.populateLogicalDeployment(expectedLd);

        assertThat(wicketoo).as("merged ld").isEqualTo(expectedLd);
    }

    @Test
    public void allows_to_import_an_large_architecture_into_a_existing_simple_one() {
        //given an existing pwm architecture
        LogicalDeployment pwmLd = pwmFactory.populateLogicalDeployment(null);
        //given an elpaaso architecture just dumped in xml format:
        LogicalDeployment elpaaso = elpaasoFactory.populateLogicalDeployment(null);
        final String elpaasoDump = ldImportExport.exportDump(elpaaso);


        //when I import the pwm xml dump
        ldImportExport.importDump(pwmLd, elpaasoDump);


        //then my merged architecture has a new phpmysqladmin jeerprocesssing,
        // not yet associated to match RDB
        LogicalDeployment expectedLd = pwmFactory.populateLogicalDeployment(null);
        elpaasoFactory.populateLogicalDeployment(expectedLd);

        assertThat(pwmLd).as("merged ld").isEqualTo(expectedLd);
    }

    @Test
    @Ignore("TODO p1")
    public void imported_architecture_can_be_persisted() {
        //persist and reload original
        //persist and reload imported
        //assert they are equals
    }

    @Test
    @Ignore("only kept to illustrates real-life service to import, but we lack it into the sample service")
    public void allows_to_import_a_phpmyadmin_into_an_existing_architecture() {
        //given an existing wicketoo architecture
        LogicalDeployment wicketoo = wicketooFactory.populateLogicalDeployment(null);
        //given a phpmysqladmin architecture dumped in xml format: : single jeeprocessing without db

        //when I import the pwm xml dump

        //then my merged architecture has a new phpmysqladmin jeerprocesssing,
        // not yet associated to match RDB

    }


    @Test
    @Ignore("only kept to illustrates real-life service to import, but we lack it into the sample service")
    public void allows_to_import_a_jenkins_into_an_existing_architecture() {
        //given an existing elpaaso architecture
        //given a jenkins architecture dumped in xml format: single jeeprocessing

        //when I import the jenkins xml dump

        //then my merged architecture has a new jenkins jeerprocesssing,
    }

    @Test
    public void import_from_an_empty_architecture_allows_editing_an_existing_architecture_in_xml_format_an_import_it() {
        //given an empty original architecture
        LogicalDeployment emptyOriginal = new LogicalDeployment();
        //and an elpaaso architecture I dumped to XML and potentially edited to change EAR references to current release built by CI build
        LogicalDeployment elpaaso = elpaasoFactory.populateLogicalDeployment(null);
        final String elpaasoDump = ldImportExport.exportDump(elpaaso);

        //when I import the elpaaso xml dump
        ldImportExport.importDump(emptyOriginal, elpaasoDump);

        //then my imported architecture has the elpaaso RELEASe version built by my ci
        LogicalDeployment expectedLd = elpaasoFactory.populateLogicalDeployment(null);
        assertThat(emptyOriginal).as("merged ld").isEqualTo(expectedLd);
    }

    @Test
    @Ignore
    public void import_from_an_empty_architecture_allows_editing_an_existing_api_designer_ri_template_architecture_in_xml_format_an_import_it() {
        //given an empty original architecture
        LogicalDeployment emptyOriginal = new LogicalDeployment();
        //and an api_designer reference implementation template architecture I dumped to XML and potentially edited to change WAR GAV
        // references to current release built by CI build
        SampleAppFactory apiDesignerReferenceImplArchitectureTemplate = elpaasoFactory;
        LogicalDeployment elpaaso = apiDesignerReferenceImplArchitectureTemplate.populateLogicalDeployment(null);
        final String elpaasoDump = ldImportExport.exportDump(elpaaso);

        //when I import the generated API reference impl
        ldImportExport.importDump(emptyOriginal, elpaasoDump);

        //then my imported architecture has the elpaaso RELEASe version built by my ci
        LogicalDeployment expectedLd = apiDesignerReferenceImplArchitectureTemplate.populateLogicalDeployment(null);
        assertThat(emptyOriginal).as("merged ld").isEqualTo(expectedLd);
    }

    @Test
    @Ignore
    public void p2_xml_dumps_should_be_human_readeable_without_extra_id_version_name_technical_fields() {
        //given

        //when

        //then

        //missing fields get default values (e.g. LogicalNodeServiceAssociation.accessType
        //                                 <accessType>NOT_APPLICABLE</accessType>

    }

    @Test
    @Ignore
    public void p3_xml_dumps_should_be_stable_in_time_to_allow_projects_to_store_them_in_their_scm() {
        //should have a version schema
        //should report invalid schema

        //in terms of tests, store the xml dump in resources/ as to detect changes/incompatibilities in formats across pass releases
    }


    @Test
    public void import_should_rename_conflicting_elements_in_the_imported_version() {
        //given an existing pwm architecture
        LogicalDeployment original = pwmFactory.populateLogicalDeployment(null);
        //given an 2nd elpaaso architecture just dumped in xml format:
        LogicalDeployment duplicate = pwmFactory.populateLogicalDeployment(null);
        final String duplicateDump = ldImportExport.exportDump(duplicate);

        //when I import the pwm xml dump
        ldImportExport.importDump(original, duplicateDump);

        //then my merged architecture has two independent pwm elements
        LogicalDeployment expectedLd = pwmFactory.populateLogicalDeployment(null);
        suffixLabelOnAllExecNodeAndService(expectedLd, "-2");
        pwmFactory.populateLogicalDeployment(expectedLd);

        assertThat(original).as("merged ld").isEqualTo(expectedLd);
    }

    @Test
    public void duplicate_candidate_elements_are_renamed() {
        //given an original ld: pwm
        LogicalDeployment original = pwmFactory.populateLogicalDeployment(null);
        //given a candidate ld: pwm
        LogicalDeployment duplicate = pwmFactory.populateLogicalDeployment(null);


        //when I ask to rename duplicates
        ldImportExport.renameDuplicatesLogicalItems(original, duplicate);
        //then

        LogicalDeployment expectedLd = pwmFactory.populateLogicalDeployment(null);
        suffixLabelOnAllExecNodeAndService(expectedLd, "-2");

        assertThat(duplicate).as("renamed ld").isEqualTo(expectedLd);
    }
    @Test
    public void nonduplicate_candidate_elements_are_not_renamed() {
        //given an original ld: pwm
        LogicalDeployment original = pwmFactory.populateLogicalDeployment(null);
        //given a candidate ld: pwm
        LogicalDeployment nonDuplicate = elpaasoFactory.populateLogicalDeployment(null);


        //when I ask to rename duplicates
        ldImportExport.renameDuplicatesLogicalItems(original, nonDuplicate);
        //then

        LogicalDeployment expectedLd = elpaasoFactory.populateLogicalDeployment(null);

        assertThat(nonDuplicate).as("renamed ld").isEqualTo(expectedLd);
    }

    @Test
    public void successive_duplicate_candidate_elements_renamed_to_non_conflict() {
        //given an original ld: pwm + pwm-2
        LogicalDeployment pwm = pwmFactory.populateLogicalDeployment(null);
        suffixLabelOnAllExecNodeAndService(pwm, "-2");
        pwmFactory.populateLogicalDeployment(pwm);
        //given a candidate ld: pwm
        LogicalDeployment duplicate = pwmFactory.populateLogicalDeployment(null);


        //when I ask to rename duplicates against pwm
        ldImportExport.renameDuplicatesLogicalItems(pwm, duplicate);

        //then

        LogicalDeployment expectedLd = pwmFactory.populateLogicalDeployment(null);
        suffixLabelOnAllExecNodeAndService(expectedLd, "-3");

        assertThat(duplicate).as("renamed ld").isEqualTo(expectedLd);
    }

    private void suffixLabelOnAllExecNodeAndService(LogicalDeployment ld, String suffix) {
        List<ProcessingNode> executionNodes = ld.listProcessingNodes(ProcessingNode.class);
        for (ProcessingNode executionNode : executionNodes) {
            executionNode.setLabel(executionNode.getLabel() + suffix);
            suffixLabelForAllServices(executionNode, suffix);
        }

    }

    private void suffixLabelForAllServices(ProcessingNode executionNode, String suffix) {
        List<LogicalService> services = executionNode.listLogicalServices();
        for (LogicalService service : services) {
            service.setLabel(service.getLabel() + suffix);
        }

    }

}
