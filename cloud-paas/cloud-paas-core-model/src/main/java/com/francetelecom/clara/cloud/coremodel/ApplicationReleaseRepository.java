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
package com.francetelecom.clara.cloud.coremodel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

/**
 * DAO interface for ApplicationRelease Entity management
 */
public interface ApplicationReleaseRepository extends JpaRepository<ApplicationRelease, Integer> {

    /**
     * Retrieve an (not removed) application release from its uid.
     *
     * @param uid application release uid
     * @return an application release or null if no uid is matching.
     */
    @Query("SELECT ar FROM ApplicationRelease ar WHERE ar.uid=?1")
    ApplicationRelease findByUID(String uid);


    /**
     * Counts ACTIVE application releases for a specific application
     *
     * @param applicationUID the application UID related to the releases we want to count
     * @return the number of releases of the application
     */
    @Query("SELECT COUNT(ar) FROM ApplicationRelease ar JOIN ar.application a WHERE ar.state<>4 AND a.uid = ?1")
    long countApplicationReleasesByApplicationUID(String applicationUID);

    /**
     * find all ACTIVE application releases
     *
     * @return a Collection of ApplicationRelease
     */
    @Query("SELECT ar FROM ApplicationRelease ar JOIN ar.application a WHERE ar.state<>4 ORDER BY a.label, ar.releaseVersion ASC")
    List<ApplicationRelease> findAll();

    /**
     * Counts all application releases
     *
     * @return the total number of application releases
     */
    @Query("SELECT COUNT(ar) FROM ApplicationRelease ar WHERE ar.state<>4")
    long countApplicationReleases();

    /**
     * find application releases of active and private applications a user
     * is a member of
     *
     * @param ssoId ssoid of application member
     * @return a List of ApplicationRelease
     */
    @Query("SELECT ar FROM ApplicationRelease ar JOIN ar.application a JOIN a.members m WHERE ar.state<>4 AND m.value = ?1 ORDER BY ar.application.label, ar.releaseVersion ASC")
    Collection<ApplicationRelease> findAllByApplicationMember(String ssoId);

    /**
     * count all application releases of active and private applications a user
     * is a member of
     *
     * @param currentUser ssoid of application member
     * @return a count of ApplicationRelease
     */
    @Query("SELECT COUNT(ar) FROM ApplicationRelease ar JOIN ar.application a JOIN a.members m WHERE ar.state<>4 AND m.value = ?1")
    long countByApplicationMember(String currentUser);

    /**
     * find all application releases of active and ( public application or
     * private applications a user is a member of)
     *
     * @param ssoId ssoid of application member
     * @return a List of ApplicationRelease
     */
    @Query("SELECT ar FROM ApplicationRelease ar JOIN ar.application a LEFT JOIN a.members m WHERE ar.state<>4 AND (a.isPublic = true OR m.value = ?1) ORDER BY ar.application.label, ar.releaseVersion ASC")
    List<ApplicationRelease> findAllPublicOrPrivateByMember(String ssoId);

    /**
     * count application releases of active and ( public application or
     * private applications a user is a member of)
     *
     * @param currentUser ssoid of application member
     * @return a count of ApplicationRelease
     */
    @Query("SELECT COUNT(ar) FROM ApplicationRelease ar JOIN ar.application a LEFT JOIN a.members m WHERE ar.state<>4 AND (a.isPublic = true OR m.value = ?1)")
    long countPublicOrPrivateByMember(String currentUser);

    /**
     * count application releases of the given application if this application is active and public or
     * private and the given user is a member of
     *
     * @param currentUser          ssoid of application member
     * @param applicationUID UID of the application
     * @return a count of ApplicationRelease
     */
    @Query("SELECT COUNT(ar) FROM ApplicationRelease ar JOIN ar.application a LEFT JOIN a.members m WHERE a.uid = ?2 AND ar.state<>4 AND (a.isPublic = true OR m.value = ?1)")
    long countPublicOrPrivateByMemberAndByAppUID(String currentUser, String applicationUID);

    @Query("SELECT ar FROM ApplicationRelease ar JOIN ar.application a WHERE ar.state<>4 AND a.uid = ?1 ORDER BY ar.application.label, ar.releaseVersion ASC")
    List<ApplicationRelease> findApplicationReleasesByAppUID(String appUid);

    /**
     * Retrieve all application where there is QRS at least one
     *
     * @return list of application name
     */
    @Query("SELECT DISTINCT a.uid FROM ApplicationRelease ar, LogicalQueueReceiveService qrs JOIN ar.application a JOIN ar.logicalDeployment lg JOIN lg.logicalServices ls WHERE ar.state<>4 AND ls.id= qrs.id")
    List<String> findApplicationHavingQrs();

    /**
     * Retrieve all application versions from a specific application
     *
     * @param appUID application UID
     * @return list of release
     */
    @Query("SELECT DISTINCT ar.releaseVersion FROM ApplicationRelease ar JOIN ar.application a WHERE ar.state<>4 AND a.uid= ?1")
    List<String> findApplicationVersion(String appUID);

    /**
     * Find application release by application and version
     *
     * @param applicationUID application uid
     * @param releaseVersion release version
     * @return application release
     */
    @Query("SELECT DISTINCT ar FROM ApplicationRelease ar JOIN ar.application a WHERE ar.state<>4 AND a.uid= ?1 AND ar.releaseVersion= ?2")
    ApplicationRelease findByApplicationUIDAndReleaseVersion(String applicationUID, String releaseVersion);

    /**
     * Retrieve all service QueueReceiveService from a specific application
     * release
     *
     * @param applicationName name
     * @param releaseVersion  version
     * @return list of service name of Queue Receive Service
     */
    @Query("SELECT DISTINCT qrs.serviceName FROM ApplicationRelease ar, LogicalQueueReceiveService qrs JOIN ar.application a JOIN ar.logicalDeployment lg JOIN lg.logicalServices ls WHERE ar.state<>4 AND a.uid= ?1 and ar.releaseVersion= ?2 and ls.id= qrs.id")
    List<String> findQRSServiceName(String applicationName, String releaseVersion);

    /**
     * Retrieve all service versions from a specific service QRS
     *
     * @param applicationName name
     * @param releaseVersion  version
     * @param serviceName     service name
     * @return list of service versions of Queue Receive Service
     */
    @Query("SELECT DISTINCT qrs.serviceVersion FROM ApplicationRelease ar, LogicalQueueReceiveService qrs JOIN ar.application a JOIN ar.logicalDeployment lg JOIN lg.logicalServices ls WHERE ar.state<>4 AND a.uid= ?1 and ar.releaseVersion= ?2 and ls.id= qrs.id and qrs.serviceName= ?3")
    List<String> findQRSServiceVersion(String applicationName, String releaseVersion, String serviceName);

    /**
     * find application releases with the "REMOVED" status and without
     * associated environment
     *
     * @return list of applications releases that match
     */
    @Query("SELECT ar FROM ApplicationRelease ar WHERE ar.state = 4 AND (SELECT count(e) from Environment e where e.applicationRelease = ar) = 0 ")
    List<ApplicationRelease> findRemovedReleasesWithoutEnvironment();

    /**
     * find all application releases of the given application if this application is active and public or
     * private and the given user is a member of
     *
     * @param currentUser          ssoid of application member
     * @param applicationUID UID of the application
     * @return a List of ApplicationRelease
     */
    @Query("SELECT ar FROM ApplicationRelease ar JOIN ar.application a LEFT JOIN a.members m WHERE a.uid = ?2 AND ar.state<>4 AND (a.isPublic = true OR m.value = ?1) ORDER BY ar.application.label, ar.releaseVersion ASC")
    List<ApplicationRelease> findPublicOrPrivateByMemberAndByAppUID(String currentUser, String applicationUID);

}
