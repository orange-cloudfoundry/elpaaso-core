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
package com.francetelecom.clara.cloud.techmodel;

import com.francetelecom.clara.cloud.commons.P6SpyAppender;
import com.francetelecom.clara.cloud.commons.PersistenceTestUtil;
import com.francetelecom.clara.cloud.commons.ValidatorUtil;
import com.francetelecom.clara.cloud.model.ModelItem;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * AbstractPersistenceTest
 * This class allow persist test
 * <p/>
 * last changed : $LastChangedDate: 2011-11-03 17:25:06 +0100 (jeu., 03 nov. 2011) $
 * last author  : $Author: dwvd1206 $
 *
 * @version : $Revision: 11019 $
 */
@ContextConfiguration
public abstract class AbstractPersistenceTest {

    protected final static Logger LOG = LoggerFactory.getLogger(AbstractPersistenceTest.class.getName());

    @Autowired
    protected PersistenceTestUtil persistenceTestUtil;

    @PersistenceContext
    protected EntityManager pm;

    P6SpyAppender p6spyAppender = null;

    @Before
    public void setUp() {
        p6spyAppender = P6SpyAppender.getCurrentInstance();
        if (p6spyAppender != null) {
            p6spyAppender.reset();
        }
    }

    @After
    public void tearDown() {
        p6spyAppender = P6SpyAppender.getCurrentInstance();
        if (p6spyAppender != null) {
            p6spyAppender.checkStats(false);
        }
    }

    protected <T extends ModelItem> T validateAndPersistModel(T modelItem) {
        return validateAndPersistModel(modelItem, true);
    }

    protected <T extends ModelItem> T validateAndPersistModel(T modelItem, boolean forceEagerFetching) {
        String action = "(validatorUtil) validating";
        try {
            ValidatorUtil.validate(modelItem);
            action = "(pm) persisting";

            persistenceTestUtil.persistObject(modelItem);
        } catch (javax.validation.ConstraintViolationException cve) {
            Set<ConstraintViolation<?>> viols = cve.getConstraintViolations();
            LOG.error("ConstraintViolationException details :");
            for (ConstraintViolation v : viols) {
                LOG.error("\t violation {}", v.toString());
            }
            fail("unexpected invalid model\n javax.validation.ConstraintViolationException while "
                    + action + " : see ERROR logs for more details about ConstraintViolations");
        } catch (Exception e) {
            fail("unexpected invalid model ; exception while " + action + " : " + e);
        }

        T reloadedEntity = (T) persistenceTestUtil.reloadEntity(modelItem.getClass(), modelItem.getId(), forceEagerFetching);
        return reloadedEntity;
    }
}
