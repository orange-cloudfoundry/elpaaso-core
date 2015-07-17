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
package com.francetelecom.clara.cloud.db.liquibase;

import com.francetelecom.clara.cloud.test.database.DbaasDatabase;
import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * This test compare a DB initialized using liquibase change log with a db initialiazed using hibernate automatic schema creation (hbm2ddl.auto = create)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public abstract class CompareChangeLogWithHibernateAutoCreateIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareChangeLogWithHibernateAutoCreateIT.class.getName());

    // 1st test DB initialized using hibernate in create-drop mode
    @Autowired
    DbaasDatabase db1;

    // 2nd test DB initialized using liquibase
    @Autowired
    DbaasDatabase db2;

    private final LiquibaseTestWrapper liquibaseWrapper = new LiquibaseTestWrapper();


    @Test
    public void compare() throws SQLException, LiquibaseException, IOException, ParserConfigurationException {

        // Test exercise: Run diff
        DiffResult diffResult = liquibaseWrapper.diff(
                db1.getUrl(), db1.getUser(), db1.getPassword(),
                db2.getUrl(), db2.getUser(), db2.getPassword());

        if (diffResult != null) {
            DiffOutputControl diffOutputControl = liquibaseWrapper.getTablespaceOnlyDiff();
            ObjectChangeFilter customFilter = new ObjectChangeFilter() {
                StandardObjectChangeFilter columnWithDefaultValueFilter = new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, "Column:middlewareprofileversion,Column:path,Column:servicename");

                @Override
                public boolean includeMissing(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase) {
                    return true;
                }

                @Override
                public boolean includeUnexpected(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase) {
                    return true;
                }

                @Override
                public boolean includeChanged(DatabaseObject object, ObjectDifferences differences, Database referenceDatabase, Database comparisionDatabase) {
                    LOGGER.debug("Check default value for {} - {} difference(s) found(s)", object, differences.getDifferences().size());
                    if (!(object instanceof Column)) {
                        return true;
                    }
                    if (!columnWithDefaultValueFilter.includeChanged(object, differences, referenceDatabase, comparisionDatabase)) {
                        differences.removeDifference("defaultValue");
                    }
                    boolean includeChangeOnlyIfHasDifferences = differences.hasDifferences();
                    return includeChangeOnlyIfHasDifferences;
                }
            };
            diffOutputControl.setObjectChangeFilter(customFilter);

            DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, diffOutputControl);

            changeLogWriter.setChangeSetAuthor("paas");
            File shouldBeAnEmptyChangelog = File.createTempFile("changelogDiff-", ".xml", new File("target/"));
            try (PrintStream ps = new PrintStream(new FileOutputStream(shouldBeAnEmptyChangelog), true)) {
                changeLogWriter.print(ps);
            }
            boolean differenceFound = searchForDifferenceInXml(shouldBeAnEmptyChangelog);//FileUtils.contentEqualsIgnoreEOL(emptyChangelogFile, shouldBeAnEmptyChangelog, "UTF-8");
            assertFalse("They are differences: \n" + FileUtils.readFileToString(shouldBeAnEmptyChangelog, "UTF-8"), differenceFound);
        }
    }

    private boolean searchForDifferenceInXml(File xmlFile) {
        boolean differenceFound = false;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringElementContentWhitespace(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            Element databaseChangeLogRoot=document.getDocumentElement();
            differenceFound=databaseChangeLogRoot.hasChildNodes();
            // Do something with the document here.
        } catch (ParserConfigurationException | IOException |SAXException e) {
            LOGGER.info("Failed to parse xml file: {}",xmlFile,e);
        }
        return differenceFound;
    }

    private boolean hasDifference(DiffResult diffResult) {
        if (diffResult == null) {
            return false;
        }
        return diffResult.getChangedObjects().size() > 0 || diffResult.getMissingObjects().size() > 0 || diffResult.getUnexpectedObjects().size() > 0;
    }
}
