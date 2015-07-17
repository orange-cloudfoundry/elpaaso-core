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
package com.francetelecom.clara.cloud.core.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.francetelecom.clara.cloud.coremodel.ConfigRole;

public interface ConfigRoleRepository extends JpaRepository<ConfigRole, Integer> {

	public List<ConfigRole> findByApplicationUID(String applicatioUID);

	public ConfigRole findByUid(String uid);

	public List<ConfigRole> findByUidIn(List<String> uids);

}
