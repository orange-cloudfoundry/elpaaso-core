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

import com.francetelecom.clara.cloud.EnvironmentDescriptionHandler;
import com.francetelecom.clara.cloud.commons.DateHelper;
import com.francetelecom.clara.cloud.core.domain.EnvironmentRepository;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.dao.impl.GenericDaoJpaImpl;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: Loic Descotte Date: 25/02/11
 */
public class EnvironmentDaoJpaImpl extends GenericDaoJpaImpl<Environment, Integer> implements EnvironmentRepository {
	private static final Logger LOG = LoggerFactory.getLogger(EnvironmentDaoJpaImpl.class);

	@Override
	public List<Environment> findAllActive() {
		TypedQuery<Environment> q = em
				.createQuery(
						"SELECT e FROM Environment e where e.status <> 'REMOVED' and e.status <> 'REMOVING' ORDER BY e.applicationRelease.application.label, e.applicationRelease.releaseVersion, e.label ASC",
						Environment.class);
		return q.getResultList();
	}

	@Override
	public List<Environment> findAllActive(int first, int count) {
		TypedQuery<Environment> q = em
				.createQuery(
						"SELECT e FROM Environment e where e.status <> 'REMOVED' ORDER BY e.applicationRelease.application.label, e.applicationRelease.releaseVersion, e.label ASC",
						Environment.class);
		q.setFirstResult(first);
		q.setMaxResults(count);
		return q.getResultList();
	}

	@Override
	public List<Environment> findAllActive(int first, int count, String sortProperty, String sortType) {
		String query = "SELECT e FROM Environment e where e.status <> 'REMOVED' ORDER BY e." + sortProperty + " " + sortType;
		TypedQuery<Environment> q = em.createQuery(query, Environment.class);
		q.setFirstResult(first);
		q.setMaxResults(count);
		return q.getResultList();
	}

	@Override
	public List<Environment> findAllActiveByApplicationMember(SSOId member, int first, int count, String sortProperty, String sortType) {
		String query = "SELECT e FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.status <> 'REMOVED' AND m.value = :member ORDER BY e."
				+ sortProperty + " " + sortType;
		TypedQuery<Environment> q = em.createQuery(query, Environment.class);
		q.setParameter("member", member.getValue());
		q.setFirstResult(first);
		q.setMaxResults(count);
		return q.getResultList();

	}

	@Override
	public List<Environment> findAllPublicOrPrivateByMember(SSOId member, int first, int count, String sortProperty, String sortType) {
		String query = "SELECT e FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.status <> 'REMOVED' AND (a.isPublic = true OR m.value = :member) ORDER BY e."
				+ sortProperty + " " + sortType;
		TypedQuery<Environment> q = em.createQuery(query, Environment.class);
		q.setParameter("member", member.getValue());
		q.setFirstResult(first);
		q.setMaxResults(count);
		return q.getResultList();
	}

	@Override
	public List<Environment> findAllByOwner(PaasUser owner) {

		// use of TypedQuery interface
		try {
			TypedQuery<Environment> q = em.createQuery("SELECT e FROM Environment e where e.paasUser = :owner", Environment.class);
			q.setParameter("owner", owner);
			return q.getResultList();
		} catch (NoResultException e) {
			return null;
		}

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public boolean updateEnvironmentStateByTDI(int tdiId, EnvironmentStatus newStatus, String message, int percent) {
		String newStatusString = newStatus != null ? newStatus.toString() : "(null)";
		LOG.debug("updateEnvironmentStateByTDI tdi#{} newState={}:" + message, tdiId, newStatusString);
		// use of TypedQuery interface
		try {
			String selectedEnv = "SELECT e FROM Environment e where e.technicalDeploymentInstance.id = :tdiId";
			TypedQuery<Environment> q = em.createQuery(selectedEnv, Environment.class);
			q.setParameter("tdiId", tdiId);
			Environment env = q.getSingleResult();
			env.updateStatus(newStatus, message, percent);
		} catch (NoResultException e) {
			return false;
		}
		return true;
	}

	@Override
	@Transactional(readOnly = true)
    public ActivationContext getActivationContext(int tdiId, EnvironmentDescriptionHandler environmentDescriptionHandler) {
        try {
			TypedQuery<Environment> q = em.createQuery("SELECT e FROM Environment e where e.technicalDeploymentInstance.id = :tdiId",
					Environment.class);
			q.setParameter("tdiId", tdiId);
			Environment env = q.getSingleResult();
            return environmentDescriptionHandler.toActivationContext(env);
        } catch (NoResultException e) {
            return null;
        }
	}

	@Override
	public List<Environment> findAllActiveByApplicationReleaseUid(String applicationReleaseUID, int first, int count, String sortProperty,
			String sortType) {
		String query = "SELECT e FROM Environment e where e.applicationRelease.uid = :applicationReleaseUID and e.status <> 'REMOVED' ORDER BY e."
				+ sortProperty + " " + sortType;
		TypedQuery<Environment> q = em.createQuery(query, Environment.class);
		q.setFirstResult(first);
		q.setMaxResults(count);
		q.setParameter("applicationReleaseUID", applicationReleaseUID);
		return q.getResultList();
	}

	@Override
	public List<Environment> findAllByApplicationReleaseUidAndApplicationMember(String releaseUID, SSOId member, int first, int count,
			String sortProperty, String sortType) {
		String query = "SELECT e FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.applicationRelease.uid = :releaseUID AND e.status <> 'REMOVED' AND m.value = :member ORDER BY e."
				+ sortProperty + " " + sortType;
		TypedQuery<Environment> q = em.createQuery(query, Environment.class);
		q.setFirstResult(first);
		q.setMaxResults(count);
		q.setParameter("releaseUID", releaseUID);
		q.setParameter("member", member.getValue());
		return q.getResultList();
	}

	@Override
	public List<Environment> findAllPublicOrPrivateByMemberAndByApplicationRelease(String releaseUID, SSOId member, int first, int count,
			String sortProperty, String sortType) {
		String query = "SELECT e FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.applicationRelease.uid = :releaseUID AND e.status <> 'REMOVED' AND (a.isPublic = true OR m.value = :member) ORDER BY e."
				+ sortProperty + " " + sortType;
		TypedQuery<Environment> q = em.createQuery(query, Environment.class);
		q.setFirstResult(first);
		q.setMaxResults(count);
		q.setParameter("releaseUID", releaseUID);
		q.setParameter("member", member.getValue());
		return q.getResultList();
	}

	@Override
	public Long count() {
		Query q = em.createQuery("SELECT COUNT(e.id) FROM Environment e");
		return (Long) q.getSingleResult();
	}

	@Override
	public Long countActive() {
		Query q = em.createQuery("SELECT COUNT(e.id) FROM Environment e WHERE e.status != 'REMOVED'");
		return (Long) q.getSingleResult();
	}

	@Override
	public Long countActiveByOwner(PaasUser owner) {
		TypedQuery<Long> q = em.createQuery("SELECT count(e) FROM Environment e where e.status <> 'REMOVED' AND e.paasUser = :owner",
				Long.class);
		q.setParameter("owner", owner);
		return q.getSingleResult();
	}

	@Override
	public Long countActiveByApplicationMember(SSOId member) {
		String query = "SELECT count(e) FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.status <> 'REMOVED' AND m.value = :member";
		TypedQuery<Long> q = em.createQuery(query, Long.class);
		q.setParameter("member", member.getValue());
		return q.getSingleResult();
		
	}

	@Override
	public Long countPublicOrPrivateByMember(SSOId member) {
		String query = "SELECT count(e) FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.status <> 'REMOVED' AND (a.isPublic = true OR m.value = :member)";
		TypedQuery<Long> q = em.createQuery(query, Long.class);
		q.setParameter("member", member.getValue());
		return q.getSingleResult();
	}

	@Override
	public long countActiveByApplicationReleaseUid(String releaseUID) {
		// use of TypedQuery interface
		TypedQuery<Long> q = em.createQuery(
				"SELECT count(e) FROM Environment e JOIN e.applicationRelease ar WHERE ar.uid = :applicationReleaseUID AND e.status <> 'REMOVED'",
				Long.class);
		q.setParameter("applicationReleaseUID", releaseUID);
		return q.getSingleResult().intValue();
	}

	@Override
	public Long countAllPublicOrPrivateByMemberAndByApplicationRelease(String releaseUID, SSOId member) {
		String query = "SELECT count(e) FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.status <> 'REMOVED' AND ar.uid = :releaseUID AND (a.isPublic = true OR m.value = :member)";
		TypedQuery<Long> q = em.createQuery(query, Long.class);
		q.setParameter("member", member.getValue());
		q.setParameter("releaseUID", releaseUID);
		return q.getSingleResult();
	}
	
	@Override
	public Environment findByApplicationReleaseUIDAndLabel(String releaseUID, String label) {
		// use of TypedQuery interface
		try {
			TypedQuery<Environment> q = em.createQuery(
					"SELECT e FROM Environment e where e.applicationRelease.uid = :releaseUID and e.label = :label and e.status <> 'REMOVED'",
					Environment.class);
			q.setParameter("releaseUID", releaseUID);
			q.setParameter("label", label);
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Environment findByUID(String uid) {
		// use of TypedQuery interface
		try {
			TypedQuery<Environment> q = em.createQuery("SELECT e FROM Environment e where e.uid = :uid", Environment.class);
			q.setParameter("uid", uid);
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Environment findByTDIId(int tdiId) {
		// use of TypedQuery interface
		try {
			TypedQuery<Environment> q = em.createQuery("SELECT e FROM Environment e where e.technicalDeploymentInstance.id = :tdiId",
					Environment.class);
			q.setParameter("tdiId", tdiId);
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public List<Environment> findRemovedOlderThanNDays(int nbDay) {
		Date maxDate = DateHelper.getDateDeltaDay(-nbDay);
		String findQuery = "SELECT e FROM Environment e " + "WHERE e.status = 'REMOVED' " + "AND e.deletionDate IS NOT NULL "
				+ "AND e.deletionDate < :maxDate "
				+ "ORDER BY e.applicationRelease.application.label, e.applicationRelease.releaseVersion, e.label ASC";
		TypedQuery<Environment> q = em.createQuery(findQuery, Environment.class);
		q.setParameter("maxDate", maxDate, TemporalType.DATE);
		List<Environment> resultList = q.getResultList();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
		LOG.debug("findRemovedEnvironmentOlderThanNDays maxDate={} / resultSize={}", dateFormat.format(maxDate), resultList.size());
		return resultList;

	}

	@Override
	public int purgeEnvironments(List<Environment> environmentsToPurge) {
		Validate.notNull(environmentsToPurge);
		int nbRemoved = 0;
		for (Environment env : environmentsToPurge) {
			if (env.isRemoved()) {
				remove(env);
				nbRemoved++;
			}
		}
		return nbRemoved;
	}

	@Override
	public int purgeEnvironment(Environment environment) {
		if (environment == null) {
			return 0;
		}
		return purgeEnvironments(Arrays.asList(environment));

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public boolean forceRemovedStatusForAndEnvironment(String uid, EnvironmentStatus newStatus) {
		if (newStatus == null) {
			return false;
		}
		LOG.warn("forceRemovedStatusForAndEnvironment uid={}, newState={}", uid, newStatus.toString());
		// use of TypedQuery interface
		try {
			TypedQuery<Environment> q = em.createQuery("SELECT e FROM Environment e where e.uid = :uid", Environment.class);
			q.setParameter("uid", uid);
			Environment env = q.getSingleResult();
			env.setStatus(newStatus);
			em.merge(env);
		} catch (NoResultException e) {
			return false;
		}
		return true;
	}

}
