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
package com.francetelecom.clara.cloud.core.infrastructure;

import java.util.Arrays;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.core.domain.ApplicationRepository;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.dao.impl.GenericDaoJpaImpl;

/**
 * DAO implementation for Application Entity management
 */
public class ApplicationDaoJpaImpl extends GenericDaoJpaImpl<Application, Integer> implements ApplicationRepository {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationReleaseDaoJpaImpl.class);

	@Override
	public Application findByUid(String applicationUID) {
        return findByUid(applicationUID, false);
	}

    @Override
    public Application findByUid(String applicationUID, boolean evenRemovedOne) {
        try {
            String queryApp = "SELECT a FROM Application a WHERE a.uid = :applicationUID";
            if (!evenRemovedOne) {
                queryApp += " AND a.state<>'REMOVED'";
            }
            TypedQuery<Application> q = em.createQuery(queryApp, Application.class);
            q.setParameter("applicationUID", applicationUID);
            q.setMaxResults(1);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
	public Application findByLabel(String label) {
		// use of TypedQuery interface
		try {
			TypedQuery<Application> q = em.createQuery("SELECT a FROM Application a WHERE a.state<>'REMOVED' AND a.label = :label", Application.class);
			q.setParameter("label", label);
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Application findByCode(String code) {
		// use of TypedQuery interface
		try {
			TypedQuery<Application> q = em.createQuery("SELECT a FROM Application a WHERE a.state<>'REMOVED' AND a.code = :code", Application.class);
			q.setParameter("code", code);
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public long count() {
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(a) FROM Application a WHERE a.state<>'REMOVED'", Long.class);
		return q.getSingleResult();
	}

	@Override
	public long countByMember(SSOId member) {
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(a) FROM Application a JOIN a.members m WHERE a.state<>'REMOVED' AND m.value = :ssoId", Long.class);
		q.setParameter("ssoId", member.getValue());
		return q.getSingleResult();
	}

	@Override
	public List<Application> findAll() {
		TypedQuery<Application> q = em.createQuery("SELECT a FROM Application a WHERE a.state<>'REMOVED' ORDER BY a.label ASC", Application.class);
		return q.getResultList();
	}

	@Override
	public List<Application> findAllPublicOrPrivateByMember(SSOId ssoId) {
		TypedQuery<Application> q = em.createQuery("SELECT DISTINCT a FROM Application a LEFT JOIN a.members m WHERE a.state<>'REMOVED' AND (a.isPublic = true OR m.value = :ssoId) ORDER BY a.label ASC", Application.class);
		q.setParameter("ssoId", ssoId.getValue());
		return q.getResultList();
	}

	@Override
	public List<Application> findAll(int first, int count, String sortProperty, String sortType) {
		TypedQuery<Application> q = em.createQuery("SELECT DISTINCT a FROM Application a WHERE a.state<>'REMOVED' order by a."
				+ sortProperty + " " + sortType, Application.class);
		q.setFirstResult(first);
		q.setMaxResults(count);
		return q.getResultList();
	}

	@Override
	public List<Application> findAllByMember(SSOId ssoId,int first, int count, String sortProperty, String sortType) {
		TypedQuery<Application> q = em.createQuery("SELECT DISTINCT a FROM Application a JOIN a.members m WHERE a.state<>'REMOVED' AND m.value = :ssoId ORDER by a."
				+ sortProperty + " " + sortType, Application.class);
		q.setParameter("ssoId", ssoId.getValue());
		q.setFirstResult(first);
		q.setMaxResults(count);
		return q.getResultList();
	}
	

	@Override
    public List<Application> findRemovedApplicationWithoutRelease() {
        String query = "SELECT application"
                + " FROM Application application"
                + " WHERE application.state = 'REMOVED'"
                + " AND (SELECT count(ar) from ApplicationRelease ar where ar.application = application) = 0 " // no release
                ;
        TypedQuery<Application> q = em.createQuery(query,Application.class);
        return q.getResultList();
    }

    @Override
    public int purgeApplications(List<Application> applications) {
        Validate.notNull(applications);
        int nbRemoved = 0;
        for(Application application : applications) {
            boolean haveNoRelease = (countReleaseForAnApplication(application) == 0);
            if (haveNoRelease && application.isRemoved()) {
                purgeAnApplication(application); nbRemoved++;
            }
        }
        return nbRemoved;
    }

    private void purgeAnApplication(Application application) {
        Validate.notNull(application);
        LOG.info("PURGE Application :{}", application.toString());
        em.remove(application);
    }

    public int purgeApplication(Application application) {
        return purgeApplications(Arrays.asList(application));
    }

    private Long countReleaseForAnApplication(Application application) {
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(ar) FROM ApplicationRelease ar WHERE ar.application = :app", Long.class)
                .setParameter("app", application);
        Long countEnv = q.getSingleResult();
        return countEnv;
    }

    
}
