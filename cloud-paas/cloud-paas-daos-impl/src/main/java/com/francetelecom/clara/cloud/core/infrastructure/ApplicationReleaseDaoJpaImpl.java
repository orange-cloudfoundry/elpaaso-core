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

import java.util.Collection;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.francetelecom.clara.cloud.core.domain.ApplicationReleaseRepository;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.dao.impl.GenericDaoJpaImpl;

/**
 * DAO implementation for ApplicationRelease Entity management
 */
public class ApplicationReleaseDaoJpaImpl extends GenericDaoJpaImpl<ApplicationRelease, Integer> implements ApplicationReleaseRepository {

    @Override
    public ApplicationRelease findByUID(String uid) {
        return findByUID(uid, false);
    }

    @Override
    public ApplicationRelease findByUID(String uid, boolean evenRemovedOne) {
        try {
            String queryAR = "SELECT ar FROM ApplicationRelease ar WHERE ar.uid=:uid ";
            if (!evenRemovedOne) {
                queryAR += " AND ar.state<>4";
            }
            TypedQuery<ApplicationRelease> q = em.createQuery(queryAR, ApplicationRelease.class);
            q.setParameter("uid", uid);
            q.setMaxResults(1);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
	public Collection<ApplicationRelease> findApplicationReleasesByAppUID(String appUid) {
		TypedQuery<ApplicationRelease> q = em
				.createQuery(
						"SELECT ar FROM ApplicationRelease ar JOIN ar.application a WHERE ar.state<>4 AND a.uid = :applicationUID ORDER BY ar.application.label, ar.releaseVersion ASC",
						ApplicationRelease.class);
		q.setParameter("applicationUID", appUid);
		return q.getResultList();
	}

	@Override
	public List<ApplicationRelease> findAll() {
		TypedQuery<ApplicationRelease> q = em.createQuery("SELECT ar FROM ApplicationRelease ar JOIN ar.application a WHERE ar.state<>4 ORDER BY a.label, ar.releaseVersion ASC", ApplicationRelease.class);
		return q.getResultList();
	}

    @Override
    public List<ApplicationRelease> findAll(int first, int count) {
    	TypedQuery<ApplicationRelease> q = em.createQuery(
    			"SELECT ar FROM ApplicationRelease ar JOIN ar.application a WHERE ar.state<>4 ORDER BY a.label, ar.releaseVersion ASC", ApplicationRelease.class);
    	q.setFirstResult(first);
    	q.setMaxResults(count);
    	return q.getResultList();
    }

    @Override
	public long countApplicationReleases() {
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(ar) FROM ApplicationRelease ar WHERE ar.state<>4", Long.class);
		return q.getSingleResult();
	}

	@Override
	public Collection<ApplicationRelease> findAllByApplicationMember(SSOId ssoId, int first, int count) {
		TypedQuery<ApplicationRelease> q = em.createQuery(
				"SELECT ar FROM ApplicationRelease ar JOIN ar.application a JOIN a.members m WHERE ar.state<>4 AND m.value = :ssoId ORDER BY ar.application.label, ar.releaseVersion ASC", ApplicationRelease.class);
		q.setParameter("ssoId", ssoId.getValue());
		q.setFirstResult(first);
		q.setMaxResults(count);
		return q.getResultList();
	}

	@Override
	public long countByApplicationMember(SSOId ssoId) {
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(ar) FROM ApplicationRelease ar JOIN ar.application a JOIN a.members m WHERE ar.state<>4 AND m.value = :ssoId", Long.class);
		q.setParameter("ssoId", ssoId.getValue());
		return q.getSingleResult();
	}
	
	@Override
	public List<ApplicationRelease> findAllPublicOrPrivateByMember(SSOId ssoId, int first, int count) {
		TypedQuery<ApplicationRelease> q = em.createQuery(
				"SELECT ar FROM ApplicationRelease ar JOIN ar.application a LEFT JOIN a.members m WHERE ar.state<>4 AND (a.isPublic = true OR m.value = :ssoId) ORDER BY ar.application.label, ar.releaseVersion ASC", ApplicationRelease.class);
		q.setParameter("ssoId", ssoId.getValue());
		q.setFirstResult(first);
		q.setMaxResults(count);
		return q.getResultList();
	}
	
	@Override
	public List<ApplicationRelease> findPublicOrPrivateByMemberAndByAppUID(SSOId ssoId, String appUid, int first, int count) {
		TypedQuery<ApplicationRelease> q = em.createQuery(
				"SELECT ar FROM ApplicationRelease ar JOIN ar.application a LEFT JOIN a.members m WHERE a.uid = :applicationUID AND ar.state<>4 AND (a.isPublic = true OR m.value = :ssoId) ORDER BY ar.application.label, ar.releaseVersion ASC", ApplicationRelease.class);
		q.setParameter("ssoId", ssoId.getValue());
		q.setParameter("applicationUID", appUid);
		q.setFirstResult(first);
		q.setMaxResults(count);
		return q.getResultList();
	}

	@Override
	public long countPublicOrPrivateByMember(SSOId ssoId) {
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(ar) FROM ApplicationRelease ar JOIN ar.application a LEFT JOIN a.members m WHERE ar.state<>4 AND (a.isPublic = true OR m.value = :ssoId)", Long.class);
		q.setParameter("ssoId", ssoId.getValue());
		return q.getSingleResult();
	}

	@Override
	public long countPublicOrPrivateByMemberAndByAppUID(SSOId ssoId, String appUID) {
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(ar) FROM ApplicationRelease ar JOIN ar.application a LEFT JOIN a.members m WHERE a.uid = :applicationUID AND ar.state<>4 AND (a.isPublic = true OR m.value = :ssoId)", Long.class);
		q.setParameter("ssoId", ssoId.getValue());
		q.setParameter("applicationUID", appUID);
		return q.getSingleResult();
	}

	@Override
	public long countApplicationReleasesByApplicationUID(String applicationUID) {
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(ar) FROM ApplicationRelease ar JOIN ar.application a WHERE ar.state<>4 AND a.uid = :applicationUID",
				Long.class);
		q.setParameter("applicationUID", applicationUID);
		return q.getSingleResult().intValue();
	}

	@Override
	public Collection<ApplicationRelease> findApplicationReleasesByAppUID(String appUid, int first, int count) {
		TypedQuery<ApplicationRelease> q = em
				.createQuery(
						"SELECT ar FROM ApplicationRelease ar JOIN ar.application a WHERE ar.state<>4 AND a.uid = :applicationUID ORDER BY ar.application.label, ar.releaseVersion ASC",
						ApplicationRelease.class);
		q.setParameter("applicationUID", appUid);
		q.setFirstResult(first);
		q.setMaxResults(count);
		return q.getResultList();
	}

	@Override
	public List<String> findApplicationHavingQrs() {
		TypedQuery<String> q = em
				.createQuery(
						"SELECT DISTINCT a.uid FROM ApplicationRelease ar, LogicalQueueReceiveService qrs JOIN ar.application a JOIN ar.logicalDeployment lg JOIN lg.logicalServices ls WHERE ar.state<>4 AND ls.id= qrs.id",
						String.class);
		return q.getResultList();
	}

	@Override
	// TODO should be renamed ->getByAppllicationUID
	public List<String> findApplicationVersion(String applicationUID) {
		TypedQuery<String> q = em.createQuery(
				"SELECT DISTINCT ar.releaseVersion FROM ApplicationRelease ar JOIN ar.application a WHERE ar.state<>4 AND a.uid= :applicationName",
				String.class);
		q.setParameter("applicationName", applicationUID);
		return q.getResultList();
	}

	@Override
	public ApplicationRelease findByApplicationUIDAndReleaseVersion(String applicationUID, String releaseVersion) {
		try {
			TypedQuery<ApplicationRelease> q = em
					.createQuery(
							"SELECT DISTINCT ar FROM ApplicationRelease ar JOIN ar.application a WHERE ar.state<>4 AND a.uid= :applicationUID AND ar.releaseVersion= :version",
							ApplicationRelease.class);
			q.setParameter("applicationUID", applicationUID);
			q.setParameter("version", releaseVersion);
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public List<String> findQRSServiceName(String applicationName, String releaseVersion) {
		TypedQuery<String> q = em
				.createQuery(
						"SELECT DISTINCT qrs.serviceName FROM ApplicationRelease ar, LogicalQueueReceiveService qrs JOIN ar.application a JOIN ar.logicalDeployment lg JOIN lg.logicalServices ls WHERE ar.state<>4 AND a.uid= :applicationName and ar.releaseVersion= :releaseVersion and ls.id= qrs.id",
						String.class);
		q.setParameter("applicationName", applicationName);
		q.setParameter("releaseVersion", releaseVersion);
		return q.getResultList();
	}

	@Override
	public List<String> findQRSServiceVersion(String applicationName, String releaseVersion, String serviceName) {
		TypedQuery<String> q = em
				.createQuery(
						"SELECT DISTINCT qrs.serviceVersion FROM ApplicationRelease ar, LogicalQueueReceiveService qrs JOIN ar.application a JOIN ar.logicalDeployment lg JOIN lg.logicalServices ls WHERE ar.state<>4 AND a.uid= :applicationName and ar.releaseVersion= :releaseVersion and ls.id= qrs.id and qrs.serviceName= :serviceName",
						String.class);
		q.setParameter("applicationName", applicationName);
		q.setParameter("releaseVersion", releaseVersion);
		q.setParameter("serviceName", serviceName);
		return q.getResultList();
	}

    @Override
    public List<ApplicationRelease> findRemovedReleasesWithoutEnvironment() {
        String query = "SELECT ar"
                + " FROM ApplicationRelease ar"
                + " WHERE ar.state = 4" // release is removed
                + " AND (SELECT count(e) from Environment e where e.applicationRelease = ar) = 0 " // no environment
                ;
        TypedQuery<ApplicationRelease> q = em.createQuery(query,ApplicationRelease.class);
        return q.getResultList();
    }

  

}
